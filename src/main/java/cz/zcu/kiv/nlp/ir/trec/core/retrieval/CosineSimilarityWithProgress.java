package cz.zcu.kiv.nlp.ir.trec.core.retrieval;

import cz.zcu.kiv.nlp.ir.trec.Configuration;
import cz.zcu.kiv.nlp.ir.trec.core.SimilarityCalculator;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Allows to calculate similarity step-by-step so that progress can be tracked.
 *
 * New instance should be created every time similarity is to eb calculated.
 */
public class CosineSimilarityWithProgress implements RetrievalWithProgress {

    /**
     * Ids of document to go through.
     */
    private Set<String> documentIds;
    private Iterator<String> documentIterator;

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


    public CosineSimilarityWithProgress(Set<String> documentIds, PriorityQueue<Result> resultQueue, SimilarityCalculator similarityCalculator) {
        this.documentIds = documentIds;
        this.resultQueue = resultQueue;
        this.similarityCalculator = similarityCalculator;

        documentIterator = documentIds.iterator();
        progress = 0;
        if (documentIds.size() == 0) {
            progressStep = 0;
        } else {
            progressStep = ((double) Configuration.getMaxProgress()) / documentIds.size();
        }
    }

    /**
     * Performs one step of similarity calculation
     */
    public void oneStep() {
        if (done()) {
            return;
        }

        String dId = documentIterator.next();
        double score = similarityCalculator.calculateScore(dId);
        ResultImpl r = new ResultImpl();
        r.setDocumentID(dId);
        r.setScore((float)score);
        resultQueue.add(r);
        progress += progressStep;
    }

    /**
     * Returns true when all of the postings were processed.
     * @return
     */
    public boolean done() {
        return !documentIterator.hasNext();
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
