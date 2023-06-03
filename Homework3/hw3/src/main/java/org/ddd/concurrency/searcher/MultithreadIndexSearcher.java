package org.ddd.concurrency.searcher;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.ddd.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultithreadIndexSearcher {
    private int coresNumber;
    private int totalCoresUsed;
    private IndexSearcher[] searchers;

    public MultithreadIndexSearcher(String indexesPath) throws IOException {
        // apro la directory degli indici
        File indexesDir = new File(indexesPath);

        // ottengo la lista degli indici
        File[] dirs = indexesDir.listFiles(pathname -> pathname.getName().contains(Utility.PREFIX_IDX));
        if (dirs == null) { throw new RuntimeException(); }

        // ottengo il numero massimo di core del processore (-1 per mantenere attivo il MainThread)
        this.coresNumber = Runtime.getRuntime().availableProcessors() - 1;
//        this.coresNumber = 1;
        int dirLen = dirs.length;                                   // numero di indici

        this.totalCoresUsed = Math.min(coresNumber, dirLen);        // core effettivamente utilizzati
        //this.totalCoresUsed = 8;

        int step = dirLen / this.totalCoresUsed;                    // numero di indici per core (minimo uno)
        int r = dirLen % this.totalCoresUsed;                       // resto della distribuzione di 'step'-indici su 'this.totalCoresUsed'-core
        this.searchers = new IndexSearcher[this.totalCoresUsed];    // array dei searcher per ogni core
        System.out.println("Numero dei core in uso : " + totalCoresUsed);

        // assegno gli indici ai vari core
        int ub = 0;     // limite superiore per l'assegnazione
        int lb;         // limite inferiore per l'assegnazione
        for (int i = 0; i < this.totalCoresUsed; i++) {
            // limite superiore dell'iter. precedente = limite inferiore dell'iter. corrente
            lb = ub;
            // calcolo del nuovo limite superiore
            ub += step + (i < r ? 1 : 0);
            IndexReader[] readers = new IndexReader[ub-lb];
            for (int j = lb, k = 0; j < ub; j++, k++) {
                readers[k] = DirectoryReader.open(FSDirectory.open(dirs[j].toPath()));
            }
            MultiReader mr = new MultiReader(readers);
            searchers[i] = new IndexSearcher(mr);

        }

    }

    public List<Document> search(Query query) throws InterruptedException {
        List<Document> result = new ArrayList<>();
        ThreadSearcher[] threads = new ThreadSearcher[this.totalCoresUsed];

        // lancio la query su più thread
        for (int i = 0; i < this.totalCoresUsed; i++) {
            threads[i] = new ThreadSearcher(query, searchers[i]);
            threads[i].start();
        }

        for(ThreadSearcher t : threads) {
            t.join();
            result.addAll(t.getValue());
        }

        return result;
    }

}
