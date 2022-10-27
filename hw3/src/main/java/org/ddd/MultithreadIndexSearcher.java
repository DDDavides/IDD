package org.ddd;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MultithreadIndexSearcher {

    private IndexSearcher[] searchers;
    private ThreadPoolExecutor texec;
    private int coresNumber;
    private int totalCoresUsed;


    public MultithreadIndexSearcher(String indexesPath) throws IOException {

        // apro la directory degli indici
        File indexesDir = new File(indexesPath);

        // ottengo la lista degli indici
        File[] dirs = indexesDir.listFiles(pathname -> pathname.getName().contains("idx"));
        if (dirs == null) { throw new RuntimeException(); }

        // ottengo il numero massimo di core del processore
        coresNumber = Runtime.getRuntime().availableProcessors();

        int dirLen = dirs.length;                                   // numero di indici
        this.totalCoresUsed = Math.min(coresNumber, dirLen);        // core effettivamente utilizzati

        // creo il ThreadPoolExecutor per la gestione dei thread d'esecuzione
        this.texec = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.totalCoresUsed);

        System.out.println("totalCoresUsed = " + totalCoresUsed);
        int step = dirLen / this.totalCoresUsed;                    // numero di incici per core (minimo 1)
        int r = dirLen % this.totalCoresUsed;                       // resto della distribuzione di 'step'-indici su 'this.totalCoresUsed'-core
        System.out.println("step = " + step);
        System.out.println("r = " + r);
        this.searchers = new IndexSearcher[this.totalCoresUsed];    // array dei searcher per ogni core

        // assegno gli indici ai vari searcher
        int ub = 0;     // limite superiore per l'assegnazione
        int lb;         // limite inferiore per l'assegnazione

        for (int i = 0; i < this.totalCoresUsed; i++) {
            // limite superiore dell'iter. precedente = limite inferiore dell'iter. corrente
            lb = ub;
            // calcolo del nuovo limite superiore
            ub += step + (i < r ? 1 : 0);
            IndexReader[] subreaders = new IndexReader[ub-lb];
            for (int j = lb, k = 0; j < ub; j++, k++) {
                System.out.println(dirs[j].toString());
                System.out.println(ub-lb);
                subreaders[k] = DirectoryReader.open(FSDirectory.open(dirs[j].toPath()));
            }

            MultiReader multiReader = new MultiReader(subreaders);
            this.searchers[i] = new IndexSearcher(multiReader);
        }

    }

    public void shutdown() {
        this.texec.shutdown();
    }

    public List<Document> search(Query query) {
        List<Document> result = new ArrayList<>();
        ThreadSearcher[] threads = new ThreadSearcher[this.totalCoresUsed];

        // lancio la query su più thread
        for (int i = 0; i < this.totalCoresUsed; i++) {
            threads[i] = new ThreadSearcher(this.searchers[i], query);
            this.texec.execute(threads[i]);
        }


        // controllo lo stato dei thread
        boolean isFinished = false;
        while (!isFinished) {

            boolean check = true;
            for (ThreadSearcher t : threads)
                check = check && !t.isAlive();

            isFinished = check;
        }

        for(ThreadSearcher t : threads) {
            result.addAll(t.getResult());
        }

        return result;
    }

}
