package cz.zcu.kiv.nlp.ir.trec.gui.search.actions;

import cz.zcu.kiv.nlp.ir.trec.Configuration;
import cz.zcu.kiv.nlp.ir.trec.Main;
import cz.zcu.kiv.nlp.ir.trec.core.SearchMode;
import cz.zcu.kiv.nlp.ir.trec.core.retrieval.RetrievalWithProgress;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * Background task for searching index and updating progress bar.
 */
public class SearchIndexTask extends SwingWorker<List, Integer> {

    private String query;
    private int topK;
    private JProgressBar progressBar;
    private SearchMode searchMode;
    private int totalDocumentCount;

    public SearchIndexTask(String query, int topK, SearchMode searchMode, JProgressBar progressBar) {
        this.query = query;
        this.topK = topK;
        this.progressBar = progressBar;
        this.searchMode = searchMode;
        this.totalDocumentCount = 0;
    }

    @Override
    protected void process(List<Integer> chunks) {
        int i = chunks.get(chunks.size()-1);
        progressBar.setValue(i);
    }

    @Override
    protected List doInBackground() throws Exception {
        RetrievalWithProgress retrievalWithProgress = Main.searchWithProgress(query, searchMode, topK);
        if (retrievalWithProgress == null) {
            publish(Configuration.getMaxProgress());
            return Collections.emptyList();
        }

        while(!retrievalWithProgress.done()) {
            retrievalWithProgress.oneStep();
            publish(retrievalWithProgress.getProgress());
        }

        publish(Configuration.getMaxProgress());

        totalDocumentCount = retrievalWithProgress.getResultQueue().size();
        return Main.extractTopKResults(retrievalWithProgress.getResultQueue(), topK);
    }

    /**
     * Returns total number of found documents.
     * @return
     */
    public int getTotalDocumentCount() {
        return totalDocumentCount;
    }
}
