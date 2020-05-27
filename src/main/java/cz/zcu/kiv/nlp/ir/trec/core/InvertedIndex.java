package cz.zcu.kiv.nlp.ir.trec.core;

import org.apache.lucene.search.BooleanClause;

import java.io.Serializable;
import java.util.*;

/**
 * Wrapper for inverted index.
 */
public class InvertedIndex implements Serializable{

    /**
     * Index which maps terms to postings.
     * Each posting is mapped to document id for easier retrieval.
     */
    private Map<String, Map<String,Posting>> invertedIndex;

    /**
     * Ids of indexed documents.
     */
    private Set<String> indexedDocuments;

    /**
     * Inverted document frequencies of terms.
     */
    private Map<String, Double> termIdf;

    /**
     * Size of TF-IDF vectors.
     *
     * docId -> size
     */
    private Map<String, Double> documentTfIdfNorms;

    /**
     * Comparator which compares postings by document id hash.
     * Must be serializable.
     */
    private Comparator<Posting> postingComparator;

    public InvertedIndex() {
        invertedIndex = new HashMap<>();
        indexedDocuments = new HashSet<>();
        postingComparator = new PostingsComparator();
        termIdf = new HashMap<>();
        documentTfIdfNorms = new HashMap<>();
    }

    /**
     * Adds document to index.
     * @param tokens Text of the document to be indexed represented as preprocessed tokens.
     * @param documentId Id of the document to be indexed.
     */
    public void indexDocument(String[] tokens, String documentId) {
        indexedDocuments.add(documentId);

        for(String token : tokens) {
            Map<String, Posting> postingMap;
            if (!invertedIndex.containsKey(token)) {
                postingMap = new HashMap<>();
                invertedIndex.put(token, postingMap);
            } else {
                postingMap = invertedIndex.get(token);
            }

            if (!postingMap.containsKey(documentId)) {
                postingMap.put(documentId, new Posting(documentId));
            } else {
                postingMap.get(documentId).incrementTermFrequency();
            }
        }
    }

    /**
     * Recalculates inversed DF of terms.
     */
    public void recalculateTermIdfs() {
        for(String term : invertedIndex.keySet()) {
            double idf = invertedIndex.get(term).size();
            idf = Math.log10(getDocumentCount() / idf);

            termIdf.put(term, idf);
        }
    }

    /**
     * Recalculates TF-IDF for all terms and documents.
     * Assumes term IDF was already calculated.
     */
    public void recalculateDocumentTfIdfs() {
        for(String term : invertedIndex.keySet()) {
            for(Posting p : invertedIndex.get(term).values()) {
                p.recalculateTfIdf(termIdf.get(term));

                // add tf-idf of current term and document to document norm
                if (!documentTfIdfNorms.containsKey(p.getDocumentId())) {
                    documentTfIdfNorms.put(p.getDocumentId(), 0.0);
                }

                double currentTfIdf = documentTfIdfNorms.get(p.getDocumentId());
                documentTfIdfNorms.put(p.getDocumentId(),currentTfIdf + p.getTfIdf()*p.getTfIdf());
            }
        }

        // sqrt(sumsqr) for each document
        for(Map.Entry<String, Double> entry : documentTfIdfNorms.entrySet()) {
            double tfIdfNorm = entry.getValue();
            tfIdfNorm = Math.sqrt(tfIdfNorm);
            documentTfIdfNorms.put(entry.getKey(), tfIdfNorm);
        }
    }

    /**
     * Returns tf-idf for given term-document combination.
     * @param term Term.
     * @param docId Id of document.
     * @return TF-IDF for given document-term or 0 if such combination is not indexed.
     */
    public double getTfIdfOfTermInDocument(String term, String docId) {
        if (invertedIndex.containsKey(term) && invertedIndex.get(term).containsKey(docId)) {
            return invertedIndex.get(term).get(docId).getTfIdf();
        } else {
            return 0;
        }
    }

    /**
     * Returns the
     * @param docId
     * @return
     */
    public double getTfIdfNormForDocument(String docId) {
        if (!documentTfIdfNorms.containsKey(docId)) {
            return 0.0;
        }

        return documentTfIdfNorms.get(docId);
    }

