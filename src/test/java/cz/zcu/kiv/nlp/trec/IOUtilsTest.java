package cz.zcu.kiv.nlp.trec;

import cz.zcu.kiv.nlp.ir.trec.IOUtils;
import cz.zcu.kiv.nlp.ir.trec.core.InvertedIndex;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IOUtilsTest {

    /**
     * Save index to file and try to load it again.
     */
    @Test
    public void testSaveIndex() throws IOException, ClassNotFoundException {
        String fileName = "test-saved-index.indx";
        String testTerm = "doc";
        InvertedIndex index = new InvertedIndex();
        index.indexDocument(new String[] {"test", "doc"}, "d1");
        index.indexDocument(new String[] {"test", "doc", "2"}, "d2");

        // save and load index again
        IOUtils.saveIndex(index, fileName);
        InvertedIndex newIndex = IOUtils.loadIndex(fileName);

        // test
        assertNotNull("Null index returned!", newIndex );
        assertEquals("Wrong number of indexed documents!", index.getDocumentCount(), newIndex.getDocumentCount());
        assertEquals("Inverted index doesn't return correct document frequency!", index.documentFrequency(testTerm), newIndex.documentFrequency(testTerm));
    }
}
