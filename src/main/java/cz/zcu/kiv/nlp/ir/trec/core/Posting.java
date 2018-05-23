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
