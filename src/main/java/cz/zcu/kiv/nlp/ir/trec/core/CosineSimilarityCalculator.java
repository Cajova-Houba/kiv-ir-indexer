package cz.zcu.kiv.nlp.ir.trec.core;

import java.util.HashMap;
import java.util.Map;

public class CosineSimilarityCalculator implements SimilarityCalculator {

    /**
     * Inverted index which maps words to its occurrences in documents.
     * word -> (doc1Id -> # of occurrences, doc2Id -> # of occurrences, ...).
     */
    private Map<String, Map<String, Integer>> invertedIndex;

    /**
     * Number of document indexes.
     */
    private int documentCount;

    private InvertedIndex iinvertedIndex;

    public CosineSimilarityCalculator(Map<String, Map<String, Integer>> invertedIndex, int documentCount) {
        this.invertedIndex = invertedIndex;
        this.documentCount = documentCount;
    }

    public CosineSimilarityCalculator(InvertedIndex iinvertedIndex) {
        this.iinvertedIndex = iinvertedIndex;
    }

    public double calculateScore(String[] query, String documentId) {
        double cosSim = 0;
        double cosNormConstant = 0;

        // un-weighted term frequencies for query-query and query-document
        Map<String, Integer> queryTermF = new HashMap<String, Integer>();
        Map<String, Double> docTermF = new HashMap<String, Double>();
        for(String token : query) {
            if (queryTermF.containsKey(token)) {
                queryTermF.put(token, queryTermF.get(token) +1);
            } else {
                queryTermF.put(token, 1);
            }

            if (!docTermF.containsKey(token)) {
                double termDocTf = ltf(token,documentId);
                docTermF.put(token, termDocTf);
                cosNormConstant += termDocTf*termDocTf;
            }
        }

        // cos normalization for document weights
        if (cosNormConstant == 0) {
            cosNormConstant = 1;
        } else {
            cosNormConstant = 1/Math.sqrt(cosNormConstant);
        }

        for(String token : queryTermF.keySet()) {
            // weighted query term frequency
            double qtf = (1 + Math.log(queryTermF.get(token)))*idf(token);

            // weighted and normalized document frequency
            double dtf = docTermF.get(token) * cosNormConstant;

            // cos normalization
            cosSim += dtf*qtf;
        }

        return cosSim;
    }

    public int df(String term) {
        return iinvertedIndex.documentFrequency(term);
    }

    public double idf(String term) {
        double idf = df(term);
        if (idf != 0)  {
            idf = Math.log(getDocumentCount() / idf);
        }

        return idf;
    }

    public double ntf(String term, String documentId) {
        return iinvertedIndex.getTermFrequency(term, documentId);
    }

    public double ltf(String term, String documentId){
        double ltf = ntf(term, documentId);
        if (ltf != 0) {
            ltf = 1 + Math.log(ltf);
        }

        return ltf;
    }

    public int getDocumentCount() {
        return iinvertedIndex.getDocumentCount();
    }
}
