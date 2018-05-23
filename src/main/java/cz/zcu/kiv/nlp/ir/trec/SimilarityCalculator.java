package cz.zcu.kiv.nlp.ir.trec;

/**
 * Base interface for similarity calculators.
 */
public interface SimilarityCalculator {

    /**
     * Calculates similarity between query and document.
     *
     * @param query Query passed as array of tokens (preferably preprocessed).
     * @param documentId Id of document.
     * @return Similarity.
     */
    double calculateScore(String[] query, String documentId);

    /**
     * Calculates document frequency of term.
     *
     * @param term Term.
     * @return Document frequency.
     */
    int df(String term);

    /**
     * Calculates IDF for one term.
     *
     * @param term Term.
     * @return IDF of term.
     */
    double idf(String term);

    /**
     * Returns natural term frequency of term in document.
     *
     * @param term Term.
     * @param documentId Id of document.
     * @return Unweighted term frequency.
     */
    double ntf(String term, String documentId);

    /**
     * Calculates weighted term frequency of term in document.
     * Formula is: wtf = 1 + log(tf)
     *
     * @param term Term.
     * @param documentId Document id.
     * @return Weighted term frequency.
     */
    double ltf(String term, String documentId);

    /**
     * Returns number of documents in whole collection.
     * @return
     */
    int getDocumentCount();
}
