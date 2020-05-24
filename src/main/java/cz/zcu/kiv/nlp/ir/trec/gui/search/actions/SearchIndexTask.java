package cz.zcu.kiv.nlp.ir.trec.gui.search.actions;

import cz.zcu.kiv.nlp.ir.trec.Configuration;
import cz.zcu.kiv.nlp.ir.trec.Main;
import cz.zcu.kiv.nlp.ir.trec.core.SimilarityCalculatorWithProgress;

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

    public SearchIndexTask(String query, int topK, JProgressBar progressBar) {
        this.query = query;
        this.topK = topK;
        this.progressBar = progressBar;
    }

    @Override
    protected void process(List<Integer> chunks) {
        int i = chunks.get(chunks.size()-1);
        progressBar.setValue(i);
    }

    @Override
    protected List doInBackground() throws Exception {
        SimilarityCalculatorWithProgress calculatorWithProgress = Main.searchWithProgress(query, topK);
        if (calculatorWithProgress == null) {
            publish(Configuration.getMaxProgress());
            return Collections.emptyList();
        }

        while(!calculatorWithProgress.done()) {
            calculatorWithProgress.oneStep();
            publish(calculatorWithProgress.getProgress());
        }

        publish(Configuration.getMaxProgress());

        return Main.extractTopKResults(calculatorWithProgress.getResultQueue(), topK);
    }
}
