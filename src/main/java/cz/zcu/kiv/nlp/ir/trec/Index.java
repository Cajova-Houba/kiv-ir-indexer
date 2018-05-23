package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.core.CosineSimilarityCalculator;
import cz.zcu.kiv.nlp.ir.trec.core.InvertedIndex;
import cz.zcu.kiv.nlp.ir.trec.core.SimilarityCalculator;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Preprocessor;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Stemmer;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Tokenizer;

import java.util.*;

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

    public List<Result> search(String query) {
        // tokenize query and stem
        String[] tokenizedQuery = preprocessor.processText(query, true, false);

        // prepare priority queue for results
        List<Result> results = new ArrayList<Result>();
        PriorityQueue<Result> resultQueue = new PriorityQueue<Result>(invertedIndex.getDocumentCount(), new Comparator<Result>() {
            public int compare(Result o1, Result o2) {
                if (o1.getScore() > o2.getScore()) return -1;
                if (o1.getScore() == o2.getScore()) return 0;
                return 1;
            }
        }) ;

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
