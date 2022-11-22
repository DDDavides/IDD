package org.ddd.concurrency.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.ddd.Indexer;
import org.ddd.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ThreadIndexer2 extends Thread {

    private HashMap<IndexWriter, List<Table>> writer2tables;

    private List<Table> tables;
    private List<IndexWriter> writers;

    public ThreadIndexer2(List<Table> tables, List<IndexWriter> writers) {
        this.tables = tables;
        this.writers = writers;
    }

    private List<Document> tablesToDocs(List<Table> tables) {
        List<Document> docs = new ArrayList<>();
        for (Table table : tables) {
            docs.addAll(table.getColumns2dataColumn().keySet()
                    .stream().map((columnName) -> {
                        Document doc = new Document();
                        doc.add(new StringField("tabella", table.getId(), Field.Store.YES));
                        doc.add(new StringField("contesto", table.getContext(), Field.Store.YES));
                        doc.add(new StringField("nomecolonna", columnName, Field.Store.YES));
                        doc.add(new TextField("colonna", table.columnToString(columnName), Field.Store.NO));
                        return doc;
                    }).collect(Collectors.toList()));
        }
        return docs;
    }

    @Override
    public void run() {
        int tablesNumber = tables.size();
        int writersNumber = writers.size();

        int step = tablesNumber / writersNumber;
        int r = tablesNumber % writersNumber;
        IndexWriter w;
        for (int i = 0, lb, ub = 0; i < writersNumber; i++) {
            lb = ub;
            ub += step + (i < r ? 1 : 0);
            w = writers.get(i);
            try {
                w.addDocuments(tablesToDocs(tables.subList(lb, ub)));
                w.commit();
            } catch(Exception e) {
                System.err.println("Errore di indicizzazione!");
            }
        }
    }
}

