package cz.zcu.kiv.nlp.trec.core;

import cz.zcu.kiv.nlp.ir.trec.core.Posting;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PostingTest {

    /**
     * Test that two postings with same document id will produce same document id hash. Otherwise the intersetion
     * algorithms would not work properly.
     */
    @Test
    public void testDocumentIdHashEquals() {
        final String docId = "asf";
        Posting p1 = new Posting(docId);
        Posting p2 = new Posting(docId);

        assertEquals("Postings hashes are not same!", p1.documentIdHash(), p2.documentIdHash());
    }
}
