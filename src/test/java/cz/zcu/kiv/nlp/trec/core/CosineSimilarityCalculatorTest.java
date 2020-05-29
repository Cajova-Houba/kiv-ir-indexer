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
        invertedIndex.recalculateTermIdfs();
        invertedIndex.recalculateDocumentTfIdfs();

        documentCount = 3;

        // calculator
        similarityCalculator = new CosineSimilarityCalculator(invertedIndex, tokenizedQuery);
    }

    /**
     * Calculate relative cosine similarity between D1 and query.
     */
    @Test
    public void testCalculateScoreD1() {
        String documentId = "d1";
        double expectedScore = 1.197433761433455;
        double realScore = similarityCalculator.calculateScore(documentId);
        assertEquals("Wrong score for query-document1!", expectedScore, realScore, 0.01);
    }

    /**
     * Calculate relative cosine similarity between D2 and query.
     */
    @Test
    public void testCalculateScoreD2() {
        String documentId = "d2";
        double expectedScore = 0.6219267648071095;
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
