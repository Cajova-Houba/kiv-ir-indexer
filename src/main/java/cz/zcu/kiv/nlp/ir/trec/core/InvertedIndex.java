package cz.zcu.kiv.nlp.ir.trec.core;

import org.apache.lucene.search.BooleanClause;

import java.util.*;

/**
 * Wrapper for inverted index.
 */
public class InvertedIndex {

    /**
     * Index which maps terms to postings.
     * Each posting is mapped to document id for easier retrieval.
     */
    private Map<String, Map<String,Posting>> invertedIndex;

    /**
     * Ids of indexed documents.
     */
    private Set<String> indexedDocuments;

    public InvertedIndex() {
        invertedIndex = new HashMap<String, Map<String,Posting>>();
        indexedDocuments = new HashSet<String>();
    }

    /**
     * Adds document to index.
     * @param tokens Text of the document to be indexed represented as preprocessed tokens.
     * @param documentId Id of the document to be indexed.
     */
    public void indexDocument(String[] tokens, String documentId) {
        indexedDocuments.add(documentId);

        for(String token : tokens) {
            // create new set for term postings
            if (!invertedIndex.containsKey(token)) {
                invertedIndex.put(token, new HashMap<String, Posting>());
                invertedIndex.get(token).put(documentId, new Posting(documentId));

                // add term occurrence for another doc
            } else if (!invertedIndex.get(token).containsKey(documentId)) {
                invertedIndex.get(token).put(documentId, new Posting(documentId));

                // increment term occurrence
            } else {
                invertedIndex.get(token).get(documentId).incrementTermFrequency();
            }
        }
    }

    /**
     * Returns number of indexed documents.
     * @return
     */
    public int getDocumentCount() {
        return indexedDocuments.size();
    }

    /**
     * Get document frequency of one term.
     * @param term Term.
     * @return Document frequency of one term.
     */
    public int documentFrequency(String term) {
        if (!invertedIndex.containsKey(term)) {
            return 0;
        }

        return invertedIndex.get(term).size();
    }

    /**
     * Get term frequency of term in document.
     * @param term Term.
     * @param documentId Document.
     * @return Term frequency.
     */
    public int getTermFrequency(String term, String documentId) {
        if (!invertedIndex.containsKey(term) || !invertedIndex.get(term).containsKey(documentId)) {
            return 0;
        }
        return invertedIndex.get(term).get(documentId).getTermFrequency();
    }

    public Set<String> getIndexedDocuments() {
        return indexedDocuments;
    }

    /**
     * Returns postings list for a term. The list will be sorted by document id hash in ascending order.
     * @param term Term.
     * @return
     */
    public List<Posting> getPostingsForTerm(String term) {
        if (!invertedIndex.containsKey(term)) {
            return new ArrayList<>();
        } else {
            List<Posting> postings = new ArrayList<>(invertedIndex.get(term).values());
            postings.sort((o1, o2) -> {
                if (o1.documentIdHash() > o2.documentIdHash()) return 1;
                if (o1.documentIdHash() == o2.documentIdHash()) return 0;
                return -1;
            });

            return postings;
        }
    }

    /**
     * Gets the posting list that should be used for query. In case of term, simple postings list from
     * inverted index is returned. Otherwise intersection of more postings list is performed and the result
     * is returned.
     *
     * This method uses recursion when intersecting postings lists.
     *
     * @param rootQuery Node which represents root of the query.
     * @return
     */
    public List<Posting> getPostingsForQuery(SearchQueryNode rootQuery) {
        // query is just a term
        if (rootQuery.isTerm()) {
            return  getPostingsForTerm(rootQuery.getText());
        } else {
            // rootQuery represents root of boolean query tree, perform intersection of posting lists
            Collection<SearchQueryNode> childQuery;
            List<Posting> res = new ArrayList<>();
            for(BooleanClause.Occur occurrence : rootQuery.getChildren().keySet()) {
                childQuery = rootQuery.getChildren().get(occurrence);
                switch (occurrence) {

                    // AND
                    case MUST:
                        for(SearchQueryNode child : childQuery) {
                            res = andIntersect(res, getPostingsForQuery(child));
                        }
                        break;

                    // OR
                    case SHOULD:
                        for(SearchQueryNode child : childQuery) {
                            res = orIntersect(res, getPostingsForQuery(child));
                        }
                    // NOT
                    case MUST_NOT:
                        break;
                }
            }

            return res;
        }
    }

    /**
     * Performs AND intersection over two posting lists and returns result.
     * Both lists are expected to be sorted by document id hash.
     *
     * @param postingList1 Posting list 1.
     * @param postingList2 Posting list 2.
     * @return AND intersection of two posting lists.
     */
    public List<Posting> andIntersect(List<Posting> postingList1, List<Posting> postingList2) {
        int p1Cur = 0;
        int p2Cur = 0;
        List<Posting> res = new ArrayList<>();

        while (p1Cur < postingList1.size() && p2Cur < postingList2.size()) {
            int h1 = postingList1.get(p1Cur).documentIdHash();
            int h2 = postingList2.get(p2Cur).documentIdHash();
            if (h1 == h2) {
                res.add(postingList1.get(p1Cur));
                p1Cur++;
                p2Cur++;
            } else if (h1 < h2) {
                p1Cur++;
            } else {
                p2Cur++;
            }
        }
        return res;
    }

    /**
     * Performs OR intersection over two posting lists and returns result.
     * Both lists are expected to be sorted by document id hash.
     *
     * @param postingList1 Posting list 1.
     * @param postingList2 Posting list 2.
     * @return OR intersection of two posting lists.
     */
    public List<Posting> orIntersect(List<Posting> postingList1, List<Posting> postingList2) {
        int p1Cur = 0;
        int p2Cur = 0;
        List<Posting> res = new ArrayList<>();

        // merge lists together while sorting them
        while (p1Cur < postingList1.size() && p2Cur < postingList2.size()) {
            int h1 = postingList1.get(p1Cur).documentIdHash();
            int h2 = postingList2.get(p2Cur).documentIdHash();
            if (h1 == h2) {
                res.add(postingList1.get(p1Cur));
                p1Cur++;
                p2Cur++;
            } else if (h1 < h2) {
                res.add(postingList1.get(p1Cur));
                p1Cur++;
            } else {
                res.add(postingList2.get(p2Cur));
                p2Cur++;
            }
        }

        // add rest of the postings
        while (p1Cur < postingList1.size()) {
            res.add(postingList1.get(p1Cur));
            p1Cur++;
        }
        while (p2Cur < postingList2.size()) {
            res.add(postingList1.get(p2Cur));
            p2Cur++;
        }

        return res;
    }
}
