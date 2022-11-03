package org.ddd.concurrency.indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.ddd.Table;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadIndexer extends Thread {

    private List<Table> tb2idx;
    private  IndexWriter writer;

    public ThreadIndexer(List<Table> tb2idx, String dirIdxPath, Codec codec) throws Exception{
        this.tb2idx = tb2idx;

        Path idxPath = Paths.get(dirIdxPath);
        Directory dir = FSDirectory.open(idxPath);

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

        // Aggiunto una mappa di <field,analyzer> per passarla all'indexWriter
        Map<String, Analyzer> perFieldAnalyzer = new HashMap<>();
        perFieldAnalyzer.put("tabella", tableAnalyzer);
        perFieldAnalyzer.put("contesto", tableAnalyzer);
        perFieldAnalyzer.put("nomecolonna", tableAnalyzer);
        perFieldAnalyzer.put("colonna", columnDataAnalyzer);
        Analyzer analyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);

        IndexWriterConfig config = new IndexWriterConfig(analyzerWrapper);
        // Setto il codec per avere un indice leggibile
        if(codec != null)
            config.setCodec(codec);

        this.writer = new IndexWriter(dir, config);
    }

    @Override
    public void run(){
        for(Table t : this.tb2idx){
            // Per ogni colonna della tabella t
            for(String columnName : t.getColumns2dataColumn().keySet()) {
                // creo un documento contente l'id della tabella associata alla colonna
                // e il campo colonna a cui associamo tutti i dati nelle varie celle
                Document doc = new Document();
                doc.add(new StringField("tabella", t.getId(), Field.Store.YES));
                doc.add(new StringField("contesto", t.getContext(), Field.Store.YES));
                doc.add(new StringField("nomecolonna", columnName, Field.Store.YES));
                doc.add(new TextField("colonna", t.columnToString(columnName), Field.Store.YES));
                try {
                    this.writer.addDocument(doc);
                } catch (IOException e) {
                    System.out.println("Failed adding a doc in the index (Thread=" + this.getId() + ")");
                }
            }
        }
        try {
            this.writer.commit();
        } catch (IOException e) {
            System.out.println("Fail to commit documents (Thread=" + this.getId() + ")");
            System.out.println(e.getMessage());
        }
        try {
            this.writer.close();
        } catch (IOException e) {
            System.out.println("Fail to close Index writer (Thread=" + this.getId() + ")\n");
            System.out.println(e.getMessage());
        }
    }
}
