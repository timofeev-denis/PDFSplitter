package ru.ntmedia;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public class MainDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField srcTextField;
    private JButton setSrcFolderButton;
    private JTextField destTextField;
    private JButton setDestFolderButton;
    private JSeparator separator;
    private JCheckBox manualDest;
    //private String srcFolder;
    //private String destFolder;

    public static void main(String[] args) {
        System.err.println("main()");
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.err.println("Run()");
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MainDialog dlg = new MainDialog();
                dlg.pack();
                dlg.setLocationRelativeTo(null);
                dlg.setVisible(true);

            }
        });

    }
    public MainDialog() {
        Image icon = new ImageIcon(getClass().getClassLoader().getResource("split-16.png")).getImage();
        this.setIconImage(icon);
        this.setTitle("Постраничное разделение файлов PDF");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setComponentsText();
        destTextField.setEditable(false);

        buttonOK.setEnabled(false);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setSrcFolderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileDialog = new JFileChooser(System.getProperty("user.dir"));
                fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if( fileDialog.showOpenDialog(MainDialog.this) == JFileChooser.APPROVE_OPTION ) {
                    srcTextField.setText(fileDialog.getSelectedFile().toString());
                }
            }
        });
        setDestFolderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileDialog = new JFileChooser(System.getProperty("user.dir"));
                fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if( fileDialog.showSaveDialog(MainDialog.this) == JFileChooser.APPROVE_OPTION ) {
                    destTextField.setText(fileDialog.getSelectedFile().toString());
                }
            }
        });
        srcTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent documentEvent) {
                updateDest();
                updateOkButton();
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                updateDest();
                updateOkButton();
            }

            public void changedUpdate(DocumentEvent documentEvent) {
                updateDest();
                updateOkButton();
            }
        });
        destTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent documentEvent) {
                updateOkButton();
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                updateOkButton();
            }

            public void changedUpdate(DocumentEvent documentEvent) {
                updateOkButton();
            }
        });


        //
        //setDebugParameters();
        manualDest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if( manualDest.isSelected() ) {
                    destTextField.setEditable(true);
                } else {
                    destTextField.setEditable(false);
                }
            }
        });
    }

    private void updateDest() {
        if (!manualDest.isSelected()) {
            if (srcTextField.getText().equals("")) {
                destTextField.setText("");
            } else {
                destTextField.setText(srcTextField.getText() + File.separator + "Split");
            }
        }
    }
    private void updateOkButton() {
        if(srcTextField.getText().equals("") || destTextField.getText().equals("")) {
            buttonOK.setEnabled(false);
        } else {
            buttonOK.setEnabled(true);
        }
    }

    private void setDebugParameters() {
        srcTextField.setText("e:\\tmp\\Excel\\");
        destTextField.setText("e:\\tmp\\Excel\\");
        updateOkButton();
    }
    private void onOK() {
        if(!Files.exists(Paths.get(srcTextField.getText()))) {
            JOptionPane.showMessageDialog(null, "Указанный каталог с файлами PDF не существует. Выберите другой каталог.", "Разделение файлов PDF", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if(!Files.exists(Paths.get(destTextField.getText()))) {
            try {
                Files.createDirectory(Paths.get(destTextField.getText()));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Не удалось создать каталог для сохранения результата. Выберите другой каталог.", "Разделение файлов PDF", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        File dir = new File(srcTextField.getText());
        if(dir == null) {
            JOptionPane.showMessageDialog(null, "Указанный каталог с файлами PDF не существует. Выберите другой каталог.", "Разделение файлов PDF", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for(File f : dir.listFiles(new FilenameFilter() {
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(".pdf");
            }
        })) {
            try {
                PDDocument doc = PDDocument.load(f);
                int pageCounter = 0;
                for(PDPage page : doc.getPages()) {
                    pageCounter++;
                    PDDocument tmp  = new PDDocument();
                    tmp.addPage(page);
                    tmp.save(getResultFileName(f.getAbsolutePath(), pageCounter));
                    tmp.close();
                    System.out.println( pageCounter );
                }
                //System.err.println("PAGES: " + doc.getPages().getCount());
                doc.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "При обработке файла произошла ошибка:\n" + e.getMessage(), "Разделение файлов PDF", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        dispose();
    }
    private String getResultFileName(String fileName, int fileCounter) {
        // cut path
        String baseFileName = Paths.get(fileName).getFileName().toString();
        // cut extension
        String rootFileName = baseFileName.substring(0, baseFileName.lastIndexOf("."));
        return String.format(destTextField.getText() + File.separator + rootFileName + "-%1$03d.pdf", fileCounter);
    }
    private void onCancel() {
        System.err.println("onCancel()");
        dispose();
    }

    private void setComponentsText() {
        UIManager.put("FileChooser.openDialogTitleText", "Открыть");
        UIManager.put("FileChooser.saveDialogTitleText", "Сохранить");
        UIManager.put("FileChooser.lookInLabelText", "Каталог");
        UIManager.put("FileChooser.openButtonText", "Открыть");
        UIManager.put("FileChooser.saveButtonText", "Выбрать");
        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла");
        UIManager.put("FileChooser.folderNameLabelText", "Каталог");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Типы файлов");
        UIManager.put("FileChooser.openButtonToolTipText", "OpenSelectedFile");
        UIManager.put("FileChooser.cancelButtonToolTipText","Отмена");
        UIManager.put("FileChooser.fileNameHeaderText","Имя файла");
        UIManager.put("FileChooser.upFolderToolTipText", "UpOneLevel");
        UIManager.put("FileChooser.homeFolderToolTipText","Desktop");
        UIManager.put("FileChooser.newFolderToolTipText","CreateNewFolder");
        UIManager.put("FileChooser.listViewButtonToolTipText","List");
        UIManager.put("FileChooser.newFolderButtonText","CreateNewFolder");
        UIManager.put("FileChooser.renameFileButtonText", "RenameFile");
        UIManager.put("FileChooser.deleteFileButtonText", "DeleteFile");
        UIManager.put("FileChooser.filterLabelText", "Типы файлов");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Details");
        UIManager.put("FileChooser.fileSizeHeaderText","Size");
        UIManager.put("FileChooser.fileDateHeaderText", "DateModified");
        UIManager.put("FileChooser.acceptAllFileFilterText", "Все файлы");
    }
}
