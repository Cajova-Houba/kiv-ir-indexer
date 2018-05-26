package cz.zcu.kiv.nlp.ir.trec.gui;

import cz.zcu.kiv.nlp.ir.trec.Main;
import cz.zcu.kiv.nlp.ir.trec.data.DocumentNew;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Panel for managing search index.
 */
public class IndexManagementPanel extends JPanel {

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    private JTextField documentIdField;
    private JTextField documentTitleField;
    private JTextArea documentTextArea;
    private JFormattedTextField documentDateField;
    private JLabel indexedDocumentsCountDisplay;
    private JButton indexBtn;

    public IndexManagementPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(300,400));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Index management"),
                BorderFactory.createEmptyBorder(10,10,10,10)
                ));

        addComponents();
    }

    private void addComponents() {
        add(createIndexedDocumentsNumDisplay());
        add(createIndexDocumentPanel());
        add(createSaveIndexBtn());
        add(createLoadIndexBtn());
    }

    private JPanel createIndexedDocumentsNumDisplay() {
        JPanel panel = new JPanel();
        panel.setAlignmentX(0.5f);
        panel.setSize(50,20);
        indexedDocumentsCountDisplay = new JLabel("0");
        panel.add(new JLabel("# of indexed documents: "));
        panel.add(indexedDocumentsCountDisplay);
        return panel;
    }

    private JPanel createIndexDocumentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setAlignmentX(0.5f);
        panel.setBorder(BorderFactory.createTitledBorder("Index new document"));

        // init components
        documentIdField = new JTextField(10);
        documentTextArea = new JTextArea(7,18);
        documentTitleField = new JTextField(10);
        documentDateField = new JFormattedTextField(new SimpleDateFormat(DATE_FORMAT));
        resetNewDocForm();

        // add form components
        panel.add(new JLabel("Id"), createContrains(0,0,1,1));
        panel.add(documentIdField, createContrains(1,0,1,1));

        panel.add(new JLabel("Title"), createContrains(0,1,1,1));
        panel.add(documentTitleField, createContrains(1,1,1,1));

        panel.add(new JLabel("Date"), createContrains(0,2,1,1));
        panel.add(documentDateField, createContrains(1,2,1,1));

        panel.add(new JLabel("Text"), createContrains(0,3,1,1));
        JScrollPane scrollPane = new JScrollPane(documentTextArea);
        panel.add(scrollPane, createContrains(0,4,2,2));

        indexBtn = new JButton(new AbstractAction("Index") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String err = "";
                if (documentIdField.getText().isEmpty()) {
                    err = "Id field is empty!";
                } else if (documentTitleField.getText().isEmpty()) {
                    err = "Title field is empty!";
                } else if (documentDateField.getText().isEmpty()) {
                    err = "Date field is empty!";
                } else if (documentTextArea.getText().isEmpty()) {
                    err = "Text field is empty!";
                }

                if (!err.isEmpty()) {
                    showErrorMessage(err);
                    return;
                }

                DocumentNew d = new DocumentNew(documentTextArea.getText(), documentIdField.getText());
                try {
                    d.setDate(new SimpleDateFormat(DATE_FORMAT).parse(documentDateField.getText()));
                } catch (ParseException e1) {
                    showErrorMessage("Error while parsing date in date field: "+e1.getMessage());
                    return;
                }
                d.setTitle(documentTitleField.getText());
                try {
                    Main.indexDocument(d);
                } catch (Exception ex) {
                    showErrorMessage("Error while indexing document: "+ex.getMessage());
                }
                setIndexedDocumentsCount(Main.getIndex().getDocumentCount());
                resetNewDocForm();
            }
        });
        panel.add(indexBtn, createContrains(1,6,1,1));

        return panel;
    }

    private GridBagConstraints createContrains(int col, int row, int rowspan, int colspan) {
        return new GridBagConstraints(
                col,row,
                rowspan,colspan,
                0.5,0.5,
                GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0,0,0,0),
                0,0
        );
    }

    private JButton createSaveIndexBtn() {
        JButton btn = new JButton(new AbstractAction("Save index") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                int returnVal = fc.showSaveDialog(IndexManagementPanel.this.getParent());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        Main.saveIndexToFile(file.getPath());
                    } catch (Exception ex) {
                        IndexManagementPanel.this.showErrorMessage("Unexpected error occurred while saving index: " + ex.getMessage());
                    }
                }
            }
        });
        btn.setAlignmentX(0.5f);

        return btn;
    }

    private JButton createLoadIndexBtn() {
        JButton btn = new JButton(new AbstractAction("Load index") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(IndexManagementPanel.this.getParent());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (!file.exists()) {
                        return;
                    }
                    try {
                        Main.loadIndexFromFile(file.getPath());
                        setIndexedDocumentsCount(Main.getIndex().getDocumentCount());
                    } catch (Exception ex) {
                        IndexManagementPanel.this.showErrorMessage("Unexpected error occurred while loading index: " + ex.getMessage());
                    }
                }
            }
        });
        btn.setAlignmentX(0.5f);

        return btn;
    }


    /**
     * Opens error dialog with error message and OK button.
     * @param errorMsg Error message to be displayed.
     */
    protected void showErrorMessage(String errorMsg) {
        JOptionPane.showMessageDialog(IndexManagementPanel.this.getParent(), errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
    }


    protected void resetNewDocForm() {
        documentIdField.setText("");
        documentTitleField.setText("");
        documentDateField.setText(new SimpleDateFormat(DATE_FORMAT).format(new java.util.Date()));
        documentTextArea.setText("");
    }

    public void setIndexedDocumentsCount(int count) {
        indexedDocumentsCountDisplay.setText(Integer.toString(count));
    }
}
