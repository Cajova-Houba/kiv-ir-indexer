package cz.zcu.kiv.nlp.trec;

import cz.zcu.kiv.nlp.ir.trec.Index;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.DocumentNew;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.preprocess.*;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test cases for indexer.
 */
public class IndexTest {

    public static final String DOC_1_ID = "d1";
    public static final String DOC_2_ID = "d2";
    public static final String DOC_3_ID = "d3";

    protected Index index;

    private String[] tokenizedQuery;

    @Before
    public void setUp() {
        Tokenizer t = new AdvancedTokenizer();
        Stemmer s = new CzechStemmerAgressive();
        Preprocessor p = new Preprocessor(t, s, Collections.emptySet());
        index = new Index(t, s, new HashSet<>());

        String query = "nejlepsi auto pojisteni";
        tokenizedQuery = p.processText(query, true, false);

        List<Document> documents = Arrays.asList(
                new DocumentNew("pojisteni auto vozidla",DOC_1_ID),
                new DocumentNew("nejhorsi pojisteni na auta",DOC_2_ID),
                new DocumentNew("uplne irelevantni dokument",DOC_3_ID)
        );

        index.index(documents);
    }

    @Test
    public void testIdf() {
        // expected inverted document frequencies for terms in query
        double[] expectedIdf = new double[] {
                0,
                Math.log10(index.getDocumentCount() / 2.0),
                Math.log10(index.getDocumentCount() / 2.0)
        };

        int i = 0;
        for(String term : tokenizedQuery) {
            double idf = index.getInvertedIndex().idf(term);
            assertEquals("Wrong idf for term: "+term, expectedIdf[i], idf, 0.01);
            i++;
        }
    }


    /**
     * Simple search with one term.
     */
    @Test
    public void testSimpleSearch() throws QueryNodeException {
        String query = "auto";
        int expResCount = 2;

        List<Result> results = index.search(query);
        assertEquals("wrong number of results returned!", expResCount, results.size() );
        assertTrue("First document not returned!", checkResultsContainDocument(results, DOC_1_ID));
        assertTrue("First document not returned!", checkResultsContainDocument(results, DOC_2_ID));
        checkResultsScoreNotNanOrZero(results);
    }

    /**
     * Simple AND query.
     */
    @Test
    public void testSimpleAndSearch() throws QueryNodeException {
        String query = "auto AND pojisteni";
        int expResCount = 2;

        List<Result> results = index.search(query);
        assertEquals("wrong number of results returned!", expResCount, results.size() );
        assertTrue("First document not returned!", checkResultsContainDocument(results, DOC_1_ID));
        assertTrue("First document not returned!", checkResultsContainDocument(results, DOC_2_ID));
        checkResultsScoreNotNanOrZero(results);
    }

    /**
     * Simple OR query.
     */
    @Test
    public void testSimpleOrSearch() throws QueryNodeException {
        String query1 = "auto nejhorsi";
        String query2 = "auto OR nejhorsi";
        int expResCount = 2;

        List<Result> results = index.search(query1);
        assertEquals("wrong number of results returned!", expResCount, results.size() );
        assertTrue("First document not returned!", checkResultsContainDocument(results, DOC_1_ID));
        assertTrue("Second document not returned!", checkResultsContainDocument(results, DOC_2_ID));
        checkResultsScoreNotNanOrZero(results);

        results = index.search(query2);
        assertEquals("wrong number of results returned for query 2!", expResCount, results.size() );
        assertTrue("First document not returned for query 2!", checkResultsContainDocument(results, DOC_1_ID));
        assertTrue("Second document not returned for query 2!", checkResultsContainDocument(results, DOC_2_ID));
        checkResultsScoreNotNanOrZero(results);
    }

    /**
     * Test simple NOT.
     */
    @Test
    public void testSimpleNotSearch() throws QueryNodeException {
        String query = "NOT auto";
        int expResCount = 1;

        List<Result> results = index.search(query);
        assertEquals("Wrong number of results returned!", expResCount, results.size() );
        assertTrue("First document not returned!", checkResultsContainDocument(results, DOC_3_ID));
        checkResultsScoreNotNanOrZero(results);
    }

    @Test
    public void testNotSearch1() throws QueryNodeException {
        String query = "NOT auto NOT pojisteni";
        int expResCount = 1;

        List<Result> results = index.search(query);
        assertEquals("Wrong number of results returned!", expResCount, results.size() );
        assertTrue("First document not returned!", checkResultsContainDocument(results, DOC_3_ID));
        checkResultsScoreNotNanOrZero(results);
    }

    @Test
    public void testNotSearch2() throws QueryNodeException {
        String query = "NOT auto NOT pojisteni NOT uplne";
        int expResCount = 0;

        List<Result> results = index.search(query);
        assertEquals("wrong number of results returned!", expResCount, results.size() );
        checkResultsScoreNotNanOrZero(results);
    }

    @Test
    public void testComplexSearch1() throws QueryNodeException {
        String query = "(NOT vozidlo) AND (NOT irelevantni)";
        int expResCount = 1;

        List<Result> results = index.search(query);
        assertEquals("Wrong number of results returned!", expResCount, results.size() );
        assertTrue("Second document not returned!", checkResultsContainDocument(results, DOC_2_ID));
        checkResultsScoreNotNanOrZero(results);
    }

    @Test
    public void testComplexSearch2() throws QueryNodeException {
        String query = "((NOT vozidlo) AND (NOT irelevantni) OR  (pojisteni AND nejhorsi))";
        int expResCount = 1;

        List<Result> results = index.search(query);
        assertEquals("Wrong number of results returned!", expResCount, results.size() );
        assertTrue("Second document not returned!", checkResultsContainDocument(results, DOC_2_ID));
        checkResultsScoreNotNanOrZero(results);
    }

    /**
     * Checks that result scores are not NaN or 0.
     * @param results
     */
    private void checkResultsScoreNotNanOrZero(List<Result> results) {
        for(Result r : results) {
            assertFalse("Resutl "+r+" has NaN score!", Double.isNaN(r.getScore()));
        }
    }

    /**
     * Checks that the result set contains document with given id.
     *
     * @param results Result set.
     * @param documentId Document id.
     * @return True if the result set contains document id.
     */
    private boolean checkResultsContainDocument(List<Result> results, String documentId) {
        for(Result r : results) {
            if (r.getDocumentID().equalsIgnoreCase(documentId)) {
                return true;
            }
        }

        return false;
    }
}
