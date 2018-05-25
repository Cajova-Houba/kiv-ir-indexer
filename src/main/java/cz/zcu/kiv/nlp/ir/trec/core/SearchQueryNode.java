package cz.zcu.kiv.nlp.ir.trec.core;

import org.apache.lucene.search.BooleanClause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of one query node.
 */
public class SearchQueryNode {

    /**
     * Child nodes mapped to possible boolean operators.
     */
    private Map<BooleanClause.Occur, List<SearchQueryNode>> children;

    /**
     * Text of this node.
     */
    private String text;

    /**
     * Whether or not is this node a term.
     */
    private boolean isTerm;

    public SearchQueryNode() {
        children = new HashMap<>();
    }

    public void addChild(BooleanClause.Occur occur, SearchQueryNode child) {
        if (!children.containsKey(occur)) {
            children.put(occur, new ArrayList<>());
        }
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

    public Map<BooleanClause.Occur, List<SearchQueryNode>> getChildren() {
        return children;
    }

    /**
     * Recursively searches through the whole query tree and returns all the terms in query.
     * @return Terms in query.
     */
    public List<String> getTerms() {
        return getTermsRec(new ArrayList<>());
    }

    /**
     * Recursively searches through the whole query tree and returns all the terms in query.
     * Internal method used to do the actual recursion.
     *
     * @return Terms in query.
     */
    private List<String> getTermsRec(List<String> terms) {
        if(isTerm) {
            terms.add(this.text);
        } else {
            for(Map.Entry<BooleanClause.Occur, List<SearchQueryNode>> childOccurrence : this.children.entrySet()) {
                for(SearchQueryNode child : childOccurrence.getValue()) {
                    child.getTermsRec(terms);
                }
            }
        }

        return terms;
    }
}
