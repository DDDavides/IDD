package org.ddd;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.*;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.tests.analysis.TokenStreamToDot;
import org.ddd.concurrency.indexer.MultiThreadIndexer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.ddd.Utility.CORPUS_PATH;
import static org.ddd.Utility.INDEX_PATH;

public class IndexerTest {

    @BeforeAll
    public static void setup(){
//        MultiThreadIndexer.indexDocs(Utility.TEST_JSON_TABLE_PATH);
    }

//    @Test
//    public void testPermormance_10_7_5_3_2_1(){
//        int[] coresTest = {10, 7, 5, 3, 2, 1};
//        for (int i = 0; i<coresTest.length; i++){
//            Utility.CORE_TO_USE = coresTest[i];
//            System.out.println("============ " + coresTest[i] + "_CORES ==============");
//            for(int j = 0; j<3; j++){
//                System.out.println("----------test " + j+1 + "------------");
//                MultiThreadIndexer.indexDocs(INDEX_PATH, CORPUS_PATH);
//            }
//        }
//
//    }

//    @Test
//    public void testPatternTokenizer() throws Exception{
//
//        Analyzer testAnalyzer = CustomAnalyzer.builder()
//                .withTokenizer(PatternTokenizerFactory.class, "pattern", "\\;;", "group", "-1")
//                .addTokenFilter(LowerCaseFilterFactory.class)
//                .build();
//
//        TokenStream ts = testAnalyzer.tokenStream(null, "Cell1 shun;;cEll2;;cell3");
//        StringWriter w = new StringWriter();
//        new TokenStreamToDot(null, ts, new PrintWriter(w)).toDot();
//        System.out.println(w);
//    }



}
