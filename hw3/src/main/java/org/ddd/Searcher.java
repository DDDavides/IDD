package org.ddd;

import org.ddd.concurrency.LoadingThread;
import org.ddd.concurrency.searcher.MultithreadIndexSearcher;

import java.util.ArrayList;
import java.util.List;

public class Searcher {
    public static void main(String[] args) {
        MultithreadIndexSearcher searcher;
        try {
            System.out.println("Apro il reader\n");
            Thread loading = new LoadingThread(new String[]{"", ".", "..", "..."}, "Sto aprendo");
            loading.start();
            searcher = new MultithreadIndexSearcher(Utility.INDEX_PATH);
            loading.interrupt();

            //per ogni termine della query cerca tutte le colonne che fanno hit
            MergeList ml = new MergeList(searcher);
            String[] stringhe = {"katab","naktubu","taktubna","taktubu","taktubāni","taktubīna","taktubūna","write","yaktubna","yaktubu","yaktubāni","yaktubūna","ʼaktubu", "Pirlo", "Write", "támeen"};

            System.out.println("\rEffettuo la query\n");
            loading = new LoadingThread(new String[]{"", ".", "..", "..."}, "Sto cercando");
            loading.start();
            List<String> topKOverlapMerge =  ml.topKOverlapMerge(5, new ArrayList<>(List.of(stringhe)));
            loading.interrupt();
            System.out.println("\r" + topKOverlapMerge);




        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
