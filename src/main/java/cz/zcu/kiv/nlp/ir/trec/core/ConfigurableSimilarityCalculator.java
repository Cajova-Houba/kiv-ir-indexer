package cz.zcu.kiv.nlp.ir.trec.core;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Similarity calculator which accepts configuration in ddd.qqq format.
 */
public class ConfigurableSimilarityCalculator implements SimilarityCalculator {

    private static final String CONF_FORMAT = "ddd.qqq";

    public static final char[] FIRST_LETTERS = new char[] {'n', 'l', 'a', 'b'};
    public static final char[] SECOND_LETTERS = new char[] {'n', 't', 'p'};
    public static final char[] THIRD_LETTERS = new char[] {'n', 'c', 'b'};

    /**
     * Checks configuration and returns true if the format is ok.
     * @param conf Configuration to be checked.
     * @return True if the configuration is ok.
     */
    public static boolean checkConfiguration(String conf) {
        if (conf == null || conf.isEmpty() || conf.length() != CONF_FORMAT.length()) {
            return false;
        }

        // check first letters of ddd and qqq
        boolean dOk = false, qOk = false;
        for(char c : FIRST_LETTERS) {
            if (!dOk && c == conf.charAt(0)) { dOk = true; }
            if (!qOk && c == conf.charAt(4)) { qOk = true; }
        }
        if (!dOk || !qOk) { return false; }

        // check second letters of ddd and qqq
        dOk = false; qOk = false;
        for(char c : SECOND_LETTERS) {
            if (!dOk && c == conf.charAt(1)) { dOk = true; }
            if (!qOk && c == conf.charAt(5)) { qOk = true; }
        }
        if (!dOk || !qOk) { return false; }


        // check third letters of ddd and qqq
        dOk = false; qOk = false;
        for(char c : THIRD_LETTERS) {
            if (!dOk && c == conf.charAt(2)) { dOk = true; }
            if (!qOk && c == conf.charAt(6)) { qOk = true; }
        }
        if (!dOk || !qOk) { return false; }

        return true;
    }

    /**
     * Configuration string. Must be in 'ddd.qqq' format.
     */
    private String configuration;

    /**
     * Inverted index used to calculate score.
     */
    private InvertedIndex invertedIndex;

    /**
     * Cache for atf. Maps document id to max TF.
     */
    private Map<String, Double> maxTfsByDocument;

    /**
     * Constructor.
     *
     * @param configuration String configuration. Must be in this format: ddd.qqq.
     *                      Possible values for the first letter: n,l,a,b.
     *                      Possible values fot the second letter: n,t,p
     *                      Possible values for the third letter: n,c,b
     *
     *                      Throws illegal argument exception if incorrect format is used.
     * @param invertedIndex Inverted index used to calculate score.
     */
    public ConfigurableSimilarityCalculator(String configuration, InvertedIndex invertedIndex) {
        if (!checkConfiguration(configuration)) {
            throw new IllegalArgumentException("Configuration '"+configuration+"' is not in correct format!");
        }
        this.configuration = configuration;
        this.invertedIndex = invertedIndex;
        maxTfsByDocument = new HashMap<>();
    }

    @Override
    public double calculateScore(String[] query, String documentId) {
        double similarity = 0;
        double dNormConst = 0;
        double qNormConst = 0;

        // un-weighted term frequencies for query-query and query-document
        Map<String, Integer> queryTermF = new HashMap<>();
        Map<String, Double> docTfs = new HashMap<>();
        for(String token : query) {
            if (queryTermF.containsKey(token)) {
                queryTermF.put(token, queryTermF.get(token) +1);
            } else {
                queryTermF.put(token, 1);
            }

            if (!docTfs.containsKey(token)) {
                docTfs.put(token, calculateTf(token, documentId));
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

        return similarity;
    }

    /**
     * Chooses right method to calculate tf for query.
     * @param unweightedTf Unweighted tf.
     * @return Weighted tf.
     */
    private double weightQueryTf(int unweightedTf) {
        switch (configuration.charAt(4)) {
            case 'l':
                return unweightedTf == 0 ? 1 : 1+Math.log(unweightedTf);
            case
        }
    }

    /**
     * Chooses right method to calculate TF for document and returns it.
     * @param term Term.
     * @param documentId Document id.
     * @return TF.
     */
    private double calculateTf(String term, String documentId) {
        switch (configuration.charAt(0)) {
            case 'n':
                return ntf(term, documentId);
            case 'l':
                return ltf(term, documentId);
            case 'a':
                return atf(term, documentId);
            case 'b':
                return btf(term, documentId);
            default:
                return 0;
        }
    }

    /**
     * Chooses right method to calculate DF for document and returns it.
     * @param term Term.
     * @return DF.
     */
    private double calculateDf(String term) {
        switch (configuration.charAt(1)) {
            case 'n':
                return ndf();
            case 't':
                return idf(term);
            case 'p':
                return pdf(term);
            default:
                return 0;
        }
    }


    @Override
    public int ndf() {
        return 1;
    }

    @Override
    public int df(String term) {
        return invertedIndex.documentFrequency(term);
    }

    @Override
    public double idf(String term) {
        double idf = df(term);
        if (idf != 0)  {
            idf = Math.log(getDocumentCount() / idf);
        }

        return idf;
    }

    @Override
    public double pdf(String term) {
        double df = df(term);
        if (df == 0 || df == getDocumentCount()) {
            return 0;
        }
        return Math.max(0, Math.log((getDocumentCount() - df)/ df));
    }

    @Override
    public double ntf(String term, String documentId) {
        return invertedIndex.getTermFrequency(term, documentId);
    }

    @Override
    public double ltf(String term, String documentId) {
        double ltf = ntf(term, documentId);
        if (ltf != 0) {
            ltf = 1 + Math.log(ltf);
        }

        return ltf;
    }

    @Override
    public double btf(String term, String documentId) {
        return ntf(term, documentId) > 0 ? 1 : 0;
    }

    private double atf

    @Override
    public double atf(String term, String documentId) {
        double maxTf = 0;

        // check if the max tf isn't cached already
        if (maxTfsByDocument.containsKey(documentId)) {
            maxTf = maxTfsByDocument.get(documentId);
        } else {
            String[] terms = invertedIndex.getTermsInDocument(documentId);
            for(String t : terms) {
                double tf = ntf(t, documentId);
                if (tf > maxTf) {
                    maxTf = tf;
                }
            }
            maxTfsByDocument.put(documentId, maxTf);
        }

        if (maxTf == 0) {
            return 0.5;
        }
        double tf = ntf(term, documentId);

        return 0.5 + ((0.5*tf)/maxTf);
    }

    @Override
    public double nn() {
        return 1;
    }

    @Override
    public double cn(double[] tfs) {
        double cosNorm = 0;
        for (int i = 0; i < tfs.length; i++) {
            cosNorm += tfs[i]*tfs[i];
        }
        return cosNorm == 0 ? 0 : 1/Math.sqrt(cosNorm);
    }

    @Override
    public double bn(String documentId) {
        if (!invertedIndex.getIndexedDocumentsIds().contains(documentId)) {
            return 0;
        }
        String[] docTerms = invertedIndex.getTermsInDocument(documentId);
        int byteLength = 0;
        for(String term : docTerms) {
            try {
                byteLength += term.getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {
                // do nothing
                // byteLength += 0
            }
        }

        return byteLength == 0 ? 0 : 1/byteLength;
    }

    @Override
    public int getDocumentCount() {
        return invertedIndex.getDocumentCount();
    }
}
