package cz.zcu.kiv.nlp.ir.trec.core;

import java.util.HashMap;
import java.util.Map;

public class CosineSimilarityCalculator implements SimilarityCalculator {

    private InvertedIndex invertedIndex;

    private String[] query;

    private Map<String, Double> queryTfIdf = new HashMap<>();

    public CosineSimilarityCalculator(InvertedIndex invertedIndex, String[] query) {
        this.invertedIndex = invertedIndex;
        this.query = query;

        calculateQueryTFIDF();
    }

    private void calculateQueryTFIDF() {
        Map<String, Integer> queryTermF = new HashMap<>();
        for(String token : query) {
            if (queryTermF.containsKey(token)) {
                queryTermF.put(token, queryTermF.get(token) +1);
            } else {
                queryTermF.put(token, 1);
            }
        }

        for (String token : queryTermF.keySet()) {
            double termIdf = invertedIndex.idf(token);

            // if idf for given term is = 0 it means it's not in the index
            // skip it
            if (Math.abs(termIdf - 0) < 0.01) {
                continue;
            }
            queryTfIdf.put(token, (1 + Math.log10(queryTermF.get(token)))*termIdf);
        }
        normalizeVector(queryTfIdf);

    }

    public double calculateScore(String documentId) {
        double cosSim = 0;

        double tfIdfNorm = invertedIndex.getTfIdfNormForDocument(documentId);
        for(String token : queryTfIdf.keySet()) {
            // relative value, the query term TF-IDF is 1
            cosSim += invertedIndex.getTfIdfOfTermInDocument(token, documentId);
        }

        // query tf-idf vector is already normalized so no need to divide by 1
        return cosSim / tfIdfNorm;
    }

    /**
     * Normalizes the Double part of given map.
     * @param vec
     */
    private void normalizeVector(Map<String, Double> vec) {
        double sqrSum = 0;
        for(Double item : vec.values()) {
            sqrSum += item*item;
        }
        sqrSum = Math.sqrt(sqrSum);

        for(String key : vec.keySet()) {
            vec.put(key, vec.get(key) / sqrSum);
        }
    }
}
