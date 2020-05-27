package cz.zcu.kiv.nlp.ir.trec.preprocess;

import java.text.Normalizer;
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

    private boolean useStemmer, useStopWords;

    public Preprocessor(Tokenizer tokenizer, Stemmer stemmer, Set<String> stopwords, boolean useStemmer, boolean useStopWords) {
        this.tokenizer = tokenizer;
        this.stemmer = stemmer;
        this.stopwords = stopwords;
        this.useStemmer = useStemmer;
        this.useStopWords = useStopWords;
    }

    public Preprocessor(Tokenizer tokenizer, Stemmer stemmer, Set<String> stopwords) {
        this(tokenizer, stemmer, stopwords, true, true);
    }

    /**
     * Processes one term (uses stemmer only).
     * @param term Term to be processed.
     * @return Processed term.
     */
    public String processTerm(String term) {
        String[] tokens = processText(term);
        return tokens.length > 0 ? tokens[0] : "";
    }

    /**
     * Process input text.
     *
     * @param text Text to be processed.
     * @return Processed text returned as array of tokens.
     */
    public String[] processText(String text) {
        if (tokenizer == null) {
            return new String[0];
        }

        text = text.toLowerCase();
        text = removeAccents(text);

        // tokenize
        List<String> tokenizedText = tokenizer.tokenize(text);

        // stopwords
        if (useStopWords && stopwords != null) {
            Iterator<String> tokenIt = tokenizedText.iterator();
            while(tokenIt.hasNext()) {
                if (stopwords.contains(tokenIt.next())) { tokenIt.remove(); }
            }
        }
        String[] tokens = tokenizedText.toArray(new String[0]);

        // stemmer
        if (useStemmer && stemmer != null) {
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = stemmer.stem(tokens[i]);
            }
        }

        return tokens;
    }

    private String removeAccents(String text) {
        text =  text == null ? "" : Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // InCombiningDiacriticalMarks won't remove everything
        for (int i = 0; i < withDiacritics.length(); i++) {
            text = text.replaceAll("" + withDiacritics.charAt(i), "" + withoutDiacritics.charAt(i));
        }

        return text;
    }
}
