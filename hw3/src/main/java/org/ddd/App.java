package org.ddd;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {

    private static String indexPath = Utility.INDEX_PATH;

    public static void main(String[] args) throws IOException {
        Path path = Paths.get(indexPath);
        IndexSearcher searcher;
        try (Directory directory = FSDirectory.open(path)) {
            System.out.println("Aperto l'indice\n");
            try (IndexReader reader = DirectoryReader.open(directory)) {
                System.out.println("Aperto il reader\n");
                searcher = new IndexSearcher(reader);
                //per ogni termine della query cerca tutte le colonne che fanno hit
                MergeList ml = new MergeList(searcher);
                String[] stringhe = {"katab","naktubu","taktubna","taktubu","taktubāni","taktubīna","taktubūna","write","yaktubna","yaktubu","yaktubāni","yaktubūna","ʼaktubu"};
                System.out.println("Effettuo la query\n");
                System.out.println(ml.topKOverlapMerge(5, new ArrayList<>(List.of(stringhe))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                directory.close();
            }
        }
    }
}
