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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * Parses query for RANKED retrieval.
     * @param query String query to be parsed.
     * @return Query node.
     * @throws QueryNodeException
     */
    public SearchQueryNode parseQuery(String query) throws QueryNodeException {
        return this.parseQuery(query, SearchMode.RANKED);
    }

    /**
     * Parse string query and create SearchQuery object.
     * @param query Query to be parsed.
     * @param searchMode Search mode used to interpret query.
     * @return Parsed query.
     */
    public SearchQueryNode parseQuery(String query, SearchMode searchMode) throws QueryNodeException {
        log.debug("Preparing to parse query \"{}\" with default field \"{}\". Search mode: {}.", query, defaultField, searchMode);

        SearchQueryNode root = new SearchQueryNode();
        switch (searchMode) {
            case RANKED:
                log.debug("Paring query for ranked retrieval.");
                parseNormalQuery(query, root);
                break;

            case BOOLEAN:
                log.debug("Parsing query for boolean retrieval.");
                parseBooleanQuery(query, root);
                break;

            default:
                throw new RuntimeException("Unsupported search mode: "+searchMode);
        }
        return root;
    }

    /**
     * Tokenizes query and creates structure of OR (token1 token2 ...)
     * @param query Query to be parsed.
     * @param root Root query node.
     */
    private void parseNormalQuery(String query, SearchQueryNode root) {
        root.setTerm(false);

        // tokenize query and create one query node for each term
        // then put them under one OR operator so that all necessary
        // Postings are collected
        String[] terms = preprocessor.processText(query);
        List<SearchQueryNode> termNodes = new ArrayList<>(terms.length);
        for(String queryTerm : terms) {
            SearchQueryNode t = new SearchQueryNode();
            t.setTerm(true);
            t.setText(queryTerm);
            termNodes.add(t);
        }

        root.getChildren().put(BooleanClause.Occur.SHOULD, termNodes);
    }

    private void parseBooleanQuery(String query, SearchQueryNode root) throws QueryNodeException {
        Query q =  parser.parse(query, "");
        createQueryTree(q, root);

        checkAloneNot(root, query);
    }

    /**
     * Checks whether the boolean query is in form of NOT [term] and throws exception if it is.
     * @param root Root of the query.
     * @param query Query to be used in error message.
     */
    private void checkAloneNot(SearchQueryNode root, String query) {
        if (!root.isTerm() && root.getChildren().size() == 1) {
            Map.Entry<BooleanClause.Occur, List<SearchQueryNode>> entry = root.getChildren().entrySet().iterator().next();
            BooleanClause.Occur occur = entry.getKey();
            if (occur.equals(BooleanClause.Occur.MUST_NOT) && entry.getValue().size() < 2 && entry.getValue().get(0).isTerm()) {
                throw new RuntimeException("Alone operator NOT is not allowed, query: "+query);
            }
        }
    }

    /**
     * Recursive method which creates tree representation of search query.
     * @param luceneQuery Lucene query.
     * @param root Root to add child to.
     */
    private void createQueryTree(Query luceneQuery, SearchQueryNode root) {

        if (luceneQuery instanceof TermQuery) {
            root.setTerm(true);
            root.setText(((TermQuery)luceneQuery).getTerm().text());
            return;
        }

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
