package org.ddd;

import org.ddd.concurrency.MultithreadIndexSearcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {


    public static void main(String[] args) throws IOException {
//        Path path = Paths.get(Utility.INDEX_PATH);
        MultithreadIndexSearcher searcher;
//        try (Directory directory = FSDirectory.open(path)) {
            System.out.println("Aperto l'indice\n");
        /*IndexReader reader = DirectoryReader.open(directory)*/
            try {
                System.out.println("Aperto il reader\n");
                searcher = new MultithreadIndexSearcher(Utility.INDEX_PATH);
                //per ogni termine della query cerca tutte le colonne che fanno hit
                MergeList ml = new MergeList(searcher);
                String[] stringhe = {"katab","naktubu","taktubna","taktubu","taktubāni","taktubīna","taktubūna","write","yaktubna","yaktubu","yaktubāni","yaktubūna","ʼaktubu"};
                System.out.println("Effettuo la query\n");
                System.out.println(ml.topKOverlapMerge(5, new ArrayList<>(List.of(stringhe))));
            } catch (Exception e) {
                throw new RuntimeException(e);
//            } finally {
//                directory.close();
//            }
        }
    }
}
