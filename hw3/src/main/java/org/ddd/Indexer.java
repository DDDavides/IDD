package org.ddd;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.index.IndexWriter;
import org.ddd.concurrency.LoadingThread;
import org.ddd.concurrency.indexer.MultiThreadIndexer;
import org.ddd.concurrency.indexer.MultiThreadIndexer2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

public class Indexer {

    public static void main(String[] args) throws Exception {
        Codec codec = (Codec) Class.forName(Utility.CODEC).newInstance();
        FileUtils.cleanDirectory(new File(Utility.INDEX_PATH));
//        MultiThreadIndexer2 mti = new MultiThreadIndexer2(Utility.INDEX_PATH, 10, codec);
        MultiThreadIndexer mti = new MultiThreadIndexer(codec);

        Reader reader = new FileReader(Utility.CORPUS_PATH);
        // Eseguo il parser dei documenti json nel corpus
        JsonParser parser = new JsonParser(reader);
        Thread loading = new LoadingThread(new String[]{"", ".", "..", "..."}, "Sto indicizzando");

        List<Table> tables;

        System.out.println("Inizio parsing dei documenti");
        loading.start();
        long indexingTime = 0;
        long parsingTime = 0;
        while (parser.hasNext()) {
            parsingTime -= System.nanoTime();
            tables = parser.next(Utility.TABLES_CHUNKS);
            parsingTime += System.nanoTime();

            indexingTime -= System.nanoTime();
            mti.indexDocs(tables);
            indexingTime += System.nanoTime();
            mti.saveTablesInfo(tables);
        }
        loading.interrupt();

        System.out.println("\rIndexing time: " + indexingTime/1000000000 + "s");
        System.out.println("Parsing time: " + parsingTime/1000000000 + "s");
//        mti.close();
    }
}
