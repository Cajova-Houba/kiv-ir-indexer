package cz.zcu.kiv.nlp.trec.core;

import cz.zcu.kiv.nlp.ir.trec.core.CosineSimilarityCalculator;
import cz.zcu.kiv.nlp.ir.trec.core.InvertedIndex;
import cz.zcu.kiv.nlp.ir.trec.core.SimilarityCalculator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CosineSimilarityCalculatorTest {

    private InvertedIndex invertedIndex;
    private int documentCount;
    private String[] tokenizedQuery;
    private SimilarityCalculator similarityCalculator;


    @Before
    public void setUp() {
        invertedIndex = new InvertedIndex();

        // query
        String query = "best car insurance";
        tokenizedQuery = query.split(" ");

        // manually create inverted index for these docs:
        // document 1: car insurance auto insurance
        // document 2: worst car auto insurance
        // document 3: completely irrelevant
        // => terms auto, car, completely, insurance, irrelevant, worst
        invertedIndex.indexDocument(new String[] {"car","insurance","auto","insurance"},"d1");
        invertedIndex.indexDocument(new String[] {"worst","car","auto","insurance"},"d2");
        invertedIndex.indexDocument(new String[] {"completely","irrelevant"},"d3");

        documentCount = 3;

        // calculator
        similarityCalculator = new CosineSimilarityCalculator(invertedIndex, tokenizedQuery);
    }

    @Test
    public void testNtf() {
        // expected natural tf for each query token in document d1
        double[] expectedNtf = new double[] {0, 1, 2};
        String documentId = "d1";

        int i = 0;
        for(String term : tokenizedQuery) {
            double ntf = similarityCalculator.ntf(term, documentId);
            assertEquals("Wrong ntf for term: "+term, expectedNtf[i], ntf, 0.01);
            i++;
        }
    }

    @Test
    public void testLtf() {
        // expected ltf for each query token in document d1
        double[] expectedLtf = new double[] {
                0,
                1 + Math.log10(1),
                1 + Math.log10(2)
        };
        String documentId = "d1";

        int i = 0;
        for(String term : tokenizedQuery) {
            double ltf = similarityCalculator.ltf(term, documentId);
            assertEquals("Wrong ltf for term: "+term, expectedLtf[i], ltf, 0.01);
            i++;
        }
    }

    @Test
    public void testDf() {
        // expected document frequencies for terms in query
        double[] expectedDf = new double[] {0, 2, 2};

        int i = 0;
        for(String term : tokenizedQuery) {
            double df = similarityCalculator.df(term);
            assertEquals("Wrong df for term: "+term, expectedDf[i], df, 0.01);
            i++;
        }
    }

    @Test
    public void testIdf() {
        // expected inverted document frequencies for terms in query
        double[] expectedIdf = new double[] {
                0,
                Math.log10(documentCount / 2.0),
                Math.log10(documentCount / 2.0)
        };

        int i = 0;
        for(String term : tokenizedQuery) {
            double idf = similarityCalculator.idf(term);
            assertEquals("Wrong idf for term: "+term, expectedIdf[i], idf, 0.01);
            i++;
        }
    }

    /**
     * Calculate cosine similarity between D1 and query.
     */
    @Test
    public void testCalculateScoreD1() {
        String documentId = "d1";
        double expectedScore = 0.84671;
        double realScore = similarityCalculator.calculateScore(documentId);
        assertEquals("Wrong score for query-document1!", expectedScore, realScore, 0.01);
    }

    /**
     * Calculate cosine similarity between D2 and query.
     */
    @Test
    public void testCalculateScoreD2() {
        String documentId = "d2";
        double expectedScore = 0.43976;
        double realScore = similarityCalculator.calculateScore(documentId);
        assertEquals("Wrong score for query-document2!", expectedScore, realScore, 0.01);
    }

    /**
     * Calculate cosine similarity between D3 and query.
     */
    @Test
    public void testCalculateScoreD3() {
        String documentId = "d3";

        double expectedScore = 0;

        double realScore = similarityCalculator.calculateScore(documentId);
        assertEquals("Wrong score for query-document3!", expectedScore, realScore, 0.01);
    }
}
