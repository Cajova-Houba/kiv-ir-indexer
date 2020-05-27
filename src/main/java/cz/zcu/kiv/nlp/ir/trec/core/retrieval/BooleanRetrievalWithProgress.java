package cz.zcu.kiv.nlp.ir.trec.core.retrieval;

import cz.zcu.kiv.nlp.ir.trec.Configuration;
import cz.zcu.kiv.nlp.ir.trec.core.Posting;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;

import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Boolean retrieval with trackable progress.
 */
public class BooleanRetrievalWithProgress implements RetrievalWithProgress {

    /**
     * Postings to process.
     */
    private List<Posting> postings;
    private Iterator<Posting> postingIterator;

    /**
     * Queue to store results into.
     */
    private PriorityQueue<Result> resultQueue;

    private double progress;
    private double progressStep;

    public BooleanRetrievalWithProgress(List<Posting> postings, PriorityQueue<Result> resultQueue) {
        this.postings = postings;
        this.resultQueue = resultQueue;

        postingIterator = postings.iterator();
        progress = 0;
        if (postings.size() == 0) {
            progressStep = 0;
        } else {
            progressStep = ((double) Configuration.getMaxProgress()) / postings.size();
        }
    }

    @Override
    public void oneStep() {
        if (done()) {
            return;
        }

        Posting p = postingIterator.next();

        ResultImpl r = new ResultImpl();
        r.setDocumentID(p.getDocumentId());
        r.setScore(1f);
        resultQueue.add(r);
        progress += progressStep;
    }

    @Override
    public boolean done() {
        return !postingIterator.hasNext();
    }

    @Override
    public int getProgress() {
        return (int)progress;
    }

    @Override
    public PriorityQueue<Result> getResultQueue() {
        return resultQueue;
    }
}
