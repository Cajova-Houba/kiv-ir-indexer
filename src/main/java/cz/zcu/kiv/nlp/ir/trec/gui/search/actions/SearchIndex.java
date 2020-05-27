package cz.zcu.kiv.nlp.ir.trec.gui.search.actions;

import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.gui.search.SearchPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Search index action.
 */
public abstract class SearchIndex extends AbstractAction {

    private static Logger log = LoggerFactory.getLogger(SearchIndex.class);

    private JProgressBar progressBar;
    private SearchPanel parentComponent;

    private SearchIndexTask task;

    public SearchIndex(String name, JProgressBar progressBar, SearchPanel parentComponent) {
        super(name);
        this.progressBar = progressBar;
        this.parentComponent = parentComponent;
    }

    public abstract void onBeforeSearch();

    public abstract void onError(String message);

    public abstract void onSearchFinished(List<Result> results);

    @Override
    public void actionPerformed(ActionEvent e) {
        onBeforeSearch();

        String query = parentComponent.getQuery();
        log.info("Executing query \"{}\"", query);
        if (query.isEmpty()) {
            log.warn("Query is empty.");
            onError("No query!");
            return;
        }

        try {
            log.debug("Starting background task.");

            task = new SearchIndexTask(query, parentComponent.getTopK(), parentComponent.getSearchMode(), progressBar) {
                @Override
                protected void done() {
                    try {
                        onSearchFinished(get());
                    } catch (Exception e1) {
                        log.error("Unexpected exception: ", e1);
                        e1.printStackTrace();
                        onError("Unexpected error while performing search: "+e1.getMessage());
                    }
                }
            };
            task.execute();
            log.info("Waiting for search to finish.");

        } catch (Exception ex) {
            log.error("Unexpected error while executing the query: ", ex);
            onError("Unexpected exception occurred while performing search: "+ex.getMessage());
        }
    }
}
