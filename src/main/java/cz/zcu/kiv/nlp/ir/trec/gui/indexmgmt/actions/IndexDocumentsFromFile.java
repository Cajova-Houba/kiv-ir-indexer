package cz.zcu.kiv.nlp.ir.trec.gui.indexmgmt.actions;

import cz.zcu.kiv.nlp.ir.trec.Main;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.RPolDocumentReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

/**
 * Action for reading documents from file and indexing them.
 */
public abstract class IndexDocumentsFromFile extends AbstractAction {

    private static Logger log = LoggerFactory.getLogger(IndexDocumentsFromFile.class);

    private Component dialogParent;

    public IndexDocumentsFromFile(String name, Component dialogComponents) {
        super(name);
        this.dialogParent = dialogComponents;
    }

    public abstract void onError(String error);

    public abstract void onIndexingFinished();

    @Override
    public void actionPerformed(ActionEvent e) {
        log.info("Indexing documents from file, waiting for user to choose it ...");

        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(dialogParent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            log.debug("Loading documents from file: {}", file.getPath());

            RPolDocumentReader reader = new RPolDocumentReader();


            try {
                log.debug("Reading and parsing file.");
                List<Document> documents = reader.readFile(file);
                log.debug("Done.");

                log.debug("Indexing files.");
                Main.indexDocuments(documents);
                log.debug("Done");
            } catch (Exception ex) {
                log.error("Exception while parsing data: ", ex);
                onError("Error while parsing data: "+ex.getMessage());
                return;
            }


        } else {
            log.info("User has cancelled the action.");
        }
        log.info("Done.");

        onIndexingFinished();
    }
}
