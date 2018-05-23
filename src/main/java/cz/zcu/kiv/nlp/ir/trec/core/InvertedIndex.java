package cz.zcu.kiv.nlp.ir.trec.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for inverted index.
 */
public class InvertedIndex {

    /**
     * Index which maps terms to postings.
     * Each posting is mapped to document id for easier retrieval.
     */
    private Map<String, Map<String,Posting>> invertedIndex;

    /**
     * Ids of indexed documents.
     */
    private Set<String> indexedDocuments;

    public InvertedIndex() {
        invertedIndex = new HashMap<String, Map<String,Posting>>();
        indexedDocuments = new HashSet<String>();
    }

    /**
     * Adds document to index.
     * @param tokens Text of the document to be indexed represented as preprocessed tokens.
     * @param documentId Id of the document to be indexed.
     */
    public void indexDocument(String[] tokens, String documentId) {
        indexedDocuments.add(documentId);

        for(String token : tokens) {
            // create new set for term postings
            if (!invertedIndex.containsKey(token)) {
                invertedIndex.put(token, new HashMap<String, Posting>());
                invertedIndex.get(token).put(documentId, new Posting(documentId));

                // add term occurrence for another doc
            } else if (!invertedIndex.get(token).containsKey(documentId)) {
                invertedIndex.get(token).put(documentId, new Posting(documentId));

                // increment term occurrence
            } else {
                invertedIndex.get(token).get(documentId).incrementTermFrequency();
            }
        }
    }

    /**
     * Returns number of indexed documents.
     * @return
     */
    public int getDocumentCount() {
        return indexedDocuments.size();
    }

    /**
     * Get document frequency of one term.
     * @param term Term.
     * @return Document frequency of one term.
     */
    public int documentFrequency(String term) {
        if (!invertedIndex.containsKey(term)) {
            return 0;
        }

        return invertedIndex.get(term).size();
    }

    /**
     * Get term frequency of term in document.
     * @param term Term.
     * @param documentId Document.
     * @return Term frequency.
     */
    public int getTermFrequency(String term, String documentId) {
        if (!invertedIndex.containsKey(term) || !invertedIndex.get(term).containsKey(documentId)) {
            return 0;
        }
        return invertedIndex.get(term).get(documentId).getTermFrequency();
    }

    public Set<String> getIndexedDocuments() {
        return indexedDocuments;
    }
}
