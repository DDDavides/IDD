package org.ddd;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenizerFactory;
import org.apache.lucene.analysis.charfilter.MappingCharFilterFactory;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.tests.analysis.TokenStreamToDot;
import org.apache.lucene.util.Version;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

public class IndexerTest {

        @Test
        public void testEnglishAnalyzer() throws Exception{

            Analyzer testAnalyzer = CustomAnalyzer.builder()
                    .withTokenizer(KeywordTokenizerFactory.class)
                    .addTokenFilter(StopFilterFactory.class, "ignoreCase", "false", "words", "stopwords.txt", "format", "wordset")
                    .build();

            TokenStream ts = testAnalyzer.tokenStream(null, "cell shun;cell;cell");
            StringWriter w = new StringWriter();
            new TokenStreamToDot(null, ts, new PrintWriter(w)).toDot();
            System.out.println(w);
        }

}