    /**
     * Returns TF-IDF vector for given document.
     * @param documentId Id of document.
     * @return term -> TF-IDF map.
     */
    public Map<String, Double> getDocumentTfIdf(String documentId) {
        if (!indexedDocuments.contains(documentId)) {
            return Collections.emptyMap();
        }
        Map<String, Double> docTfIdf = new HashMap<>();


        for(String term : invertedIndex.keySet()) {
            if (invertedIndex.get(term).containsKey(documentId)) {
                docTfIdf.put(term, invertedIndex.get(term).get(documentId).getTfIdf());
            }
        }

        return docTfIdf;
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
     *
     * @param term Term.
     * @return
     */
    public List<Posting> getPostingsForTerm(String term) {
        if (!invertedIndex.containsKey(term)) {
            return new ArrayList<>();
        } else {
            List<Posting> postings = new ArrayList<>(invertedIndex.get(term).values());
            postings.sort(postingComparator);

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
        return getPostingsForQueryRec(rootQuery, false);
    }

    /**
     * Returns IDF of given term.
     *
     * @param term Term.
     * @return IDF of given term or 0 if the term is not indexed.
     */
    public double idf(String term) {
        if (termIdf.containsKey(term)) {
            return termIdf.get(term);
        } else {
            return 0;
        }
    }

    /**
     * Recursion method for getting postings for queries.
     * @param node
     * @param notClause If node is term and this is true, all postings except the ones of this term will be returned.
     * @return
     */
    public List<Posting> getPostingsForQueryRec(SearchQueryNode node, boolean notClause) {
        // node is term
        if (node.isTerm()) {
            if (notClause) {
                // return posting of all but this term
                String notTerm = node.getText();

                // first get all unique postings except the notTerm
                Set<Posting> allPostings = new HashSet<>();
                for(String term : invertedIndex.keySet()) {
                    if (!term.equals(notTerm)) {
                        allPostings.addAll(invertedIndex.get(term).values());
                    }
                }

                // now remove all postings which are relevant to notTerm
                if (invertedIndex.containsKey(notTerm)) {
                    for(Posting p : invertedIndex.get(notTerm).values()) {
                        allPostings.remove(p);
                    }
                }

                // convert set to list and sort
                List<Posting> postings = new ArrayList<>(allPostings);
                postings.sort(postingComparator);
                return postings;

            } else {
                return getPostingsForTerm(node.getText());
            }

        // rootQuery represents root of boolean query tree, perform intersection of posting lists
        } else {
            Collection<SearchQueryNode> childQuery;
            List<Posting> res = new ArrayList<>();
            for(BooleanClause.Occur occurrence : node.getChildren().keySet()) {
                childQuery = node.getChildren().get(occurrence);
                switch (occurrence) {

                    // AND
                    case MUST:
                        for(SearchQueryNode child : childQuery) {
                            res = andIntersect(res, getPostingsForQueryRec(child, false));
                        }
                        break;

                    // OR
                    case SHOULD:
                        for(SearchQueryNode child : childQuery) {
                            res = orIntersect(res, getPostingsForQueryRec(child, false));
                        }
                        break;

                        // NOT
                    case MUST_NOT:
                        for(SearchQueryNode child : childQuery) {
                            res = notIntersect(res, getPostingsForQueryRec(child, true));
                        }
                        break;
                }
            }

            return res;
        }
    }

    /**
     * Intersect two lists so that none of them will contain postings of given term.
     * Basically: resultList = sourceList \ index[notTem].
     *
     * @param postingList1 Result list.
     * @param postingList2 Source list. It is expected that this list already contains NOT [term] postings
     *                     so the actual intersection is AND (NOT [term]).
     * @return List of postings.
     */
    public List<Posting> notIntersect(List<Posting> postingList1, List<Posting> postingList2) {
        if (postingList1.isEmpty() && postingList2.isEmpty()) {
            return new ArrayList<>();
        } else if (postingList2.isEmpty()) {
            return postingList1;
        } else if (postingList1.isEmpty()) {
            return postingList2;
        }

        List<Posting> res = new ArrayList<>();

        // lists are not empty, take items from source lists which are also in result list
        for(Posting p : postingList2) {
            if (postingList1.contains(p)) {
                res.add(p);
            }
        }
        return res;
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

        // check for 'first timers' where at least one of the provided lists is empty.
        List<Posting> resultForEmptyPosting = checkEmptyPostingsLists(postingList1, postingList2);
        if (resultForEmptyPosting != null) {
            return resultForEmptyPosting;
        }

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

        // check for 'first timers' where at least one of the provided lists is empty.
        List<Posting> resultForEmptyPosting = checkEmptyPostingsLists(postingList1, postingList2);
        if (resultForEmptyPosting != null) {
            return resultForEmptyPosting;
        }

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
            res.add(postingList2.get(p2Cur));
            p2Cur++;
        }

        return res;
    }

    private List<Posting> checkEmptyPostingsLists(List<Posting> postings1, List<Posting> postings2) {
        if (postings1.isEmpty() && postings2.isEmpty()) {
            return new ArrayList<>();
        } else if (postings1.isEmpty()) {
            return new ArrayList<>(postings2);
        } else if (postings2.isEmpty()) {
            return new ArrayList<>(postings1);
        }

        return null;
    }

    /**
     * Postings comparator. Serializable so that it can be saved to file.
     */
    private class PostingsComparator implements Comparator<Posting>, Serializable {
        @Override
        public int compare(Posting o1, Posting o2) {
            if (o1.documentIdHash() > o2.documentIdHash()) return 1;
            if (o1.documentIdHash() == o2.documentIdHash()) return 0;
            return -1;
        }
    }
}
