package cz.zcu.kiv.nlp.ir.trec.gui.indexmgmt.actions;

import cz.zcu.kiv.nlp.ir.trec.Configuration;
import cz.zcu.kiv.nlp.ir.trec.Main;
import cz.zcu.kiv.nlp.ir.trec.data.Document;

import javax.swing.*;
import java.util.List;

/**
 * Background worker for indexing chunks of documents. Progress is tracked via progress bar.
 */
public class IndexDocumentsFromFileTask extends SwingWorker<Void, Integer> {

    private List<Document> documentsToIndex;
    private JProgressBar progressBar;

    public IndexDocumentsFromFileTask(List<Document> documentsToIndex, JProgressBar progressBar) {
        this.documentsToIndex = documentsToIndex;
        this.progressBar = progressBar;
    }

    @Override
    protected void process(List<Integer> chunks) {
        int i = chunks.get(chunks.size()-1);
        progressBar.setValue(i);
    }

    @Override
    protected Void doInBackground() throws Exception {
        double progress = 0;
        double progressStep = Configuration.getMaxProgress() / (double)documentsToIndex.size();

        for(Document d : documentsToIndex) {
            Main.indexDocument(d);
            progress += progressStep;
            publish((int)progress);
        }

        return null;
    }
}
