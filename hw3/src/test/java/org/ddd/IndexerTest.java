package org.ddd;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.*;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.tests.analysis.TokenStreamToDot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

public class IndexerTest {

    @BeforeAll
    public static void setup(){
        Indexer.indexDocs(Utility.TEST_INDEX_TEST_PATH, Utility.TEST_JSON_TABLE_PATH);
    }

    @Test
    public void tvoid(){

    }

//    @Test
    public void testPatternTokenizer() throws Exception{

        Analyzer testAnalyzer = CustomAnalyzer.builder()
                .withTokenizer(PatternTokenizerFactory.class, "pattern", "\\;;", "group", "-1")
                .addTokenFilter(LowerCaseFilterFactory.class)
                .build();

        TokenStream ts = testAnalyzer.tokenStream(null, "Cell1 shun;;cEll2;;cell3");
        StringWriter w = new StringWriter();
        new TokenStreamToDot(null, ts, new PrintWriter(w)).toDot();
        System.out.println(w);
    }



}
