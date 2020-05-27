package cz.zcu.kiv.nlp.ir.trec.core;

/**
 * Supported search modes.
 */
public enum SearchMode {

    /**
     * Query is to be interpreted as boolean.
     */
    BOOLEAN("Boolean"),

    /**
     * Query is to be interpreted as term and ranked retrieval is to be performed.
     */
    RANKED("Ranked");

    public final String name;

    SearchMode(String name) {
        this.name = name;
    }
}
