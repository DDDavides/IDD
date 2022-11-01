package org.ddd.concurrency;


import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.ddd.concurrency.searcher.MultithreadIndexSearcher;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MultiThreadIndexSearcherTest {
    private static MultithreadIndexSearcher tis;

    @Before
    public void init() throws IOException {
        tis = new MultithreadIndexSearcher("../index/");
    }

    @Test
    public void testMatchNoDocsQuery() throws InterruptedException {
        assertEquals(Collections.emptyList(), tis.search(new MatchNoDocsQuery()));
    }
    @Test
    public void testMatchAllDocsQuery() throws InterruptedException {
        assertNotEquals(Collections.emptyList(), tis.search(new MatchAllDocsQuery()));
    }

    @Test
    public void testBooleanquery() throws InterruptedException {
        Query query = new BooleanQuery.Builder()
                .add(new TermQuery(new Term("colonna", "write")), BooleanClause.Occur.MUST)
                .build();

        List<Document> docs = tis.search(query);
        assertNotEquals(Collections.emptyList(), docs);
    }
}
