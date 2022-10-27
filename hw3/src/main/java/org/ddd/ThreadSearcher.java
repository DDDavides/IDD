package org.ddd;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.QueryTimeout;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Lambda;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ThreadSearcher extends Thread {
    private IndexSearcher searcher;
    private Query query;
    private List<Document> result;

    public ThreadSearcher(IndexSearcher searcher, Query query) {
        this.searcher = searcher;
        this.query = query;
        this.result = new ArrayList<>();
    }
    public List<Document> getResult() {
        return this.result;
    }
    @Override
    public void run() {
//        TotalHitCountCollector collector = new TotalHitCountCollector();
        System.out.println("Eseguo la Query:");
        try {
//            searcher.search(query, collector);
//            System.out.println("hits = " + collector.getTotalHits());
            TopDocs topDocs = searcher.search(query, 1000000);
            for(ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
//                System.out.println(doc.get("tabella"));
                this.result.add(doc);

            }
        } catch (Exception e)
        {
            System.out.println("Errore in ricerca!");
        }

    }

}