package org.ddd;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Searcher {

    private final String indexPath;

    private FSDirectory directory;

    public Searcher(String indexPath){
        this.indexPath = indexPath;
    }

    public List<Document> search(String element) throws IOException {
        Path path = Paths.get(indexPath);
        IndexSearcher searcher;
        List<Document> documents = new ArrayList<>();
        try (Directory directory = FSDirectory.open(path)) {
            try (IndexReader reader = DirectoryReader.open(directory)) {
                searcher = new IndexSearcher(reader);
                //per ogni termine della query cerca tutte le colonne che fanno hit
                TotalHitCountCollector collector = new TotalHitCountCollector();
                BooleanQuery booleanQuery = new BooleanQuery.Builder()
                        .add(new PhraseQuery("colonna", element), BooleanClause.Occur.MUST).
                        build();
                searcher.search(booleanQuery, collector);
                TopDocs docs = searcher.search(booleanQuery, collector.getTotalHits());
                //popola la mappa con le colonne ritornate
                for(int i = 0; i < docs.scoreDocs.length; i++) {
                    ScoreDoc scoreDoc = docs.scoreDocs[i];
                    Document doc = searcher.doc(scoreDoc.doc);
                    documents.add(doc);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                directory.close();
            }
        }
        return documents;
    }
}
