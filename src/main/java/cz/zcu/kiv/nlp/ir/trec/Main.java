package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.core.InvertedIndex;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.gui.MainWindow;
import cz.zcu.kiv.nlp.ir.trec.preprocess.AdvancedTokenizer;
import cz.zcu.kiv.nlp.ir.trec.preprocess.CzechStemmerAgressive;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

/**
 * Main class for running GUI.
 */
public class Main {


    /**
     * Name of the stopwords file in resource directory.
     */
    public static final String STOP_WORDS_FILE_NAME = "stopwords-czech-1.txt";

    public static Index index;
    private static Logger log = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        boolean indexStatus = initIndex();

        if (!indexStatus) {
            log.warn("Failed to initialize index, exiting application.");
            return;
        }

        JFrame mainWindow = new MainWindow();
    }

    private static boolean initIndex() {
        log.info("");
        log.info("=====================");
        log.info("Initializing index.");
        log.info("=====================");
        log.info("");
        log.info("Loading stopwrods.");
        Set<String> stopwords;
        try {
            stopwords = new HashSet<>(IOUtils.readLines(ClassLoader.getSystemResourceAsStream(STOP_WORDS_FILE_NAME)));
        } catch (Exception ex) {
            log.error("Error while loading stopwords from resource file "+STOP_WORDS_FILE_NAME+": "+ex.getMessage());
            return false;
        }
        log.info("Done");

        index = new Index(new AdvancedTokenizer(), new CzechStemmerAgressive(), stopwords);
        log.info("Index initialized");

        return true;
    }

    /**
     * Returns index.
     */
    public static Index getIndex() {
        return index;
    }

    /**
     * Loads index from file. May throw exceptions.
     * @param fileName Source file name.
     */
    public static void loadIndexFromFile(String fileName) throws IOException, ClassNotFoundException {
        InvertedIndex invertedIndex = IOUtils.loadIndex(fileName);
        index.setInvertedIndex(invertedIndex);
    }

    /**
     * Saves index to file. May throw exceptions.
     * @param fileName Target file name.
     */
    public static void saveIndexToFile(String fileName) throws IOException {
        IOUtils.saveIndex(index.getInvertedIndex(), fileName);
    }

    /**
     * Adds document to index.
     *
     * @param document Document to be indexed.
     */
    public static void indexDocument(Document document) {
        if (index != null) {
            index.index(Collections.singletonList(document));
        }
    }

    /**
     * Adds a collection of documents to index.
     *
     * @param documents Documents to be indexed.
     */
    public static void indexDocuments(List<Document> documents) {
        if (index != null) {
            index.index(documents);
        }
    }

    /**
     * Performs search and returns results.
     *
     * @param query Search query.
     * @param topK Max number of top results returned.
     * @return Found results.
     */
    public static List<Result> search(String query, int topK) throws QueryNodeException {
        log.debug("Executing query \"{}\" with max result count {}.", query, topK);
        if (index == null) {
            log.warn("No index, can't search.");
            return new ArrayList<>();
        } else {
            index.setTopResultCount(topK);
            return index.search(query);
        }
    }
}
