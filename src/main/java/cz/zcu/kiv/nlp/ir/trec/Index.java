package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.core.*;
import cz.zcu.kiv.nlp.ir.trec.core.retrieval.BooleanRetrievalWithProgress;
import cz.zcu.kiv.nlp.ir.trec.core.retrieval.CosineSimilarityWithProgress;
import cz.zcu.kiv.nlp.ir.trec.core.retrieval.RetrievalWithProgress;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Preprocessor;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Stemmer;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Tokenizer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author tigi
 *
 * Třída reprezentující index.
 *
 * Tuto třídu doplňte tak aby implementovala rozhranní {@link Indexer} a {@link Searcher}.
 * Pokud potřebujete, přidejte další rozhraní, která tato třída implementujte nebo
 * přidejte metody do rozhraní {@link Indexer} a {@link Searcher}.
 *
 *
 */
public class Index implements Indexer, Searcher {

    private static Logger log = LoggerFactory.getLogger(Index.class);

    public static final int DEF_TOP_RESULT_COUNT = 50;

    /**
     * Inverted index for documents.
     */
    private InvertedIndex invertedIndex;

    /**
     * Preprocessor used to index documents.
     */
    private Preprocessor preprocessor;

    /**
     * Max number of results with top score returned by search. If < 0, then all results will be returned.
     */
    private int topResultCount;

    public Index(Tokenizer tokenizer, Stemmer stemmer, Set<String> stopwords) {
        this(tokenizer, stemmer, stopwords, true, true);
    }

    public Index(Tokenizer tokenizer, Stemmer stemmer, Set<String> stopwords, boolean useStemmer, boolean useStopWords) {
        this.preprocessor = new Preprocessor(tokenizer, stemmer, stopwords, useStemmer, useStopWords);
        invertedIndex = new InvertedIndex();
        topResultCount = DEF_TOP_RESULT_COUNT;
    }

