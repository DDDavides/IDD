package org.ddd;


import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MultiThreadIndexSearcherTest {
    private static MultithreadIndexSearcher tis;

    @AfterAll
    public static void stop() {
        tis.shutdown();
    }

    @Before
    public void init() throws IOException {
        tis = new MultithreadIndexSearcher("../index/");
    }

    @Test
    public void testMatchNoDocsQuery() {
        assertEquals(Collections.emptyList(), tis.search(new MatchNoDocsQuery()));
    }

    @Test
    public void testMatchAllDocsQuery() {
        assertNotEquals(Collections.emptyList(), tis.search(new MatchAllDocsQuery()));
    }
}
