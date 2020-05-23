package cz.zcu.kiv.nlp.ir.trec.core;

import cz.zcu.kiv.nlp.ir.trec.preprocess.Preprocessor;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for search queries (especially boolean).
 */
public class QueryParser {

    private static Logger log = LoggerFactory.getLogger(QueryParser.class);

    /**
     * Lucene parser.
     */
    private PrecedenceQueryParser parser;

    /**
     * My cool preprocessor. Way better than the lucene's one.
     */
    private Preprocessor preprocessor;

    /**
     * Default field for query parser.
     */
    private String defaultField;

    public QueryParser(Preprocessor preprocessor) {
        this.preprocessor = preprocessor;
        this.parser = new PrecedenceQueryParser();
        defaultField = "";
    }

    /**
     * Parse string query and create SearchQuery object.
     * @param query Query to be parsed.
     * @return Parsed query.
     */
    public SearchQueryNode parseQuery(String query) throws QueryNodeException {
        log.debug("Preparing to parse query \"{}\" with default field \"{}\".", query, defaultField);
        Query q =  parser.parse(query, "");
        SearchQueryNode root = new SearchQueryNode();
        if (q.getClass() == TermQuery.class) {
            log.trace("Term query detected.");
            root.setTerm(true);
            root.setText(preprocessor.processTerm(((TermQuery)q).getTerm().text()));
        } else if (q.getClass() == BooleanQuery.class) {
            log.trace("Boolean query detected.");
            log.trace("Creating query tree for boolean query.");
            createQueryTree(q, root);
        } else {
            log.trace("Unknown query type {}.", q.getClass());
            throw new RuntimeException("Query type "+q.getClass()+" is not supported!");
        }
        return root;
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
            root.addChild(clause.getOccur(), child);

            // if boolean => continue creating the tree
            if(clause.getQuery().getClass() == BooleanQuery.class) {
                child.setText(clause.getQuery().toString());
                createQueryTree(clause.getQuery(), child);

            // if term => end recursion here
            } else if(clause.getQuery().getClass() == TermQuery.class){
                child.setText(preprocessor.processTerm(clause.getQuery().toString()));
                child.setTerm(true);
            }
        }
    }
}
