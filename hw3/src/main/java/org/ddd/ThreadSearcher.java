package org.ddd;

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

    public ThreadSearcher(Query query, Path ...indexes) throws IOException {
        this.query = query;
        IndexReader[] readers = new IndexReader[indexes.length];
        for(int i = 0; i < indexes.length; i++) {
            readers[i] = DirectoryReader.open(FSDirectory.open(indexes[i]));
        }
        MultiReader multiReader = new MultiReader(readers);
        this.searcher = new IndexSearcher(multiReader);

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
            System.out.println("hits = " + collector.getTotalHits());
            TopDocs topDocs = searcher.search(query, collector.getTotalHits());

            for(int i = 0; i < topDocs.scoreDocs.length; i++) {
                Document doc = searcher.doc(topDocs.scoreDocs[i].doc);
                System.out.println(doc.get("tabella"));
                System.out.println(doc.get("contesto"));

                this.value.add(doc);
            }
        } catch (Exception e)
        {
            System.out.println("Errore in ricerca!");
        }

    }

}