package cz.zcu.kiv.nlp.ir.trec.preprocess;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class for preprocessing queries and document texts.
 */
public class Preprocessor {

    public static final String withDiacritics = "áÁčČďĎéÉěĚíÍňŇóÓřŘšŠťŤúÚůŮýÝžŽĆć";
    public static final String withoutDiacritics = "aAcCdDeEeEiInNoOrRsStTuUuUyYzZCc";

    /**
     * Tokenizer to be used to tokenize input text.
     */
    private Tokenizer tokenizer;

    /**
     * Stemmer to e used to stem tokenized text (and tokens in search query).
     */
    private Stemmer stemmer;

    /**
     * Stopwords. Only lowercase without accents should be used. Tokens in those set will not be indexed.
     */
    private Set<String> stopwords;

    public Preprocessor(Tokenizer tokenizer, Stemmer stemmer, Set<String> stopwords) {
        this.tokenizer = tokenizer;
        this.stemmer = stemmer;
        this.stopwords = stopwords;
    }

    /**
     * Processes one term (uses stemmer only).
     * @param term Term to be processed.
     * @return Processed term.
     */
    public String processTerm(String term) {
        String t = term.toLowerCase();
        t = removeAccents(t);
        return stemmer.stem(t);
    }

    /**
     * Process input text.
     *
     * @param text Text to be processed.
     * @param useStemmer If true, stemmer will be used after tokenization.
     * @param useStopwords If true, stop words (before stemming) will be excluded from returned token array.
     * @return Processed text returned as array of tokens.
     */
    public String[] processText(String text, boolean useStemmer, boolean useStopwords) {
        if (tokenizer == null) {
            return new String[0];
        }

        text = text.toLowerCase();
        text = removeAccents(text);

        // tokenize
        List<String> tokenizedText = tokenizer.tokenize(text);

        // stopwords
        if (useStopwords && stopwords != null) {
            Iterator<String> tokenIt = tokenizedText.iterator();
            while(tokenIt.hasNext()) {
                if (stopwords.contains(tokenIt.next())) { tokenIt.remove(); }
            }
        }
        String[] tokens = tokenizedText.toArray(new String[0]);

        // stemmer
        if (useStemmer && stemmer != null) {
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = processTerm(tokens[i]);
            }
        }

        return tokens;
    }

    private String removeAccents(String text) {
        for (int i = 0; i < withDiacritics.length(); i++) {
            text = text.replaceAll("" + withDiacritics.charAt(i), "" + withoutDiacritics.charAt(i));
        }
        return text;
    }
}
