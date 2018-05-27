package cz.zcu.kiv.nlp.ir.trec.core;

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
     * Calculates 'no document frequency' of term and always returns 1.
     * @return 1.
     */
    int ndf();

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
     * Calculates prob idf for one term.
     * Formula is: pdf = max(0, log((N - df)/df))
     *
     * @param term Term.
     * @return PDF of term.
     */
    double pdf(String term);

    /**
     * Returns natural term frequency of term in document.
     *
     * @param term Term.
     * @param documentId Id of document.
     * @return Unweighted term frequency.
     */
    double ntf(String term, String documentId);

    /**
     * Calculates logarithm term frequency of term in document.
     * Formula is: wtf = 1 + log(tf)
     *
     * @param term Term.
     * @param documentId Document id.
     * @return Weighted term frequency.
     */
    double ltf(String term, String documentId);

    /**
     * Calculates boolean term frequency of term in document.
     * Formula is: atf = ntf > 0 ? 1 : 0.
     *
     * @param term Term.
     * @param documentId Document id.
     * @return Boolean term frequency.
     */
    double btf(String term, String documentId);

    /**
     * Calculates augmented term frequency of term in document.
     * Formula is: atf = 0.5 + ((0.5*ntf) / (max_t(tf,d))
     *
     * @param term
     * @param documentId
     * @return
     */
    double atf(String term, String documentId);

    /**
     * Returns no-normalization constant.
     * @return 1.
     */
    double nn();

    /**
     * Calculates cosine normalization constant.
     * Formula: 1/sqrt(sum(weighted-tf^2)).
     *
     * @param tfs Weighted tfs to be used for calculation.
     * @return Cosine normalization constant.
     */
    double cn(double[] tfs);

    /**
     * Calculates byte length normalisation constant.
     * Formula: 1/document_len_bytes
     *
     * @param documentId Id of document to be used for constant calculation
     * @return byte length normalization constant.
     */
    double bn(String documentId);

    /**
     * Returns number of documents in whole collection.
     * @return
     */
    int getDocumentCount();
}
