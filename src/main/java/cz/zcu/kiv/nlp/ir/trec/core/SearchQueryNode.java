package cz.zcu.kiv.nlp.ir.trec.core;

import org.apache.lucene.search.BooleanClause;

import java.util.Collection;
import java.util.Map;

/**
 * Representation of one query node.
 */
public class SearchQueryNode {

    /**
     * Child nodes mapped to possible boolean operators.
     */
    private Map<BooleanClause.Occur, Collection<SearchQueryNode>> children;

    /**
     * Text of this node.
     */
    private String text;

    /**
     * Whether or not is this node a term.
     */
    private boolean isTerm;

    public void addChild(BooleanClause.Occur occur, SearchQueryNode child) {
        this.children.get(occur).add(child);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isTerm() {
        return isTerm;
    }

    public void setTerm(boolean term) {
        isTerm = term;
    }
}
