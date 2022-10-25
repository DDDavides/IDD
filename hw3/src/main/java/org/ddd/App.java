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

/**
 * Hello world!
 *
 */
public class App {

    private static String indexPath;

    public static void main(String[] args) throws IOException {
        Path path = Paths.get(indexPath);
        IndexSearcher searcher;
        try (Directory directory = FSDirectory.open(path)) {
            try (IndexReader reader = DirectoryReader.open(directory)) {
                searcher = new IndexSearcher(reader);
                //per ogni termine della query cerca tutte le colonne che fanno hit
                MergeList ml = new MergeList(searcher);
                ml.topKOverlapMerge(5, new ArrayList<>());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                directory.close();
            }
        }
    }
}
