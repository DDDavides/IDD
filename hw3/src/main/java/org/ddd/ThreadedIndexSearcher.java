package org.ddd;

import org.apache.lucene.store.FSDirectory;

import java.util.concurrent.*;

public class ThreadedIndexSearcher {


    public ThreadedIndexSearcher (String indexesPath) {
        int cores = Runtime.getRuntime().availableProcessors();

        Executor executor = Executors.newFixedThreadPool(cores);
        ThreadPoolExecutor texec = (ThreadPoolExecutor) Executors.newFixedThreadPool(cores);

    }

}
