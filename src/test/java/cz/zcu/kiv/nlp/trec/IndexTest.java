package cz.zcu.kiv.nlp.trec;

import cz.zcu.kiv.nlp.ir.trec.Index;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.DocumentNew;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.preprocess.AdvancedTokenizer;
import cz.zcu.kiv.nlp.ir.trec.preprocess.CzechStemmerAgressive;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for indexer.
 */
public class IndexTest {

    public static final String DOC_1_ID = "d1";
    public static final String DOC_2_ID = "d2";
    public static final String DOC_3_ID = "d3";

    protected Index index;

    @Before
    public void setUp() {
        index = new Index(new AdvancedTokenizer(), new CzechStemmerAgressive(), new HashSet<>());

        List<Document> documents = Arrays.asList(
                new DocumentNew("car insurance auto insurance",DOC_1_ID),
                new DocumentNew("worst car auto insurance",DOC_2_ID),
                new DocumentNew("completely irrelevant",DOC_3_ID)
        );

        index.index(documents);
    }


    /**
     * Simple search with one term.
     */
    @Test
    public void testSimpleSearch() {
        String query = "car";
        int expResCount = 2;

        List<Result> results = index.search(query);
        assertEquals("wrong number of results returned!", expResCount, results.size() );
        assertTrue("First document not returned!", checkResultsContainDocument(results, DOC_1_ID));
        assertTrue("First document not returned!", checkResultsContainDocument(results, DOC_2_ID));
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
