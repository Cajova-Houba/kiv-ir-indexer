package cz.zcu.kiv.nlp.ir.trec.gui.indexmgmt.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action for reading documents from file and indexing them.
 */
public class IndexDocumentsFromFile extends AbstractAction {

    private static Logger log = LoggerFactory.getLogger(IndexDocumentsFromFile.class);

    public IndexDocumentsFromFile(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.info("Indexing documents from file, waiting for user to choose it ...");
        log.info("Done.");
    }
}
