package cz.zcu.kiv.nlp.trec.core;

import cz.zcu.kiv.nlp.ir.trec.core.InvertedIndex;
import cz.zcu.kiv.nlp.ir.trec.core.Posting;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        invertedIndex.recalculateTermIdfs();
        invertedIndex.recalculateDocumentTfIdfs();

        documentCount = 3;
    }

    /**
     * Test AND intersection of two posting lists.
     *
     * [d1,d2,d3] AND [d2,d3,d4] = [d2,d3]
     */
    @Test
    public void testAndIntersect() {
        List<Posting> p1 = new ArrayList<>();
        p1.add(new Posting("d1"));
        p1.add(new Posting("d2"));
        p1.add(new Posting("d3"));
        List<Posting> p2 = new ArrayList<>();
        p2.add(new Posting("d2"));
        p2.add(new Posting("d3"));
        p2.add(new Posting("d4"));
        List<Posting> expectedResult = new ArrayList<>();
        expectedResult.add(new Posting("d2"));
        expectedResult.add(new Posting("d3"));

        List<Posting> res = invertedIndex.andIntersect(p1, p2);

        assertEquals("Wrong number of results returned!", expectedResult.size(), res.size());
        for(Posting expectedP : expectedResult) {
            assertTrue(expectedP+" not returned!", res.contains(expectedP));
        }
    }

    /**
     * Test OR intersection of two posting lists.
     *
     * [d1,d2,d3] AND [d2,d3,d4] = [d1,d2,d3,d4]
     */
    @Test
    public void testOrIntersect() {
        List<Posting> p1 = new ArrayList<>();
        p1.add(new Posting("d1"));
        p1.add(new Posting("d2"));
        p1.add(new Posting("d3"));
        List<Posting> p2 = new ArrayList<>();
        p2.add(new Posting("d2"));
        p2.add(new Posting("d3"));
        p2.add(new Posting("d4"));
        List<Posting> expectedResult = new ArrayList<>();
        expectedResult.add(new Posting("d1"));
        expectedResult.add(new Posting("d2"));
        expectedResult.add(new Posting("d3"));
        expectedResult.add(new Posting("d4"));

        List<Posting> res = invertedIndex.orIntersect(p1, p2);

        assertEquals("Wrong number of results returned!", expectedResult.size(), res.size());
        for(Posting expectedP : expectedResult) {
            assertTrue(expectedP+" not returned!", res.contains(expectedP));
        }
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

    @Test
    public void testGetDocumentTfIdf_d1() {
        Map<String, Double> expectedTfIdfs = new HashMap<>();
        expectedTfIdfs.put("car", Math.log10(3 / 2.0) * (1+ Math.log10(1.0)));
        expectedTfIdfs.put("insurance", Math.log10(3 / 2.0) *(1+  Math.log10(2.0)));
        expectedTfIdfs.put("auto", Math.log10(3 / 2.0) *  (1+Math.log10(1.0)));

        Map<String, Double> tfIdfs = invertedIndex.getDocumentTfIdf("d1");

        for(Map.Entry<String, Double> entry: expectedTfIdfs.entrySet()) {
            assertTrue("Missing term "+entry.getKey(), tfIdfs.containsKey(entry.getKey()));
            assertEquals("Wrong TF-IDF", entry.getValue(), tfIdfs.get(entry.getKey()), 0.001);
        }
    }


}
