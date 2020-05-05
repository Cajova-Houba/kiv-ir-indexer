package cz.zcu.kiv.nlp.trec.core;

import cz.zcu.kiv.nlp.ir.trec.core.CosineSimilarityCalculator;
import cz.zcu.kiv.nlp.ir.trec.core.InvertedIndex;
import cz.zcu.kiv.nlp.ir.trec.core.Posting;
import cz.zcu.kiv.nlp.ir.trec.core.SimilarityCalculator;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for InvertedIndex.
 */
public class InvertedIndexTest {

    private InvertedIndex invertedIndex;
    private int documentCount;
    private String[] tokenizedQuery;

    private String[] d1 = new String[] {"car","insurance","auto","insurance"},
                    d2 = new String[] {"worst","car","auto","insurance"},
                    d3 = new String[] {"completely","irrelevant"};

    private int[] d1tf = new int[] {1,2,1,2},
                d2tf = new int[] {1,1,1,1},
                d3tf = new int[] {1,1};

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
        invertedIndex.indexDocument(d1,"d1");
        invertedIndex.indexDocument(d2,"d2");
        invertedIndex.indexDocument(d3,"d3");

        documentCount = 3;
    }

    @Test
    public void testGetDocumentCount() {
        assertEquals("Wrong document count!", documentCount, invertedIndex.getDocumentCount());
    }

    @Test
    public void testGetDocumentFrequency_nonExistentTerm() {
        assertEquals("Wrong document frequency for non existing term.", 0, invertedIndex.documentFrequency("nonexistent"));
    }

    @Test
    public void testGetDocumentFrequency() {
        assertEquals("Wrong document frequency for term 'car'", 2, invertedIndex.documentFrequency("car"));
        assertEquals("Wrong document frequency for term 'worst'", 1, invertedIndex.documentFrequency("worst"));
    }

    @Test
    public void testGetTermFrequencyD1() {
        for (int i = 0; i < d1.length; i++) {
            String term = d1[i];
            int tf = d1tf[i];
            assertEquals("Wrong term frequency for term "+term, tf, invertedIndex.getTermFrequency(term, "d1"));
        }
    }

    @Test
    public void testGetTermFrequencyD2() {
        for (int i = 0; i < d2.length; i++) {
            String term = d2[i];
            int tf = d2tf[i];
            assertEquals("Wrong term frequency for term "+term, tf, invertedIndex.getTermFrequency(term, "d2"));
        }
    }

    @Test
    public void testGetTermFrequencyD3() {
        for (int i = 0; i < d3.length; i++) {
            String term = d3[i];
            int tf = d3tf[i];
            assertEquals("Wrong term frequency for term "+term, tf, invertedIndex.getTermFrequency(term, "d3"));
        }
    }

    @Test
    public void testGetPostingsForTem_car() {
        String term = "car";
        Map<String, Integer> documentTermFreq = new HashMap<>();
        documentTermFreq.put("d1", 1);
        documentTermFreq.put("d2", 1);

        List<Posting> postings = invertedIndex.getPostingsForTerm(term);
        for (Posting p : postings) {
            assertTrue("Posting contains unexpected document!", documentTermFreq.containsKey(p.getDocumentId()));
            assertEquals("Wrong term frequency of term "+term+" in document "+p.getDocumentId(), documentTermFreq.get(p.getDocumentId()).intValue(), p.getTermFrequency());
        }
    }

    @Test
    public void testGetPostingsForTem_insurance() {
        String term = "insurance";
        Map<String, Integer> documentTermFreq = new HashMap<>();
        documentTermFreq.put("d1", 2);
        documentTermFreq.put("d2", 1);

        List<Posting> postings = invertedIndex.getPostingsForTerm(term);
        for (Posting p : postings) {
            assertTrue("Posting contains unexpected document!", documentTermFreq.containsKey(p.getDocumentId()));
            assertEquals("Wrong term frequency of term "+term+" in document "+p.getDocumentId(), documentTermFreq.get(p.getDocumentId()).intValue(), p.getTermFrequency());
        }
    }


}