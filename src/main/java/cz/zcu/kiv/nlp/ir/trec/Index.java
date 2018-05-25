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
 */

public class Index implements Indexer, Searcher {

    public static final int DEF_TOP_RESULT_COUNT = 10;

    /**
     * Inverted index for documents.
     */
    private InvertedIndex invertedIndex;

    /**
     * Preprocessor used to index documents.
     */
    private Preprocessor preprocessor;

    /**
     * Max number of results with top score returned by search.
     */
    private int topResultCount;

    public Index(Tokenizer tokenizer, Stemmer stemmer, Set<String> stopwords) {
        this.preprocessor = new Preprocessor(tokenizer, stemmer, stopwords);
        invertedIndex = new InvertedIndex();
        topResultCount = DEF_TOP_RESULT_COUNT;
    }

    public void index(List<Document> documents) {
        for(Document d : documents) {
            String dId = d.getId();
            String dText = d.getText();

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

    private PriorityQueue<Result> prepareTopKQueue(int initialCapasity)  {
        return new PriorityQueue<>(initialCapasity, (o1, o2) -> {
            if (o1.getScore() > o2.getScore()) return -1;
            if (o1.getScore() == o2.getScore()) return 0;
            return 1;
        });
    }

    private List<Result> getTopKResults(PriorityQueue<Result> queue, int k)  {
        List<Result> results = new ArrayList<Result>();
        int max = Math.min(queue.size(), k);
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

    private List<Result> oldSearch(String query) {
        // tokenize query and stem
        String[] tokenizedQuery = preprocessor.processText(query, true, false);

        // prepare priority queue for results
        List<Result> results = new ArrayList<Result>();
        PriorityQueue<Result> resultQueue = prepareTopKQueue(invertedIndex.getDocumentCount());

        // compute query-document similarity for each document
        SimilarityCalculator similarityCalculator = new CosineSimilarityCalculator(invertedIndex);
        for(String documentId : invertedIndex.getIndexedDocuments()) {
            double score = similarityCalculator.calculateScore(tokenizedQuery, documentId);
            ResultImpl r = new ResultImpl();
            r.setDocumentID(documentId);
            r.setScore((float)score);
            resultQueue.add(r);
        }

        // select top K results
        for(int i = 0; i < topResultCount; i++) {
            ResultImpl res = (ResultImpl)resultQueue.poll();
            res.setRank(10 - i);
            results.add(res);
        }
        return results;
    }
}
