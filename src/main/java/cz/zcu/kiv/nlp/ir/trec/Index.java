package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Stemmer;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Tokenizer;

import java.util.*;

/**
 * @author tigi
 */

public class Index implements Indexer, Searcher {

    /**
     * Inverted index which maps words to its occurrences in documents.
     * word -> (doc1Id -> # of occurrences, doc2Id -> # of occurrences, ...).
     */
    private Map<String, Map<String, Integer>> invertedIndex;

    /**
     * Documents mapped by their ids.
     */
    // maybe not needed
    private Map<String, Document> documentMap;

    /**
     * Number of document indexes.
     */
    private int documentCount;

    /**
     * Tokenizer to be used to tokenize input text.
     */
    private Tokenizer tokenizer;

    /**
     * Stemmer to e used to stem tokenized text (and tokens in search query).
     */
    private Stemmer stemmer;

    /**
     * Stopwords. Only lowercase without accents should be used. Tokens in those set will not be indexed.
     */
    private Set<String> stopwords;

    public Index(Tokenizer tokenizer, Stemmer stemmer, Set<String> stopwords) {
        this.tokenizer = tokenizer;
        this.stemmer = stemmer;
        this.stopwords = stopwords;
        invertedIndex = new HashMap<String, Map<String, Integer>>();
        documentMap = new HashMap<String, Document>();
    }

    public void index(List<Document> documents) {
        documentCount = documents.size();
        for(Document d : documents) {
            String dId = d.getId();
            String dText = d.getText();

            // tokenize text and index document
            List<String> tokens = tokenizer.tokenize(dText);
            for(String token : tokens) {
                // skip stopwords
                if (isStopword(token)) {
                    continue;
                }
                String stem = stemmer.stem(token);

                // create new set for term postings
                if (!invertedIndex.containsKey(stem)) {
                    invertedIndex.put(stem, new HashMap<String, Integer>());
                    invertedIndex.get(stem).put(dId, 1);

                // add term occurrence for another doc
                } else if (!invertedIndex.get(stem).containsKey(dId)) {
                    invertedIndex.get(stem).put(dId, 1);

                // increment term occurrence
                } else {
                    int occurrence = invertedIndex.get(stem).get(dId);
                    invertedIndex.get(stem).put(dId, occurrence+1);
                }
            }
        }
    }

    public List<Result> search(String query) {
        //  todo implement
        // tokenize query
        List<String> tokenizedQuery = tokenizer.tokenize(query);

        // find matches in inverted index
        List<Result> results = new ArrayList<Result>();
        for(String token : tokenizedQuery) {
            String stemToken = stemmer.stem(token);
            if (invertedIndex.containsKey(stemToken)) {
                for (String dId : invertedIndex.get(stemToken).keySet()) {
                    ResultImpl r = new ResultImpl();
                    r.setDocumentID(dId);
                    r.setRank(1);
                    r.setScore((float)tfIdf(stemToken, dId));
                    results.add(r);
                }
            }
        }
        return results;
    }

    /**
     * Returns true if the token is a stopword.
     * @param token Token.
     * @return True if the token is a stopword.
     */
    private boolean isStopword(String token) {
        if (stopwords == null || stopwords.isEmpty()) {
            return false;
        }

        return stopwords.contains(token);
    }

    /**
     * Calculates the cosine similarity between query and document.
     *
     * @param queryTfIdf Array which contains td-idf values calculated for document and each term in query.
     * @param documentId Id of document.
     * @return Cosine similarity.
     */
    private double cosineSimilarity(double[] queryTfIdf, String documentId) {
        return 0.0;
    }

    /**
     * Calculates TF-IDF rank for one term and one document.
     *
     * @param term Term (part of a query).
     * @param documentId Id of a document.
     * @return Calculated TF-IDF.
     */
    private double tfIdf(String term, String documentId) {
        if (!invertedIndex.containsKey(term)) {
            return 0;
        }
        double tf = 1 + Math.log(invertedIndex.get(term).get(documentId));
        double idf = Math.log(documentCount / invertedIndex.get(term).size());
        return tf*idf;
    }

    /**
     * One item in inverted index postings. Contains document id and number of term occurances.
     */
    private class IndexItem {

        private String documentId;
        private int termOccurrence;

        public IndexItem(String documentId) {
            this.documentId = documentId;
            termOccurrence = 1;
        }

        /**
         * Increments termOccurrence by 1.
         */
        public void increaseOccurance() {
            termOccurrence++;
        }

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public int getTermOccurances() {
            return termOccurrence;
        }

        public void setTermOccurances(int termOccurrence) {
            this.termOccurrence = termOccurrence;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IndexItem indexItem = (IndexItem) o;

            return documentId != null ? documentId.equals(indexItem.documentId) : indexItem.documentId == null;
        }

        @Override
        public int hashCode() {
            return documentId != null ? documentId.hashCode() : 0;
        }
    }
}
