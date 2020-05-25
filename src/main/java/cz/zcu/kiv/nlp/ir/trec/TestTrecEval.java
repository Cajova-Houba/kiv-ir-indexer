package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.Topic;
import cz.zcu.kiv.nlp.ir.trec.preprocess.AdvancedTokenizer;
import cz.zcu.kiv.nlp.ir.trec.preprocess.CzechStemmerAgressive;
import org.apache.log4j.*;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

import java.io.*;
import java.util.*;


/**
 * @author tigi
 *
 * Třída slouží pro vyhodnocení vámi vytvořeného vyhledávače
 *
 */
public class TestTrecEval {

    private static Logger log = Logger.getLogger(TestTrecEval.class);
    private static final String OUTPUT_DIR = "./TREC";
    private static final String STOPWORDS_CZ_1 = "stopwords-czech-1.txt";

    protected static void configureLogger() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();

        File results = new File(OUTPUT_DIR);
        if (!results.exists()) {
            results.mkdir();
        }

        try {
            Appender appender = new WriterAppender(new PatternLayout(), new FileOutputStream(new File(OUTPUT_DIR + "/" + SerializedDataHelper.SDF.format(System.currentTimeMillis()) + " - " + ".log"), false));
            BasicConfigurator.configure(appender);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.getRootLogger().setLevel(Level.TRACE);
    }

    /**
     * Metoda vytvoří objekt indexu, načte data, zaindexuje je provede předdefinované dotazy a výsledky vyhledávání
     * zapíše souboru a pokusí se spustit evaluační skript.
     *
     * Na windows evaluační skript pravděpodbně nebude možné spustit. Pokud chcete můžete si skript přeložit a pak
     * by mělo být možné ho spustit.
     *
     * Pokud se váme skript nechce překládat/nebo se vám to nepodaří. Můžete vygenerovaný soubor s výsledky zkopírovat a
     * spolu s přiloženým skriptem spustit (přeložit) na
     * Linuxu např. pomocí vašeho účtu na serveru ares.fav.zcu.cz
     *
     * Metodu není třeba měnit kromě řádků označených T O D O  - tj. vytvoření objektu třídy {@link Index} a
     */
    public static void main(String args[]) throws QueryNodeException {
        configureLogger();

        Index index = new Index(
                new AdvancedTokenizer(),
                new CzechStemmerAgressive(),
                loadStopwords(STOPWORDS_CZ_1));
        index.setTopResultCount(-1);

        List<Topic> topics = SerializedDataHelper.loadTopic(new File(OUTPUT_DIR + "/topicData.bin"));

        File serializedData = new File(OUTPUT_DIR + "/czechData.bin");

        List<Document> documents = new ArrayList<>();
        log.info("load");
        try {
            if (serializedData.exists()) {
                documents = SerializedDataHelper.loadDocument(serializedData);
            } else {
                log.error("Cannot find " + serializedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Documents: " + documents.size());

        // index load documents
        log.info("Indexing... ");
        index.index(documents);
        log.info("Done.");


        List<String> lines = new ArrayList<>();

        int cnt = 1;
        for (Topic t : topics) {
            //TODO vytvoření dotazu, třída Topic představuje dotaz pro vyhledávání v zaindexovaných dokumentech
            //a obsahuje tři textová pole title, description a narrative. To jak sestavíte dotaz je na Vás a pravděpodobně
            //to ovlivní výsledné vyhledávání - zkuste změnit a uvidíte jaký MAP (Mean Average Precision) dostanete pro jednotlivé
            //kombinace např. pokud budete vyhledávat jen pomocí title (t.getTitle()) nebo jen pomocí description (t.getDescription())
            //nebo jejich kombinací (t.getTitle() + " " + t.getDescription())
            log.debug(cnt+"/"+topics.size());
            List<Result> resultHits = index.search(t.getTitle() + " " + t.getDescription());

            Comparator<Result> cmp = (o1, o2) -> {
                if (o1.getScore() > o2.getScore()) return -1;
                if (Float.compare(o1.getScore(), o2.getScore()) == 1) return 0;
                return 1;
            };

            resultHits.sort(cmp);
            for (Result r : resultHits) {
                final String line = r.toString(t.getId());
                lines.add(line);
            }
            if (resultHits.size() == 0) {
                lines.add(t.getId() + " Q0 " + "abc" + " " + "99" + " " + 0.0 + " runindex1");
            }
            cnt++;
        }
        final File outputFile = new File(OUTPUT_DIR + "/results " + SerializedDataHelper.SDF.format(System.currentTimeMillis()) + ".txt");
        IOUtils.saveFile(outputFile, lines);
        //try to run evaluation
        try {
            runTrecEval(outputFile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String runTrecEval(String predictedFile) throws IOException {

        String commandLine = "./trec_eval.8.1/./trec_eval" +
                " ./trec_eval.8.1/czech" +
                " " + predictedFile;

        System.out.println(commandLine);
        Process process = Runtime.getRuntime().exec(commandLine);

        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String trecEvalOutput;
        StringBuilder output = new StringBuilder("TREC EVAL output:\n");
        for (String line; (line = stdout.readLine()) != null; ) output.append(line).append("\n");
        trecEvalOutput = output.toString();
        System.out.println(trecEvalOutput);

        int exitStatus = 0;
        try {
            exitStatus = process.waitFor();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        System.out.println(exitStatus);

        stdout.close();
        stderr.close();

        return trecEvalOutput;
    }

    /**
     * Loads stopwords from file placed in resource folder.
     *
     * @param resourceFileName Name of the file in resource folder.
     * @return Stopword.
     */
    private static Set<String> loadStopwords(String resourceFileName) {

        return new HashSet<>(IOUtils.readLines(ClassLoader.getSystemResourceAsStream(resourceFileName)));
    }
}
