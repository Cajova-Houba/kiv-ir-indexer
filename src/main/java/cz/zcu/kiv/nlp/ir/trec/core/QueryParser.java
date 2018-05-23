package cz.zcu.kiv.nlp.ir.trec.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Parser for search queries (especially boolean).
 */
public class QueryParser {

    /**
     * Lucene parser.
     */
    private PrecedenceQueryParser parser;

    public QueryParser(Analyzer analyzer) {
        this.parser = new PrecedenceQueryParser(analyzer);
    }

    /**
     * Parse string query and create SearchQuery object.
     * @param query Query to be parsed.
     * @return Parsed query.
     */
    public SearchQueryNode parseQuery(String query) {
        try {
            Query q =  parser.parse(query, "");
            SearchQueryNode root = new SearchQueryNode();
            createQueryTree(q, root);
            return root;
        } catch (QueryNodeException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recursive method which creates tree representation of search query.
     * @param luceneQuery Lucene query.
     * @param root Root to add child to.
     */
    private void createQueryTree(Query luceneQuery, SearchQueryNode root) {
        BooleanQuery booleanQuery = (BooleanQuery) luceneQuery;
        for(BooleanClause clause : booleanQuery.clauses()) {
            SearchQueryNode child = new SearchQueryNode();
            child.setText(clause.getQuery().toString());
            root.addChild(clause.getOccur(), child);
            if(clause.getQuery().getClass() == BooleanQuery.class) {
                createQueryTree(clause.getQuery(), child);
            }else if(clause.getQuery().getClass() == TermQuery.class){
                child.setTerm(true);
            }
        }
    }
}
