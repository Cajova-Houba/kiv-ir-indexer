package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.core.SearchMode;
import cz.zcu.kiv.nlp.ir.trec.core.retrieval.RetrievalWithProgress;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

import java.util.List;

/**
 * Created by Tigi on 6.1.2015.
 *
 * Rozhraní umožňující vyhledávat v zaindexovaných dokumentech.
 *
 * Pokud potřebujete, můžete přidat další metody k implementaci, ale neměňte signaturu
 * již existující metody search.
 *
 * Metodu search implementujte ve tříde {@link Index}
 */
public interface Searcher {
    List<Result> search(String query);

    /**
     * Perform search in given search mode.
     *
     * @param query
     * @param searchMode
     * @return
     */
    List<Result> search(String query, SearchMode searchMode) throws QueryNodeException;

    /**
     * Returns a retrieval object which allows tracking progress.
     *
     * Results needs to be extracted afterwards.
     *
     * @param query
     */
    RetrievalWithProgress searchWithProgress(String query, SearchMode searchMode) throws QueryNodeException;
}
