package org.ddd;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Indexer {
    private static String INDEX_PATH = "../index/";
    private static String CORPUS_PATH = "/Volumes/ssd esterno/IDD/tables.json";
    //private static String CORPUS_PATH = "../corpus/tables.json";

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
                indexDocs(dir, corpusPath, new SimpleTextCodec());
            } catch (Exception ex) {
                System.out.println("Failed to index documents\n" + ex.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Failed to open index directory " + idxPath + "\n" + e.getMessage());
        }
    }

    private static void indexDocs(Directory dirIndex, String corpusPath, Codec codec) throws Exception{
        // Table analyzer
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
        perFieldAnalyzer.put("colonna", columnDataAnalyzer);
        Analyzer analyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);

        IndexWriterConfig idxWriterConfig = new IndexWriterConfig(analyzerWrapper);
        // Setto il codec per avere un indice leggibile
        if(codec != null)
            idxWriterConfig.setCodec(codec);
        IndexWriter indexWriter = new IndexWriter(dirIndex, idxWriterConfig);
        indexWriter.deleteAll();

        // Eseguo il parser dei documenti json nel corpus
        JsonParser parser = new JsonParser();
        List<Table> tables = parser.parse(corpusPath);
        showTablesInfo(tables);
        // Per ogni tabella
        for(Table t : tables){
            // Per ogni colonna della tabella t
            for(String columnName : t.getColumns2dataColumn().keySet()) {
                // creo un documento contente l'id della tabella associata alla colonna
                // e il campo colonna a cui associamo tutti i dati nelle varie celle
                Document doc = new Document();
                doc.add(new StringField("tabella", t.getId(), Field.Store.YES));
                doc.add(new TextField("colonna", t.columnToString(columnName), Field.Store.YES));
                indexWriter.addDocument(doc);
            }
        }
        indexWriter.commit();
        indexWriter.close();
    }

    private static void showTablesInfo(List<Table> tables) {
        for (Table t : tables) {
            System.out.println(t);
        }
    }
}
