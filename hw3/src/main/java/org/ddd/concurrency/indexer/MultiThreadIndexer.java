package org.ddd.concurrency.indexer;

import com.google.gson.Gson;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.ddd.JsonParser;
import org.ddd.Table;
import org.ddd.Utility;
import org.ddd.concurrency.LoadingThread;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ddd.Utility.*;

public class MultiThreadIndexer {



    public static void main(String[] args) {
        indexDocs(INDEX_PATH, CORPUS_PATH);
    }

    /**
     * Indicizza i documenti di un corpus in una cartella dove memorizzare l'indice
     * @param indexPath percorso della cartella dove memorizzare l'indice
     * @param corpusPath percorso del corpus di documenti da indicizzare
     */
    public static void indexDocs(String indexPath, String corpusPath){
        Path idxPath = Paths.get(indexPath);
        try {
            Directory dir = FSDirectory.open(idxPath);
            try {
                indexDocs(corpusPath, (Codec) Class.forName(Utility.CODEC).newInstance());
            } catch (Exception ex) {
                System.out.println("Failed during indexing documents\n" + ex.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Failed to open index directory " + idxPath + "\n" + e.getMessage());
        }
    }

    private static void indexDocs(String corpusPath, Codec codec) throws Exception{

        // Eseguo il parser dei documenti json nel corpus
        JsonParser parser = new JsonParser();
        long parsingTime = System.nanoTime();
        System.out.println("Inizio parsing dei documenti");
        Thread loading = new LoadingThread(new String[]{"", ".", "..", "..."}, "Sto parsando");
        loading.start();
        List<Table> tables = null;
        try{
            tables = parser.parse(corpusPath);
        }catch (Exception e){
            System.out.println(e);
            return;
        }finally {
            loading.interrupt();
        }
        parsingTime = (System.nanoTime() - parsingTime)/1000000;
        System.out.println("\rParsing time: " + parsingTime + "ms");
        // salvataggio delle statistiche delle tabelle
        saveTablesInfo(tables);

        int maxCoreAvailable = Runtime.getRuntime().availableProcessors(); //numero core disponibili

        int numTables = tables.size();
        // tanti core usabili quanti quelli disponibili o un core per ogni tabella (se ho meno tabelle dei core disponibili)
        int coreToUse = Math.min(maxCoreAvailable, numTables);
        // int coreToUse = CORE_TO_USE;
        ThreadIndexer[] threads = new ThreadIndexer[coreToUse]; // uso il minimo dei thread necessari (uno per tabella o tutti quelli disponibili)

        // Creo le directory che dovranno essere usate dai singoli core per scriverci l'indice
        String[] dirIdxs = new String[coreToUse];
        for (int i=0; i<coreToUse; i++) {
            String dir_i = Utility.INDEX_PATH + Utility.PREFIX_IDX + i + "/"; // "../index/idx_i/"
            dirIdxs[i] = dir_i;
        }

        int tables2thread = numTables / maxCoreAvailable; // numero di tabelle da assegnare ad ogni core
        int r = numTables % maxCoreAvailable; // resto della divisione tra numero di tabelle e numero core disponibili

        long indexingTime = 0;
        int lb; //indice della prima tabella nella porzione corrente da indicizzare
        int ub = 0; //indice ultima tabella della porzione corrente da indicizzare
        indexingTime = System.nanoTime();
        System.out.println("Inizio indicizzazione");
        loading = new LoadingThread(new String[]{"", ".", "..", "..."}, "Sto indicizzando");
        loading.start();
        for(int i=0; i < coreToUse; i++){
            lb = ub;
            // spalmo il resto "r" su tutte le porzioni => aumenta la dimensione delle prime i porzioni (ove i < r)
            ub += tables2thread + (i < r ? 1 : 0);
            // prendo la sotto lista di tabelle da indicizzare
            List<Table> tableToIndex = tables.subList(lb, ub);
            threads[i] = new ThreadIndexer(tableToIndex, dirIdxs[i], codec);
            threads[i].start();
        }
        for(Thread t : threads) {
            t.join();
        }
        loading.interrupt();
        indexingTime = (System.nanoTime() - indexingTime)/1000000;
        System.out.println("\rIndexing time: " + indexingTime + "ms");
    }

    private static void saveTablesInfo(List<Table> tables) throws IOException {
        boolean statsDirCreated = true;
        boolean statsFileCreated = true;

        // Creo la directory dove mettere il file stats
        File statsDir = new File(Utility.STATS_DIR_PATH);
        if(!statsDir.exists()){
            statsDirCreated = statsDir.mkdir();
        }

        // Creo il file stats nella sua directory
        File f = new File(Utility.STATS_FILE);
        if(!f.exists()) {
            statsFileCreated = f.createNewFile();
        }

        if(!statsDirCreated) {
            throw new IOException("Stats directory not created\n");
        }

        if(!statsFileCreated){
            throw new IOException("Stats file not created\n");
        }

        Gson gson = new Gson();
        Writer writer = new BufferedWriter(new FileWriter(Utility.STATS_FILE, false));
        for (Table t : tables) {
            gson.toJson(t, writer);
            writer.write("\n");
        }

        writer.close();
    }
}
