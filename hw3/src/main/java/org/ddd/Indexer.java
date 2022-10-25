package org.ddd;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
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

    public static void main(String[] args) {
        Path idxPath = Paths.get(INDEX_PATH);
        try {
            Directory dir = FSDirectory.open(idxPath);
            try {
                indexDocs(dir, new SimpleTextCodec());
            } catch (Exception ex) {
                System.out.println("Failed to index documents\n" + ex.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Failed to open index directory " + idxPath + "\n" + e.getMessage());
        }
    }

    private static void indexDocs(Directory dir, Codec codec) throws Exception{
        Analyzer tableAnalyzer = new StandardAnalyzer();
        Analyzer dataColumnDataAnalyzer = CustomAnalyzer.builder()
                .withTokenizer(PatternTokenizerFactory.class, "pattern", "\\;;", "group", "-1")
                .addTokenFilter(StopFilterFactory.class, "ignoreCase", "false", "words", "stopwords.txt", "format", "wordset")
                .build();

        Map<String, Analyzer> perFieldAnalyzer = new HashMap<>();
        perFieldAnalyzer.put("tabella", tableAnalyzer);
        perFieldAnalyzer.put("colonna", dataColumnDataAnalyzer);
        Analyzer analyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);

        IndexWriterConfig idxWriterConfig = new IndexWriterConfig(analyzerWrapper);
        if(codec != null)
            idxWriterConfig.setCodec(codec);
        IndexWriter indexWriter = new IndexWriter(dir, idxWriterConfig);
        indexWriter.deleteAll();

        JsonParser parser = new JsonParser();
        List<Table> tables = parser.parse("../corpus/");
        for(Table t : tables){
            for(String columnName : t.getColumns2dataColumn().keySet()) {
                Document doc = new Document();
                doc.add(new StringField("tabella", t.getId(), Field.Store.YES));
                doc.add(new TextField("colonna", t.columnToString(columnName), Field.Store.YES));
            }
        }
    }
}
