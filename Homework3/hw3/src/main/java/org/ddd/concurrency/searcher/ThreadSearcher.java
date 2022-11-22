package org.ddd.concurrency.searcher;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class ThreadSearcher extends Thread {
    private IndexSearcher searcher;
    private Query query;
    private List<Document> value;

    public ThreadSearcher(Query query, IndexSearcher searcher) {
        this.query = query;
        this.searcher = searcher;
        this.value = new ArrayList<>();
    }

    public List<Document> getValue() {
        return this.value;
    }

    @Override
    public void run() {
        TotalHitCountCollector collector = new TotalHitCountCollector();
        try {
            searcher.search(query, collector);
            if(collector.getTotalHits() <= 0) return;

            TopDocs topDocs = searcher.search(query, collector.getTotalHits());

            for (ScoreDoc sc : topDocs.scoreDocs) {
                this.value.add(searcher.doc(sc.doc));
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

}