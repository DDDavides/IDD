package org.ddd.concurrency;

import com.sun.tools.javac.Main;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.ddd.Utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MultithreadIndexSearcher {

    private final ArrayList<Path[]> paths;
    private final int coresNumber;
    private final int totalCoresUsed;


    public MultithreadIndexSearcher(String indexesPath) {
        // apro la directory degli indici
        File indexesDir = new File(indexesPath);

        // ottengo la lista degli indici
        File[] dirs = indexesDir.listFiles(pathname -> pathname.getName().contains(Utility.PREFIX_IDX));
        if (dirs == null) { throw new RuntimeException(); }

        // ottengo il numero massimo di core del processore (-1 per mantenere attivo il MainThread)
        this.coresNumber = Runtime.getRuntime().availableProcessors() - 1;
        int dirLen = dirs.length;                                   // numero di indici

        this.totalCoresUsed = Math.min(coresNumber, dirLen);        // core effettivamente utilizzati

        int step = dirLen / this.totalCoresUsed;                    // numero di indici per core (minimo uno)
        int r = dirLen % this.totalCoresUsed;                       // resto della distribuzione di 'step'-indici su 'this.totalCoresUsed'-core
        this.paths = new ArrayList<>(this.totalCoresUsed);          // array dei searcher per ogni core

        System.out.println("Numero dei core in uso : " + totalCoresUsed);

        // assegno gli indici ai vari core
        int ub = 0;     // limite superiore per l'assegnazione
        int lb;         // limite inferiore per l'assegnazione
        for (int i = 0; i < this.totalCoresUsed; i++) {
            // limite superiore dell'iter. precedente = limite inferiore dell'iter. corrente
            lb = ub;
            // calcolo del nuovo limite superiore
            ub += step + (i < r ? 1 : 0);
            Path[] indexes = new Path[ub-lb];
            for (int j = lb, k = 0; j < ub; j++, k++) {
                indexes[k] = dirs[j].toPath();
            }
            this.paths.add(indexes);
        }

    }


    public List<Document> search(Query query) throws IOException, InterruptedException {
        List<Document> result = new ArrayList<>();
        ThreadSearcher[] threads = new ThreadSearcher[this.totalCoresUsed];

        // lancio la query su più thread
        for (int i = 0; i < this.totalCoresUsed; i++) {
            threads[i] = new ThreadSearcher(query, this.paths.get(i));
            threads[i].start();
        }

//      controllo lo stato dei thread
        boolean isFinished = false;
        String[] animation = {"\\", "|", "/", "-"};
        System.out.println();
        int i = 0;
        while (!isFinished) {

            boolean check = true;
            for (ThreadSearcher t : threads)
                check = check && !t.isAlive();

            isFinished = check;

            System.out.print("\rCercando " + animation[i]);
            System.out.flush();
            Thread.sleep(200);
            i++;
            i = i % animation.length;
        }
        System.out.print("\rRisultati :   \n");
        System.out.flush();

        for(ThreadSearcher t : threads) {
            result.addAll(t.getValue());
        }


        return result;
    }

}
