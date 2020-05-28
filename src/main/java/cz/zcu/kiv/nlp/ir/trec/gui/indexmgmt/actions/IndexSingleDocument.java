package cz.zcu.kiv.nlp.ir.trec.gui.indexmgmt.actions;

import cz.zcu.kiv.nlp.ir.trec.Configuration;
import cz.zcu.kiv.nlp.ir.trec.Main;
import cz.zcu.kiv.nlp.ir.trec.data.DocumentNew;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Load document from GUI and index it.
 */
public abstract class IndexSingleDocument extends AbstractAction {

    private static Logger log = LoggerFactory.getLogger(IndexSingleDocument.class);

    private JTextField documentIdField;
    private JTextField documentTitleField;
    private JTextArea documentTextArea;
    private JFormattedTextField documentDateField;

    public IndexSingleDocument(String name, JTextField documentIdField, JTextField documentTitleField, JTextArea documentTextArea, JFormattedTextField documentDateField) {
        super(name);
        this.documentIdField = documentIdField;
        this.documentTitleField = documentTitleField;
        this.documentTextArea = documentTextArea;
        this.documentDateField = documentDateField;
    }

    /**
     * Called when error occurs during action.
     * @param error String error to be displayed to user.
     */
    public abstract void onError(String error);

    /**
     * Callback used to re-set components.
     */
    public abstract void onIndexingFinished();

    public abstract void onBeforeIndex();

    @Override
    public void actionPerformed(ActionEvent e) {
        log.info("Indexing document.");

        onBeforeIndex();

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
            log.warn("Error while gathering document data: {}.", err);
            onError(err);
            return;
        }

        log.debug("Creating document.");
        DocumentNew d = new DocumentNew(documentTextArea.getText(), documentIdField.getText());
        try {
            d.setDate(new SimpleDateFormat(Configuration.getDateFormat()).parse(documentDateField.getText()));
        } catch (ParseException e1) {
            log.error("Error while parsing date: ", e1);
            onError("Error while parsing date in date field: "+e1.getMessage());
            return;
        }
        d.setTitle(documentTitleField.getText());
        try {
            log.debug("Indexing document");
            Main.indexDocument(d);
            Main.recalculateIndex();
            log.debug("Done.");
        } catch (Exception ex) {
            log.error("Unexpected exception while indexing document: ", ex);
            onError("Error while indexing document: "+ex.getMessage());
        }

        onIndexingFinished();
    }


}
