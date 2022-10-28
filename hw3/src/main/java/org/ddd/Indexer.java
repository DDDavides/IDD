package org.ddd;

import com.google.gson.Gson;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static org.ddd.Utility.*;

public class Indexer {



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
                indexDocs(dir, corpusPath, (Codec) Class.forName(Utility.CODEC).newInstance());
            } catch (Exception ex) {
                System.out.println("Failed during indexing documents\n" + ex.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Failed to open index directory " + idxPath + "\n" + e.getMessage());
        }
    }

    private static void indexDocs(Directory dirIndex, String corpusPath, Codec codec) throws Exception{

        // Eseguo il parser dei documenti json nel corpus
        JsonParser parser = new JsonParser();
        long parsingTime = System.nanoTime();
        List<Table> tables = parser.parse(corpusPath);
        parsingTime = System.nanoTime() - parsingTime;
        System.out.println("Parsing time: " + parsingTime + "ns");
        saveTablesInfo(tables);

        int maxCoreAvailable = Runtime.getRuntime().availableProcessors(); //numero core disponibili

        int numTables = tables.size();
        // tanti core usabili quanti quelli disponibili o un core per ogni tabella (se ho meno tabelle dei core disponibili)
        int coreToUse = Math.min(maxCoreAvailable, numTables);
        ThreadIndexer[] threads = new ThreadIndexer[coreToUse]; // uso il minimo dei thread necessari (uno per tabella o tutti quelli disponibili)

        // Creo le directory che dovranno essere usate dai singoli core per scriverci l'indice
        String[] dirIdxs = new String[coreToUse];
        for (int i=0; i<coreToUse; i++) {
            String dir_i = Utility.INDEX_PATH + Utility.PREFIX_IDX + i + "/"; // "../index/idx_i/"
//            System.out.println("dirIdxs[" + i + "] = " + dir_i);
            dirIdxs[i] = dir_i;
        }
//        System.out.println("Numero tabelle: " + numTables);
//        System.out.println("Numero core disponibili: " + maxCoreAvailable);
        int tables2thread = numTables / maxCoreAvailable; // numero di tabelle da assegnare ad ogni core
        int r = numTables % maxCoreAvailable; // resto della divisione tra numero di tabelle e numero core disponibili
//        System.out.println("Numero di tabelle per thread: " + tables2thread);
//        System.out.println("Resto: " + r);

        long indexingTime = 0;
        int lb; //indice della prima tabella nella porzione corrente da indicizzare
        int ub = 0; //indice ultima tabella della porzione corrente da indicizzare
        indexingTime = System.nanoTime();
        System.out.println("Inizio indicizzazione");
        for(int i=0; i < coreToUse; i++){
            lb = ub;
            // spalmo il resto "r" su tutte le porzioni => aumenta la dimensione delle prime i porzioni (ove i < r)
            ub += tables2thread + (i < r ? 1 : 0);
//            System.out.println("SubTable [" + lb + ", " + ub + "]");
            // prendo la sotto lista di tabelle da indicizzare
            List<Table> tableToIndex = tables.subList(lb, ub);
            threads[i] = new ThreadIndexer(tableToIndex, dirIdxs[i], codec);
//            System.out.println("Thread id: " + i);
//            long delta = System.nanoTime();
            threads[i].run();
//            delta = System.nanoTime() - delta;
//            indexingTime += delta;
        }
        indexingTime = System.nanoTime() - indexingTime;
        System.out.println("Indexing time: " + indexingTime + "ns");
        for(Thread t : threads) {
            t.join();
        }
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
