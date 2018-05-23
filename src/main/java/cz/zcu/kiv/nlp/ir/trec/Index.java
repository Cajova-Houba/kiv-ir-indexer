package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Stemmer;
import cz.zcu.kiv.nlp.ir.trec.preprocess.Tokenizer;

import java.util.*;

/**
 * @author tigi
 */

public class Index implements Indexer, Searcher {

    public static final int DEF_TOP_RESULT_COUNT = 10;

    /**
     * Inverted index which maps words to its occurrences in documents.
     * word -> (doc1Id -> # of occurrences, doc2Id -> # of occurrences, ...).
     */
    private Map<String, Map<String, Integer>> invertedIndex;

    /**
     * Documents mapped by their ids.
     */
    // maybe not needed
    private Map<String, Document> documentMap;

    /**
     * Ids of indexed documents.
     */
    private Set<String> documentIds;

    /**
     * Number of document indexes.
     */
    private int documentCount;

    /**
     * Tokenizer to be used to tokenize input text.
     */
    private Tokenizer tokenizer;

    /**
     * Stemmer to e used to stem tokenized text (and tokens in search query).
     */
    private Stemmer stemmer;

    /**
     * Stopwords. Only lowercase without accents should be used. Tokens in those set will not be indexed.
     */
    private Set<String> stopwords;

    /**
     * Max number of results with top score returned by search.
     */
    private int topResultCount;

    public Index(Tokenizer tokenizer, Stemmer stemmer, Set<String> stopwords) {
        this.tokenizer = tokenizer;
        this.stemmer = stemmer;
        this.stopwords = stopwords;
        invertedIndex = new HashMap<String, Map<String, Integer>>();
        documentMap = new HashMap<String, Document>();
        documentIds = new HashSet<String>();
        topResultCount = DEF_TOP_RESULT_COUNT;
    }

    public void index(List<Document> documents) {
        documentCount = documents.size();
        for(Document d : documents) {
            String dId = d.getId();
            String dText = d.getText();
            documentIds.add(dId);

            // tokenize text and index document
            String[] tokens = tokenizer.tokenize(dText);
            for(String token : tokens) {
                // skip stopwords
                if (isStopword(token)) {
                    continue;
                }
                String stem = stemmer.stem(token);

                // create new set for term postings
                if (!invertedIndex.containsKey(stem)) {
                    invertedIndex.put(stem, new HashMap<String, Integer>());
                    invertedIndex.get(stem).put(dId, 1);

                // add term occurrence for another doc
                } else if (!invertedIndex.get(stem).containsKey(dId)) {
                    invertedIndex.get(stem).put(dId, 1);

                // increment term occurrence
                } else {
                    int occurrence = invertedIndex.get(stem).get(dId);
                    invertedIndex.get(stem).put(dId, occurrence+1);
                }
            }
        }
    }

    public List<Result> search(String query) {
        // tokenize query and stem
        String[] tokenizedQuery = tokenizer.tokenize(query);
        for(int i = 0; i < tokenizedQuery.length; i++) {
            tokenizedQuery[i] = stemmer.stem(tokenizedQuery[i]);
        }

        // prepare priority queue for results
        List<Result> results = new ArrayList<Result>();
        PriorityQueue<Result> resultQueue = new PriorityQueue<Result>(documentCount, new Comparator<Result>() {
            public int compare(Result o1, Result o2) {
                if (o1.getScore() > o2.getScore()) return -1;
                if (o1.getScore() == o2.getScore()) return 0;
                return 1;
            }
        }) ;

        // compute query-document similarity for each document
        SimilarityCalculator similarityCalculator = new CosineSimilarityCalculator(invertedIndex, documentCount);
        for(String documentId : documentIds) {
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
            results.add(resultQueue.poll());
        }
        return results;
    }

    /**
     * Returns true if the token is a stopword.
     * @param token Token.
     * @return True if the token is a stopword.
     */
    private boolean isStopword(String token) {
        if (stopwords == null || stopwords.isEmpty()) {
            return false;
        }

        return stopwords.contains(token);
    }
}
