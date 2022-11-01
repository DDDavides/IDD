package org.ddd.concurrency.indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.ddd.Table;
import org.ddd.Utility;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiThreadIndexer2 {
    private final int coreToUse;
    private boolean isClosed;
    private final List<IndexWriter>[] core2writers;


    public MultiThreadIndexer2(String indexPath, int indexesNumber, Codec codec) throws IOException {
        int maxCoreAvailable = Runtime.getRuntime().availableProcessors(); //numero core disponibili

        this.coreToUse = Math.min(maxCoreAvailable, indexesNumber);


        // Table analyzer : StandardAnalyzer
        Analyzer tableAnalyzer = new StandardAnalyzer();
        /* ColumnData analyzer :
         *  Tokenizer = PatternTokenizer che tokenizza dividendo i token tramite ";;"
         *  TokenFilter = LowerCaseFilter
         */
        Analyzer columnDataAnalyzer = CustomAnalyzer.builder()
                    .withTokenizer(PatternTokenizerFactory.class, "pattern", "\\;;", "group", "-1")
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .build();

        // Aggiunto una mappa di <field, analyzer> per passarla all'indexWriter
        Map<String, Analyzer> perFieldAnalyzer = new HashMap<>();
        perFieldAnalyzer.put("tabella", tableAnalyzer);
        perFieldAnalyzer.put("contesto", tableAnalyzer);
        perFieldAnalyzer.put("nomecolonna", tableAnalyzer);
        perFieldAnalyzer.put("colonna", columnDataAnalyzer);
        Analyzer analyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);

        ArrayList<IndexWriter> writers = new ArrayList<>(indexesNumber);
        for (int i = 0; i < indexesNumber; i++) {
            String dirPath = indexPath + Utility.PREFIX_IDX + i + "/"; // "../index/idx_i/"
            Directory dir = FSDirectory.open(Paths.get(dirPath));

            IndexWriterConfig config = new IndexWriterConfig(analyzerWrapper);
            if(codec != null)
                config.setCodec(codec);

            IndexWriter writer = new IndexWriter(dir, config);
            writer.deleteAll();

            writers.add(writer);
        }

        this.isClosed = false;

        int writerStep = indexesNumber / this.coreToUse; // numero di indici da assegnare ad ogni core
        int writersR = indexesNumber % this.coreToUse; // resto della divisione tra numero di indici e numero core disponibili
        int writerLb;
        int writerUb = 0;
        this.core2writers = new List[this.coreToUse];
        for(int i = 0; i < this.coreToUse; i++){
            writerLb = writerUb;
            writerUb += writerStep + (i < writersR ? 1 : 0);
            this.core2writers[i] = writers.subList(writerLb, writerUb);
        }
    }

    public void indexDocs(List<Table> tables) throws Exception {
        int numTables = tables.size();

        // creo un thread per ogni cor da usare
        ThreadIndexer2[] threads = new ThreadIndexer2[this.coreToUse];
        int tableStep = numTables / this.coreToUse;
        int tablesR = numTables % this.coreToUse;

        int tablesLb; //indice della prima tabella nella porzione corrente da indicizzare
        int tablesUb = 0; //indice ultima tabella della porzione corrente da indicizzare

        for(int i = 0; i < this.coreToUse; i++){
            tablesLb = tablesUb;
            // spalmo il resto "r" su tutte le porzioni
            // => aumenta la dimensione delle prime i porzioni (ove i < r)
            tablesUb += tableStep + (i < tablesR ? 1 : 0);

            // prendo la sotto lista di tabelle da indicizzare
            List<Table> subTables = tables.subList(tablesLb, tablesUb);
            threads[i] = new ThreadIndexer2(subTables, core2writers[i]);
            threads[i].start();
        }

        for(Thread t : threads) {
            t.join();
        }
    }

    public void close() throws IOException {
        if(this.isClosed) return;
        for(List<IndexWriter> writers : core2writers) {
            for(IndexWriter writer : writers)
                writer.close();
        }
        this.isClosed = true;
    }
}
