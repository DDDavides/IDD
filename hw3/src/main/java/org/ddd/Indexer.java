package org.ddd;

import org.apache.lucene.codecs.Codec;
import org.ddd.concurrency.LoadingThread;
import org.ddd.concurrency.indexer.MultiThreadIndexer2;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Indexer {

    public static void main(String[] args) throws Exception {
        Codec codec = (Codec) Class.forName(Utility.CODEC).newInstance();
        MultiThreadIndexer2 mti = new MultiThreadIndexer2(Utility.INDEX_PATH, 10, codec);

        Reader reader = new FileReader(Utility.CORPUS_PATH);
        // Eseguo il parser dei documenti json nel corpus
        JsonParser parser = new JsonParser(reader);
        Thread loading = new LoadingThread(new String[]{"", ".", "..", "..."}, "Sto indicizzando");

        System.out.println("Inizio parsing dei documenti");
        loading.start();
        List<Table> tables;
        while (parser.hasnext()) {
            tables = parser.next(1000);
            mti.indexDocs(tables);
        }
        loading.interrupt();
        mti.close();

    }
}
