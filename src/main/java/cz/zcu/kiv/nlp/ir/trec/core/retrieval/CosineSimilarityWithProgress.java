package cz.zcu.kiv.nlp.ir.trec.core.retrieval;

import cz.zcu.kiv.nlp.ir.trec.Configuration;
import cz.zcu.kiv.nlp.ir.trec.core.Posting;
import cz.zcu.kiv.nlp.ir.trec.core.SimilarityCalculator;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;

import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Allows to calculate similarity step-by-step so that progress can be tracked.
 *
 * New instance should be created every time similarity is to eb calculated.
 */
public class CosineSimilarityWithProgress implements RetrievalWithProgress {

    /**
     * Postings to process.
     */
    private List<Posting> postings;
    private Iterator<Posting> postingIterator;

    /**
     * Queue to store results into.
     */
    private PriorityQueue<Result> resultQueue;

    /**
     * Similarity calculator to be used.
     */
    private SimilarityCalculator similarityCalculator;

    private double progress;
    private double progressStep;


    public CosineSimilarityWithProgress(List<Posting> postings, PriorityQueue<Result> resultQueue, SimilarityCalculator similarityCalculator) {
        this.postings = postings;
        this.resultQueue = resultQueue;
        this.similarityCalculator = similarityCalculator;

        postingIterator = postings.iterator();
        progress = 0;
        if (postings.size() == 0) {
            progressStep = 0;
        } else {
            progressStep = ((double) Configuration.getMaxProgress()) / postings.size();
        }
    }

    /**
     * Performs one step of similarity calculation
     */
    public void oneStep() {
        if (done()) {
            return;
        }

        Posting p = postingIterator.next();
        double score = similarityCalculator.calculateScore(p.getDocumentId());
        ResultImpl r = new ResultImpl();
        r.setDocumentID(p.getDocumentId());
        r.setScore((float)score);
        resultQueue.add(r);
        progress += progressStep;
    }

    /**
     * Returns true when all of the postings were processed.
     * @return
     */
    public boolean done() {
        return !postingIterator.hasNext();
    }

    /**
     * Progress of similarity calculation.
     * @return Number in range [0;100].
     */
    public int getProgress() {
        return (int)progress;
    }

    public PriorityQueue<Result> getResultQueue() {
        return resultQueue;
    }
}
