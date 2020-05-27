package cz.zcu.kiv.nlp.trec.core;

import cz.zcu.kiv.nlp.ir.trec.core.QueryParser;
import cz.zcu.kiv.nlp.ir.trec.core.SearchMode;
import cz.zcu.kiv.nlp.ir.trec.core.SearchQueryNode;
import cz.zcu.kiv.nlp.ir.trec.preprocess.*;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.BooleanClause;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class QueryParserTest {

    private Preprocessor preproc;

    @Before
    public void setUp() {
        Tokenizer t = new AdvancedTokenizer();
        Stemmer s = new CzechStemmerAgressive();
        preproc= new Preprocessor(t, s, Collections.emptySet(), true, false);
    }

    @Test
    public void testParseRankedQuery() throws QueryNodeException {
        String query =  "irrelevant auto";
        int expectedTerms = 2;
        SearchQueryNode queryRoot =  new QueryParser(preproc).parseQuery(query, SearchMode.RANKED);

        assertEquals("Wrong number of childer!", 1, queryRoot.getChildren().size());
        Map.Entry<BooleanClause.Occur, List<SearchQueryNode>> entry = queryRoot.getChildren().entrySet().iterator().next();
        assertEquals("Wrong occur!", BooleanClause.Occur.SHOULD, entry.getKey());
        assertEquals("Wrong number of terms!", expectedTerms, entry.getValue().size());
    }
}
