package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.core.InvertedIndex;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.gui.MainWindow;
import cz.zcu.kiv.nlp.ir.trec.preprocess.AdvancedTokenizer;
import cz.zcu.kiv.nlp.ir.trec.preprocess.CzechStemmerAgressive;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Main class for running GUI.
 */
public class Main {


    /**
     * Name of the stopwords file in resource directory.
     */
    public static final String STOP_WORDS_FILE_NAME = "stopwords-czech-1.txt";

    public static Index index;
    static Logger log = Logger.getLogger(Main.class);


    public static void main(String[] args) {
        initIndex();

        JFrame mainWindow = new MainWindow();
    }

    public static void initIndex() {
        log.info("Initializing index.");
        log.info("Loading stopwrods.");
        Set<String> stopwords = new HashSet<>();
        try {
            stopwords = new HashSet<>(IOUtils.readLines(ClassLoader.getSystemResourceAsStream(STOP_WORDS_FILE_NAME)));
        } catch (Exception ex) {
            log.error("Error while loading stopwords from resource file "+STOP_WORDS_FILE_NAME+": "+ex.getMessage());
        }
        log.info("Done");

        index = new Index(new AdvancedTokenizer(), new CzechStemmerAgressive(), stopwords);
        log.info("Index initialized");
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
            index.index(Arrays.asList(document));
        }
    }
}
