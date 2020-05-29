package cz.zcu.kiv.nlp.ir.trec.core;

/**
 * Base interface for similarity calculators.
 */
public interface SimilarityCalculator {

    /**
     * Calculates similarity between query and document.
     *
     * @param documentId Id of document.
     * @return Similarity.
     */
    double calculateScore(String documentId);
}
