package org.ddd.concurrency;

import org.ddd.JsonParser;
import org.ddd.Table;
import org.ddd.Utility;
import org.ddd.concurrency.indexer.MultiThreadIndexer2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class MultiThreadIndexerTest {
    private static MultiThreadIndexer2 mti;
    private static JsonParser jp;
    @BeforeAll
    public static void setup() throws IOException {
//        MultiThreadIndexer.indexDocs(Utility.TEST_INDEX_TEST_PATH, Utility.TEST_JSON_TABLE_PATH);
        mti = new MultiThreadIndexer2(Utility.TEST_INDEX_TEST_PATH, 10, null);
        jp = new JsonParser(Utility.TEST_JSON_TABLE_PATH);
    }

    @Test
    public void testIndexing() throws Exception {
        List<Table> tables = jp.parse();
        mti.indexDocs(tables);
    }
}
