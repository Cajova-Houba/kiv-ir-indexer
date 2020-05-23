package cz.zcu.kiv.nlp.ir.trec.gui.indexmgmt;

import cz.zcu.kiv.nlp.ir.trec.Configuration;
import cz.zcu.kiv.nlp.ir.trec.Main;
import cz.zcu.kiv.nlp.ir.trec.gui.indexmgmt.actions.IndexDocumentsFromFile;
import cz.zcu.kiv.nlp.ir.trec.gui.indexmgmt.actions.IndexSingleDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Panel for managing search index.
 */
public class IndexManagementPanel extends JPanel {

    private static Logger log = LoggerFactory.getLogger(IndexManagementPanel.class);

    private JTextField documentIdField;
    private JTextField documentTitleField;
    private JTextArea documentTextArea;
    private JFormattedTextField documentDateField;
    private JLabel indexedDocumentsCountDisplay;
    private JButton indexBtn, indexDocumentsFromFile;

    public IndexManagementPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentY(Component.TOP_ALIGNMENT);
        setPreferredSize(new Dimension(300,800));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Index management"),
                BorderFactory.createEmptyBorder(10,10,10,10)
                ));

        addComponents();
    }

    private void addComponents() {
        add(createIndexedDocumentsNumDisplay());
        add(createIndexDocumentPanel());
        add(createIndexDataFromFilePanel());
        add(Box.createVerticalStrut(50));
        add(createSaveIndexBtn());
        add(createLoadIndexBtn());
        add(Box.createVerticalBox());
    }

    /**
     * Panel with components able to load file, parse documents and index them.
     * @return
     */
    private Component createIndexDataFromFilePanel() {
        JPanel panel = new JPanel();
        panel.setAlignmentX(0.5f);
        panel.setAlignmentY(Component.TOP_ALIGNMENT);
        panel.setMaximumSize(new Dimension(300,100));
        panel.setBorder(BorderFactory.createTitledBorder("Index documents from file"));

        indexDocumentsFromFile = new JButton(new IndexDocumentsFromFile("Choose the source file", getParent()) {
            @Override
            public void onError(String error) {
                showErrorMessage(error);
            }

            @Override
            public void onIndexingFinished() {
                setIndexedDocumentsCount(Main.getIndex().getDocumentCount());
            }
        });
        panel.add(indexDocumentsFromFile);

        return panel;
    }

    private JPanel createIndexedDocumentsNumDisplay() {
        JPanel panel = new JPanel();
        panel.setAlignmentX(0.5f);
        panel.setAlignmentY(Component.TOP_ALIGNMENT);
        panel.setSize(300,20);
        panel.setMaximumSize(new Dimension(300,20));
        indexedDocumentsCountDisplay = new JLabel("0");
        panel.add(new JLabel("# of indexed documents: "));
        panel.add(indexedDocumentsCountDisplay);
        return panel;
    }

    private JPanel createIndexDocumentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setSize(400,300);
        panel.setMaximumSize(new Dimension(300,300));
        panel.setAlignmentX(0.5f);
        panel.setAlignmentY(Component.TOP_ALIGNMENT);
        panel.setBorder(BorderFactory.createTitledBorder("Index new document"));

        // init components
        documentIdField = new JTextField(10);
        documentTextArea = new JTextArea(7,18);
        documentTitleField = new JTextField(10);
        documentDateField = new JFormattedTextField(new SimpleDateFormat(Configuration.getDateFormat()));
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

        indexBtn = new JButton(new IndexSingleDocument("Index", documentIdField, documentTitleField, documentTextArea, documentDateField) {
            @Override
            public void onError(String error) {
                showErrorMessage(error);
            }

            @Override
            public void onIndexingFinished() {
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
                GridBagConstraints.NORTH,
                GridBagConstraints.NONE,
                new Insets(0,0,0,0),
                0,0
        );
    }

    private JButton createSaveIndexBtn() {
        JButton btn = new JButton(new AbstractAction("Save index") {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("Saving index to file, waiting for user to choose it ...");

                final JFileChooser fc = new JFileChooser();
                int returnVal = fc.showSaveDialog(IndexManagementPanel.this.getParent());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    log.debug("Saving to file: {}", file.getPath());
                    try {
                        Main.saveIndexToFile(file.getPath());
                        log.info("Save successful.");
                    } catch (Exception ex){
                        log.error("Exception while saving index to file: ", ex);
                        IndexManagementPanel.this.showErrorMessage("Unexpected error occurred while saving index: " + ex.getMessage());
                    }
                } else {
                    log.info("User has cancelled the action.");
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
                log.info("Loading index from file, waiting for user to choose it ...");

                final JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(IndexManagementPanel.this.getParent());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    log.debug("File name: {}", file.getPath());
                    if (!file.exists()) {
                        log.warn("This file does not exist.");
                        return;
                    }
                    try {
                        Main.loadIndexFromFile(file.getPath());
                        setIndexedDocumentsCount(Main.getIndex().getDocumentCount());
                        log.info("Index successfully loaded from file.");
                    } catch (Exception ex) {
                        log.error("Exception while loading index from file: ", ex);
                        IndexManagementPanel.this.showErrorMessage("Unexpected error occurred while loading index: " + ex.getMessage());
                    }
                } else {
                    log.info("User has cancelled the action.");
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
        documentDateField.setText(new SimpleDateFormat(Configuration.getDateFormat()).format(new java.util.Date()));
        documentTextArea.setText("");
    }

    public void setIndexedDocumentsCount(int count) {
        indexedDocumentsCountDisplay.setText(Integer.toString(count));
    }
}
