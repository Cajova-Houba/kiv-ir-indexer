package cz.zcu.kiv.nlp.ir.trec.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosineSimilarityCalculator implements SimilarityCalculator {

    private InvertedIndex invertedIndex;

    public CosineSimilarityCalculator(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public double calculateScore(String[] query, String documentId) {
        double cosSim = 0;

        // un-weighted term frequencies for query-query and query-document
        Map<String, Integer> queryTermF = new HashMap<>();

        // calculate term frequencies for query
        for(String token : query) {
            if (queryTermF.containsKey(token)) {
                queryTermF.put(token, queryTermF.get(token) +1);
            } else {
                queryTermF.put(token, 1);
            }
        }

        // calculate tf-idf for document
        Map<String, Double> documentTfIdf = calculateDocumentTfIdf(documentId);

        // calculate and normalize tf-idf for query
        Map<String, Double> queryTfIdf = new HashMap<>();
        for (String token : queryTermF.keySet()) {
            double termIdf = idf(token);

            // if idf for given term is = 0 it means it's not in the index
            // skip it
            if (Math.abs(termIdf - 0) < 0.01) {
                continue;
            }
            queryTfIdf.put(token, (1 + Math.log10(queryTermF.get(token)))*termIdf);
        }
        normalizeVector(queryTfIdf);

        // calculate cosine similarity
        for(String token : queryTfIdf.keySet()) {
            if (documentTfIdf.containsKey(token)) {
                cosSim += documentTfIdf.get(token) * queryTfIdf.get(token);
            }
        }

        return cosSim;
    }

    /**
     * Calculates TF-IDF for all terms in document.
     * @param documentId
     * @return Map of term -> normalized TF-IDF
     */
    private Map<String, Double> calculateDocumentTfIdf(String documentId) {
        List<String> terms = invertedIndex.getTermsFormDocument(documentId);
        Map<String, Double> tfIdfMap = new HashMap<>();
        for(String term : terms) {
            double tf = ltf(term, documentId);
            double tfIdf = tf * idf(term);
            tfIdfMap.put(term, tfIdf);
        }

        normalizeVector(tfIdfMap);

        return tfIdfMap;
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

    public int df(String term) {
        return invertedIndex.documentFrequency(term);
    }

    public double idf(String term) {
        double idf = df(term);
        if (idf != 0)  {
            idf = Math.log10(getDocumentCount() / idf);
        }

        return idf;
    }

    public double ntf(String term, String documentId) {
        return invertedIndex.getTermFrequency(term, documentId);
    }

    public double ltf(String term, String documentId){
        double ltf = ntf(term, documentId);
        if (ltf != 0) {
            ltf = 1 + Math.log10(ltf);
        }

        return ltf;
    }

    public int getDocumentCount() {
        return invertedIndex.getDocumentCount();
    }
}
