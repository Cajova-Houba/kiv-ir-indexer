package cz.zcu.kiv.nlp.trec.data;

import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.RPolDocumentReader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RPolDocumentReaderTest {

    @Test
    public void testReadOne() throws URISyntaxException, IOException, ParseException {
        String fname = "rpol-one.json";
        URL url = getClass().getResource("/"+fname);
        File f = new File(url.toURI());
        RPolDocumentReader reader = new RPolDocumentReader();

        List<Document> documents = reader.readFile(f);

        assertEquals("Wrong number of documents", 1, documents.size());

        checkDocument(documents.get(0), "TwilitSky", 25);
    }

    @Test
    public void testReadTwo() throws URISyntaxException, IOException, ParseException {
        String fname = "rpol-two.json";
        URL url = getClass().getResource("/"+fname);
        File f = new File(url.toURI());
        RPolDocumentReader reader = new RPolDocumentReader();

        List<Document> documents = reader.readFile(f);

        assertEquals("Wrong number of documents", 2, documents.size());

        checkDocument(documents.get(0), "TwilitSky", 25);
        checkDocument(documents.get(1), "SpinningHead", 17);
    }

    private void checkDocument(Document d, String username, int score) {
        assertEquals("Wrong username", username, d.getUsername());
        assertEquals("Wrong title", RPolDocumentReader.DEFAULT_TITLE, d.getTitle());
        assertEquals("Wrong score", score, d.getRedditScore());
        assertFalse("Empty text", d.getText() == null || d.getText().isEmpty());
    }
}
