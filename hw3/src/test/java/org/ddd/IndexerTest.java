package org.ddd;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.charfilter.MappingCharFilterFactory;
import org.apache.lucene.analysis.core.*;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerImpl;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.tests.analysis.TokenStreamToDot;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.Version;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class IndexerTest {
        @Test
        public void testEnglishAnalyzer() throws Exception{

            Analyzer testAnalyzer = CustomAnalyzer.builder()
                    .withTokenizer(PatternTokenizerFactory.class, "pattern", "\\;", "group", "-1")
                    .addTokenFilter(StopFilterFactory.class, "ignoreCase", "false", "words", "stopwords.txt", "format", "wordset")
                    .build();

            TokenStream ts = testAnalyzer.tokenStream(null, "cell1 shun;;cell2;cell3");
            StringWriter w = new StringWriter();
            new TokenStreamToDot(null, ts, new PrintWriter(w)).toDot();
            System.out.println(w);
        }



}
