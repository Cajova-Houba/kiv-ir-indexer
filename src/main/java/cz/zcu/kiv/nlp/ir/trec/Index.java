package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.core.*;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Preprocessor;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Stemmer;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

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
        this.preprocessor = new Preprocessor(tokenizer, stemmer, stopwords);
        invertedIndex = new InvertedIndex();
        topResultCount = DEF_TOP_RESULT_COUNT;
    }

    public int getTopResultCount() {
        return topResultCount;
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

    @Override
    public void index(List<Document> documents) {
        for(Document d : documents) {
            String dId = d.getId();
            String dText = d.getText();

            // check that the document isn't already indexed
            if (invertedIndex.getIndexedDocuments().contains(dId)) {
                throw new RuntimeException("Document with id "+dId+" is already indexed!");
            }

            // tokenize text and index document
            String[] tokens = preprocessor.processText(dText, true, true);
            invertedIndex.indexDocument(tokens, dId);
        }
    }

    /**
     * Internal method which performs search using query tree.
     * @param queryRoot Root of query interpreted as a tree.
     * @return List of results.
     */
    private List<Result> getResultsForQuery(SearchQueryNode queryRoot) {
        // prepare similarity calculator
        SimilarityCalculator similarityCalculator = new CosineSimilarityCalculator(invertedIndex);

        // get list of postings to search
        List<Posting> postings = invertedIndex.getPostingsForQuery(queryRoot);
        if(postings.isEmpty()) {
            return new ArrayList<>();
        }

        // get list of terms in query
        String[] terms = queryRoot.getTerms().toArray(new String[0]);

        // calculate similarity
        PriorityQueue<Result> resultQueue = prepareTopKQueue(postings.size());
        for(Posting p : postings) {
            double score = similarityCalculator.calculateScore(terms, p.getDocumentId());
            ResultImpl r = new ResultImpl();
            r.setDocumentID(p.getDocumentId());
            r.setScore((float)score);
            resultQueue.add(r);
        }

        return getTopKResults(resultQueue, topResultCount);
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
    private List<Result> getTopKResults(PriorityQueue<Result> queue, int k)  {
        List<Result> results = new ArrayList<>();
        int max;
        if (k < 0) {
            max = queue.size();
        } else {
            max = Math.min(queue.size(), k);
        }
        for(int i = 0; i < max; i++) {
            ResultImpl res = (ResultImpl)queue.poll();
            res.setRank(max - i);
            results.add(res);
        }

        return results;
    }

    @Override
    public List<Result> search(String query) {
        SearchQueryNode rootQuery = new QueryParser(preprocessor).parseQuery(query);

        return getResultsForQuery(rootQuery);
    }
}
