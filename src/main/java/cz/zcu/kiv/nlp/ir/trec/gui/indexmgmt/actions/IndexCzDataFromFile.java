package cz.zcu.kiv.nlp.ir.trec.gui.indexmgmt.actions;

import cz.zcu.kiv.nlp.ir.trec.SerializedDataHelper;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.RPolDocumentReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public abstract class IndexCzDataFromFile extends AbstractAction {

    private static Logger log = LoggerFactory.getLogger(IndexDocumentsFromFile.class);

    private Component dialogParent;
    private JProgressBar progressBar;

    public IndexCzDataFromFile(String name, Component dialogComponents, JProgressBar indexFromDocProgressBar) {
        super(name);
        this.dialogParent = dialogComponents;
        progressBar = indexFromDocProgressBar;
    }

    public abstract void onBeforeIndex();

    public abstract void onError(String error);

    public abstract void onIndexingFinished();

    @Override
    public void actionPerformed(ActionEvent e) {
        log.info("Indexing documents from file, waiting for user to choose it ...");

        onBeforeIndex();

        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(dialogParent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            log.debug("Loading documents from file: {}", file.getPath());

            RPolDocumentReader reader = new RPolDocumentReader();


            try {
                log.debug("Reading and parsing file.");
                List<Document> documents = SerializedDataHelper.loadDocument(file);
                log.debug("Done.");

                log.debug("Creating task for indexing documents.");
                IndexDocumentsFromFileTask task = new IndexDocumentsFromFileTask(documents, progressBar) {
                    @Override
                    protected void done() {
                        onIndexingFinished();
                        super.done();
                    }
                };
                task.execute();
                log.debug("Waiting for task to finish.");
            } catch (Exception ex) {
                log.error("Exception while parsing data: ", ex);
                onError("Error while parsing data: "+ex.getMessage());
            }
        } else {
            log.info("User has cancelled the action.");
            onIndexingFinished();
        }
    }
}
