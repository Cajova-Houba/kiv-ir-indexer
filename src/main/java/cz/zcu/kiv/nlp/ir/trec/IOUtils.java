package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.core.InvertedIndex;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author tigi
 */
public class IOUtils {

    /**
     * Saves given index to file. May throw exceptions.
     *
     * @param index Index to be saved.
     * @param fileName Name of the file.
     */
    public static void saveIndex(InvertedIndex index, String fileName) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(index);
        out.close();
        fileOut.close();
    }

    /**
     * Loads index from file and returns it. May throw exceptions.
     *
     * @param fileName Name of the file to load index from.
     * @return Loaded index.
     */
    public static InvertedIndex loadIndex(String fileName) throws IOException, ClassNotFoundException {
        final ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(fileName));
        final Object object = objectInputStream.readObject();
        objectInputStream.close();
        return (InvertedIndex) object;
    }

    /**
     * Read lines from the stream; lines are trimmed and empty lines are ignored.
     *
     * @param inputStream stream
     * @return list of lines
     */
    public static List<String> readLines(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot locate stream");
        }
        try {
            List<String> result = new ArrayList<String>();

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result.add(line.trim());
                }
            }

            inputStream.close();

            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Read lines from the stream; lines are trimmed and empty lines are ignored.
     *
     * @param inputStream stream
     * @return text
     */
    public static String readFile(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot locate stream");
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            inputStream.close();

            return sb.toString().trim();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Saves lines from the list into given file; each entry is saved as a new line.
     *
     * @param file file to save
     * @param list lines of text to save
     */
    public static void saveFile(File file, Collection<String> list) {
        PrintStream printStream = null;
        try {
            printStream = new PrintStream(new FileOutputStream(file), true, "UTF-8");

            for (String text : list) {
                printStream.println(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (printStream != null) {
                printStream.close();
            }
        }
    }

    /**
     * Saves lines from the list into given file; each entry is saved as a new line.
     *
     * @param file file to save
     * @param text text to save
     */
    public static void saveFile(File file, String text) {
        PrintStream printStream = null;
        try {
            printStream = new PrintStream(new FileOutputStream(file), true, "UTF-8");

            printStream.println(text);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (printStream != null) {
                printStream.close();
            }
        }
    }
}
