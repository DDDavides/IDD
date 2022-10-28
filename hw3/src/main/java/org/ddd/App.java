package org.ddd;

import org.ddd.concurrency.searcher.MultithreadIndexSearcher;

import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        MultithreadIndexSearcher searcher;
        try {
            System.out.println("Aperto il reader\n");
            searcher = new MultithreadIndexSearcher(Utility.INDEX_PATH);

            //per ogni termine della query cerca tutte le colonne che fanno hit
            MergeList ml = new MergeList(searcher);
            String[] stringhe = {"katab","naktubu","taktubna","taktubu","taktubāni","taktubīna","taktubūna","write","yaktubna","yaktubu","yaktubāni","yaktubūna","ʼaktubu"};

            System.out.println("Effettuo la query\n");
            Thread t = new Thread(() -> {
                String[] animation = {"", ".", "..", "..."};
                int i = 0;
                while (true) {

                    System.out.print("\rCercando" + animation[i]);
                    System.out.flush();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        return;
                    }
                    i++;
                    i = i % animation.length;
                }
            });
            t.start();
            List<String> topKOverlapMerge =  ml.topKOverlapMerge(5, new ArrayList<>(List.of(stringhe)));
            t.interrupt();
            System.out.println("\r" + topKOverlapMerge);




        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
