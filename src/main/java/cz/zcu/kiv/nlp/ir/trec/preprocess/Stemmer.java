package cz.zcu.kiv.nlp.ir.trec.preprocess;

/**
 * Yeah a stemmer.
 */
public interface Stemmer {

    String stem(String input);
}