    @Override
    public List<Result> search(String query) {
        try {
            log.debug("Parsing query \"{}\".", query);
            SearchQueryNode rootQuery = new QueryParser(preprocessor).parseQuery(query);
            if (rootQuery == null) {
                log.warn("Null query returned.");
                return null;
            }
            log.debug("Done");

            return rankedRetrieval(rootQuery);
        } catch (QueryNodeException ex) {
            log.error("Query node exception while searching the index: ", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Result> search(String query, SearchMode searchMode) throws QueryNodeException {
        log.debug("Parsing query \"{}\".", query);
        SearchQueryNode rootQuery = new QueryParser(preprocessor).parseQuery(query, searchMode);
        if (rootQuery == null) {
            log.warn("Null query returned.");
            return null;
        }
        log.debug("Done");

        log.debug("Preparing retrieval with progress");
        switch (searchMode) {
            case BOOLEAN:
                return booleanRetrieval(rootQuery);
            case RANKED:
                return rankedRetrieval(rootQuery);
            default:
                throw new RuntimeException("Unsupported search mode: "+searchMode);
        }
    }

    @Override
    public RetrievalWithProgress searchWithProgress(String query, SearchMode searchMode) throws QueryNodeException {
        log.debug("Parsing query \"{}\".", query);
        SearchQueryNode rootQuery = new QueryParser(preprocessor).parseQuery(query, searchMode);
        if (rootQuery == null) {
            log.warn("Null query returned.");
            return null;
        }
        log.debug("Done");

        log.debug("Preparing retrieval with progress");
        switch (searchMode) {
            case BOOLEAN:
                return prepareBooleanRetrievalWithProgress(rootQuery);
            case RANKED:
                return prepareRankedRetrievalWithProgress(rootQuery);
            default:
                throw new RuntimeException("Unsupported search mode: "+searchMode);
        }
    }

    @Override
    public void index(List<Document> documents) {
        double progress = 0;
        double progressStep = documents.isEmpty() ? 100 : 100.0 / documents.size();
        int progLimit = 10;
        log.debug("Indexing progress: 0");
        for(Document d : documents) {
            String dId = d.getId();
            String dText = d.getText();

            // check that the document isn't already indexed
            if (invertedIndex.getIndexedDocuments().contains(dId)) {
                throw new RuntimeException("Document with id "+dId+" is already indexed!");
            }

            // tokenize text and index document
            String[] tokens = preprocessor.processText(dText);

            invertedIndex.indexDocument(tokens, dId);

            progress += progressStep;
            if (progress > progLimit) {
                log.debug("Indexing progress: {}.", progLimit);
                progLimit+=10;
            }
        }

        log.debug("Re-calculating term IDF");
        invertedIndex.recalculateTermIdfs();

        log.debug("Re-calculating TF-IDF");
        invertedIndex.recalculateDocumentTfIdfs();
    }

    @Override
    public void index(Document document) {
        String dId = document.getId();
        String dText = document.getText();

        // check that the document isn't already indexed
        if (invertedIndex.getIndexedDocuments().contains(dId)) {
            throw new RuntimeException("Document with id "+dId+" is already indexed!");
        }

        // tokenize text and index document
        String[] tokens = preprocessor.processText(dText);

        invertedIndex.indexDocument(tokens, dId);
    }

    public void setTopResultCount(int topResultCount) {
        this.topResultCount = topResultCount;
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public InvertedIndex getInvertedIndex() {
        return invertedIndex;
    }

    /**
     * Returns the number of indexed documents.
     * @return Number of indexed documents.
     */
    public int getDocumentCount() {
        if (invertedIndex != null) {
            return invertedIndex.getDocumentCount();
        } else {
            return 0;
        }
    }

    /**
     * Internal method which performs boolean retrieval search.
     * @param rootQuery
     * @return
     */
    private List<Result> booleanRetrieval(SearchQueryNode rootQuery) {
        log.debug("Getting results for query.");

        // get list of postings to search
        log.trace("Getting list of postings to search.");
        List<Posting> postings = invertedIndex.getPostingsForQuery(rootQuery);
        if(postings.isEmpty()) {
            return new ArrayList<>();
        }

        // calculate similarity
        log.trace("Calculating similarity.");
        PriorityQueue<Result> resultQueue = prepareTopKQueue(postings.size());
        int progressLevel = 0;
        int docProcessed = 0;
        for(Posting p : postings) {
            ResultImpl r = new ResultImpl();
            r.setDocumentID(p.getDocumentId());
            r.setScore(1f);
            resultQueue.add(r);

            docProcessed++;
            if (100.0*docProcessed / postings.size() > progressLevel) {
                log.debug("{}% of documents processed.", progressLevel);
                progressLevel += 10;
            }
        }

        log.trace("Fetching results.");
        return getTopKResults(resultQueue, topResultCount);
    }

    /**
     * Internal method which performs ranked retrieval search.
     * @param queryRoot Root of term query.
     * @return List of results.
     */
    private List<Result> rankedRetrieval(SearchQueryNode queryRoot) {
        log.debug("Getting results for query.");

        // get list of terms in query
        log.trace("Extracting terms from query.");
        String[] terms = queryRoot.getTerms().toArray(new String[0]);

        // prepare similarity calculator
        log.trace("Initializing cosine similarity calculator.");
        SimilarityCalculator similarityCalculator = new CosineSimilarityCalculator(invertedIndex, terms);

        // calculate similarity
        log.trace("Calculating similarity.");
        final int docCount = invertedIndex.getDocumentCount();
        PriorityQueue<Result> resultQueue = prepareTopKQueue(docCount);
        int progressLevel = 0;
        int docProcessed = 0;
        for(String documentId : invertedIndex.getIndexedDocuments()) {
            double score = similarityCalculator.calculateScore(documentId);
            if (Math.abs(score - 0) > 0.001 ) {
                ResultImpl r = new ResultImpl();
                r.setDocumentID(documentId);
                r.setScore((float) score);
                resultQueue.add(r);
            }

            docProcessed++;
            if (100.0*docProcessed / docCount > progressLevel) {
                log.debug("{}% of documents processed.", progressLevel);
                progressLevel += 10;
            }
        }

        log.trace("Fetching results.");
        return getTopKResults(resultQueue, topResultCount);
    }

    private BooleanRetrievalWithProgress prepareBooleanRetrievalWithProgress(SearchQueryNode queryRoot) {
        log.debug("Getting results for boolean query.");

        log.trace("Getting list of postings to search.");
        List<Posting> postings = invertedIndex.getPostingsForQuery(queryRoot);
        if(postings.isEmpty()) {
            log.warn("No postings.");
            return null;
        }

        PriorityQueue<Result> resultQueue = prepareTopKQueue(postings.size());

        log.debug("Creating boolean retrieval object.");
        return new BooleanRetrievalWithProgress(postings, resultQueue);
    }

    private CosineSimilarityWithProgress prepareRankedRetrievalWithProgress(SearchQueryNode queryRoot) {
        log.debug("Preparing search query calculator.");

        // get list of terms in query
        log.trace("Extracting terms from query.");
        String[] terms = queryRoot.getTerms().toArray(new String[0]);

        // prepare similarity calculator
        log.trace("Initializing cosine similarity calculator.");
        SimilarityCalculator similarityCalculator = new CosineSimilarityCalculator(invertedIndex, terms);

        // calculate similarity
        log.trace("Creating similarity progress calculator.");
        PriorityQueue<Result> resultQueue = prepareTopKQueue(invertedIndex.getDocumentCount());
        return new CosineSimilarityWithProgress(invertedIndex.getIndexedDocuments(), resultQueue, similarityCalculator);
    }

    /**
     * Create result queue with descending comparator and initial capacity.
     * @param initialCapacity Initial capacity.
     * @return Prepared queue.
     */
    private PriorityQueue<Result> prepareTopKQueue(int initialCapacity)  {
        return new PriorityQueue<>(initialCapacity, (o1, o2) -> Float.compare(o2.getScore(), o1.getScore()));
    }

    /**
     * Pulls out tok K results from queue and returns them.
     *
     * @param queue Queue to pull results from.
     * @param k Max number of results to pull from query.
     * @return Results sorted by their score in descending order.
     */
    public List<Result> getTopKResults(PriorityQueue<Result> queue, int k)  {
        List<Result> results = new ArrayList<>();
        int max;
        if (k < 0) {
            max = queue.size();
        } else {
            max = Math.min(queue.size(), k);
        }
        for(int i = 0; i < max; i++) {
            ResultImpl res = (ResultImpl)queue.poll();
            res.setRank(i+1);
            results.add(res);
        }

        return results;
    }
}
