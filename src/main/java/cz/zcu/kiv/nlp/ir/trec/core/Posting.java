package cz.zcu.kiv.nlp.ir.trec.core;

import java.io.Serializable;

/**
 * One item in inverted index posting.
 */
public class Posting implements Serializable{

    /**
     * Id of document term occurs in.
     */
    private String documentId;

    /**
     * Number of occurrences of term in document.
     */
    private int termFrequency;

    /**
     * TF-IDF of this term in this document.
     */
    private double tfIdf;

    public Posting(String documentId) {
        this.documentId = documentId;
        this.termFrequency = 1;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public void setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
    }

    /**
     * Increases term frequency by 1.
     */
    public void incrementTermFrequency() {
        termFrequency++;
    }

    /**
     * Returns hash of document id. If the id is null, 0 is returned.
     * Use this to sort postings by document ids even if those aren't integers.
     *
     * @return Hash of document id.
     */
    public int documentIdHash() {
        if (documentId == null)  { return 0; }
        else { return documentId.hashCode(); }
    }

    /**
     * Re-calculates TF-IDF of this posting. Assumes termFrequency is set correctly.
     *
     * @param termIdf IDF of term in this posting.
     */
    public void recalculateTfIdf(double termIdf) {
        tfIdf = termIdf * (1 + Math.log10(termFrequency));
    }

    public double getTfIdf() {
        return tfIdf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Posting posting = (Posting) o;

        return documentId != null ? documentId.equals(posting.documentId) : posting.documentId == null;
    }

    @Override
    public int hashCode() {
        return documentId != null ? documentId.hashCode() : 0;
    }
}
