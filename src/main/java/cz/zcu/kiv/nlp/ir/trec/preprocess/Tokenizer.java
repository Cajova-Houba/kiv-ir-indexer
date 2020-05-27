package cz.zcu.kiv.nlp.ir.trec.preprocess;

import java.util.List;

/**
 * Tokenizer which will convert string to a collection of tokens.
 */
public interface Tokenizer {

    /**
     * Tokenizes text and returns it as a list of tokens.
     * @param text Text to be tokenized.
     * @return Tokenized text.
     */
    List<String> tokenize(String text);
}
