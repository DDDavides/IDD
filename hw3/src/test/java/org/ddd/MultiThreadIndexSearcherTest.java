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

    @Before
    public void init() {
        tis = new MultithreadIndexSearcher("../index/");
    }

    @Test
    public void testMatchNoDocsQuery() throws IOException {
        assertEquals(Collections.emptyList(), tis.search(new MatchNoDocsQuery()));
    }

    @Test
    public void testMatchAllDocsQuery() throws IOException {
        assertNotEquals(Collections.emptyList(), tis.search(new MatchAllDocsQuery()));
    }
}
