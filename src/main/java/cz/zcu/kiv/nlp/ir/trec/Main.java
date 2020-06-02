package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.core.InvertedIndex;
import cz.zcu.kiv.nlp.ir.trec.core.SearchMode;
import cz.zcu.kiv.nlp.ir.trec.core.retrieval.RetrievalWithProgress;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.gui.MainWindow;
import cz.zcu.kiv.nlp.ir.trec.preprocess.AdvancedTokenizer;
import cz.zcu.kiv.nlp.ir.trec.preprocess.CzechStemmerLight;
import cz.zcu.kiv.nlp.ir.trec.preprocess.EnglishStemmer;
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
     * Either use CZ preproc and expect CZ data or use EN preproc and expect CZ data.
     */
    public static boolean USE_CZ = true;

    /**
     * Name of the stopwords file in resource directory.
     */
    public static final String STOP_WORDS_FILE_NAME = "stopwords-english.txt";
    private static final String STOPWORDS_CZ_1 = "stopwords-czech-1.txt";

    public static final String TREC_EVAL_PARAM = "-cz";
    public static final String LANG_PARAM = "-trec";

    public static Index index;
    private static Logger log = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {

        if (isTrecEval(args)) {
            TestTrecEval.main(args);
            return;
        }

        checkLanguageMode(args);

        boolean indexStatus = initIndex();

        if (!indexStatus) {
            log.warn("Failed to initialize index, exiting application.");
            return;
        }

        JFrame mainWindow = new MainWindow();
    }

    /**
     * Whether to use czech preprocessing and expect czech data file.
     *
     * @return
     */
    public static boolean useCz() {
        return USE_CZ;
    }

    /**
     * Checks if -cz arg is present and sets USE_CZ var accordingly.
     * @param args
     */
    private static void checkLanguageMode(String[] args) {
        USE_CZ = false;
        for(String arg : args) {
            if (LANG_PARAM.equals(arg)) {
                USE_CZ = true;
                break;
            }
        }
    }

    private static boolean initIndex() {
        if (useCz()) {
            return initIndexCz();
        } else {
            return initIndexEn();
        }
    }

    private static boolean initIndexCz() {
        log.info("");
        log.info("=====================");
        log.info("Initializing index.");
        log.info("=====================");
        log.info("");
        log.info("Loading stopwrods.");
        Set<String> stopwords;
        try {
            stopwords = new HashSet<>(IOUtils.readLines(ClassLoader.getSystemResourceAsStream(STOPWORDS_CZ_1)));
        } catch (Exception ex) {
            log.error("Error while loading stopwords from resource file "+STOPWORDS_CZ_1+": "+ex.getMessage());
            return false;
        }
        log.info("Done");

        index = new Index(new AdvancedTokenizer(), new CzechStemmerLight(), stopwords);
        log.info("Index initialized");

        return true;
    }

    /**
     * Checks if the trec eval parameter is specified.
     * @param args
     * @return
     */
    private static boolean isTrecEval(String[] args) {
        for(String arg : args) {
            if (arg.equals(TREC_EVAL_PARAM)) {
                return true;
            }
        }

        return false;
    }

    private static boolean initIndexEn() {
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

        index = new Index(new AdvancedTokenizer(), new EnglishStemmer(), stopwords);
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
            index.index(document);
        }
    }

    public static void recalculateIndex() {
        if (index != null){
            index.getInvertedIndex().recalculateTermIdfs();
            index.getInvertedIndex().recalculateDocumentTfIdfs();
        }
    }

    /**
     * Performs search and returns results.
     *
     * @param query Search query.
     * @param topK Max number of top results returned.
     * @return Found results.
     */
    public static RetrievalWithProgress searchWithProgress(String query, SearchMode searchMode, int topK) throws QueryNodeException {
        log.debug("Executing query \"{}\" with max result count {}, tracking progress.", query, topK);
        if (index == null) {
            log.warn("No index, can't search.");
            return null;
        } else {
            index.setTopResultCount(topK);
            return index.searchWithProgress(query, searchMode);
        }
    }

    public static List<Result> extractTopKResults(PriorityQueue<Result> queue, int k) {
        if (index == null) {
            log.warn("No index.");
            return Collections.emptyList();
        }

        return index.getTopKResults(queue, k);
    }
}
