package org.ddd;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.List;

public class ThreadIndexer implements Runnable {

    private int id = -1;
    private List<Table> tb2idx;
    private IndexWriter writer;

    public ThreadIndexer(List<Table> tb2idx, IndexWriter w){
        this.id += 1;
        this.tb2idx = tb2idx;
        this.writer = w;
    }

    public int getId(){
        return this.id;
    }

    @Override
    public void run() {
        for(Table t : this.tb2idx){
            // Per ogni colonna della tabella t
            for(String columnName : t.getColumns2dataColumn().keySet()) {
                // creo un documento contente l'id della tabella associata alla colonna
                // e il campo colonna a cui associamo tutti i dati nelle varie celle
                Document doc = new Document();
                doc.add(new StringField("tabella", t.getId(), Field.Store.YES));
                doc.add(new StringField("contesto", t.getContext(), Field.Store.YES));
                doc.add(new StringField("nomecolonna", columnName, Field.Store.YES));
                doc.add(new TextField("colonna", t.columnToString(columnName), Field.Store.NO));
                try {
                    this.writer.addDocument(doc);
                } catch (IOException e) {
                    System.out.println("Fail to add document in thread " + this.getId() + "\n");
                    System.out.println(e.getMessage());
                }
            }
        }
        try {
            this.writer.commit();
        } catch (IOException e) {
            System.out.println("Fail to commit documents in thread " + this.getId() + "\n");
            System.out.println(e.getMessage());
        }
    }
}
