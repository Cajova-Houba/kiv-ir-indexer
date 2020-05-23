package cz.zcu.kiv.nlp.ir.trec.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Can read json with /r/politics comments and transform them to documents.
 */
public class RPolDocumentReader {

    public static final String DATA_ORIGINAL_DATE_FORMAT = "EEE MMM dd HH:mm:ss yyyy z";

    /**
     * Document title to be used for reddit comments.
     */
    public static final String DEFAULT_TITLE = "Reddit comment";

    private SimpleDateFormat sdf;

    public List<Document> readFile(File file) throws IOException, ParseException {
        sdf = new SimpleDateFormat(DATA_ORIGINAL_DATE_FORMAT, Locale.ENGLISH);
        ObjectMapper mapper = new ObjectMapper();
        List<HashMap<String, Object>> mappedData = mapper.readValue(file, new ArrayList().getClass());
        List<Document> documents = new ArrayList<>(mappedData.size());
        for(HashMap<String, Object> rpolData : mappedData) {
            documents.add(getDocument(rpolData));
        }

        return documents;
    }

    private Document getDocument(Map<String, Object> rpolData) throws ParseException {
        String username = rpolData.get("username").toString();
        Object timestampStr = rpolData.get("timestamp");
        String text = rpolData.get("text").toString();
        String id = username+":"+text.hashCode()+":"+UUID.randomUUID();
        int score = (Integer)rpolData.get("score");

        DocumentNew d = new DocumentNew(text, id);
        d.setRedditScore(score);
        d.setTitle(DEFAULT_TITLE);
        d.setUsername(username);

        if (timestampStr == null) {
            d.setDate(new Date());
        } else {
            d.setDate(sdf.parse(timestampStr.toString()));
        }

        return d;
    }
}
