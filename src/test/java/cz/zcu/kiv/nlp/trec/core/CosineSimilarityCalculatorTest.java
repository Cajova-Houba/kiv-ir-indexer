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
        similarityCalculator = new CosineSimilarityCalculator(invertedIndex);
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
                1 + Math.log(1),
                1 + Math.log(2)
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
                Math.log(documentCount / 2.0),
                Math.log(documentCount / 2.0)
        };

        int i = 0;
        for(String term : tokenizedQuery) {
            double idf = similarityCalculator.idf(term);
            assertEquals("Wrong idf for term: "+term, expectedIdf[i], idf, 0.01);
            i++;
        }
    }

    @Test
    public void testCalculateScore() {
        // expected score for query and document 1
        // 1. ltf for all terms in query for document 1
        double[] docLtfs = new double[] {
                0,
                1 + Math.log(1),
                1 + Math.log(2)
        };

        // 2. use those ltfs to calculate cosine normalization constant
        double cosNormConst = 0;
        for(double w : docLtfs) {
            cosNormConst += w*w;
        }
        cosNormConst = 1/Math.sqrt(cosNormConst);

        // 3. tf-idf for all terms in query
        // this ones is calculated as tf-idf(query, query)
        double[] queryTfIdf = new double[] {
                0,
                Math.log(documentCount / 2.0) * (1+Math.log(1)),
                Math.log(documentCount / 2.0) * (1+Math.log(1))
        };

        // 4. expected cosine similarity
        double expectedSimilarity = 0;
        for(int i = 0; i < docLtfs.length; i++) {
            expectedSimilarity += queryTfIdf[i] * docLtfs[i] * cosNormConst;
        }
        String documentId = "d1";

        double realScore = similarityCalculator.calculateScore(tokenizedQuery, documentId);
        assertEquals("Wrong score for query-document1!", expectedSimilarity, realScore, 0.01);
    }
}
