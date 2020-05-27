package cz.zcu.kiv.nlp.ir.trec.core.retrieval;

import cz.zcu.kiv.nlp.ir.trec.data.Result;

import java.util.PriorityQueue;

public interface RetrievalWithProgress {

    /**
     * One step of retrieval calculation.
     */
    void oneStep();

    /**
     * Returns true when all of the postings were processed.
     * @return
     */
    boolean done();

    /**
     * Progress of similarity calculation.
     * @return Number in range [0;100].
     */
    int getProgress();

    /**
     * Priority queue with results.
     * @return
     */
    PriorityQueue<Result> getResultQueue();
}
