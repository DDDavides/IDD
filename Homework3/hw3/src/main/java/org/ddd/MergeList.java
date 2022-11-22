package org.ddd;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.ddd.concurrency.searcher.MultithreadIndexSearcher;

import java.io.IOException;
import java.util.*;

public class MergeList {

    private static MultithreadIndexSearcher searcher;
    public MergeList(MultithreadIndexSearcher searcher){
        this.searcher = searcher;
    }
    /**
     * Metodo che ritorna i primi top topk
     * degli elementi all'interno di un searcher
     * in un indici con gli elementi della query passati come parametro
     * @param topk
     * @param columnElements
     * @return
     * @throws Exception
     */
    public List<String> topKOverlapMerge(int topk, List<String> columnElements) throws Exception {
        // se il numero di elementi richiesti topk è minore
        // o uguale di 0 lancia un eccezione
        if (topk <= 0) {
            throw new Exception();
        }

//        BooleanQuery.Builder qBuilder = new BooleanQuery.Builder();
//        for (String element : columnElements) {
//            qBuilder.add(new TermQuery(new Term("colonna", element)), BooleanClause.Occur.SHOULD);
//        }
//        long start = System.currentTimeMillis();
//        documents = searcher.search(qBuilder.build());
//        long end = System.currentTimeMillis();
//
//        System.out.println("Documenti ripresi in " + (end - start) + "ms");


        // popola la mappa con le colonne ritornate
        // tra la query e la colonna
        // riempi la mappa colonna termini contenuti in base all'overlap
        HashMap<String, Integer> column2frequency = new HashMap<>();
        long searchTime = 0;
        for (String element : columnElements) {
            searchTime -= System.nanoTime();
            List<Document> documents = search(element);
            searchTime += System.nanoTime();
            //popola la mappa con le colonne ritornate
            for (Document doc : documents) {
                //se la colonna è già presente nella mappa
                String nomecolonna = doc.get("nomecolonna");
                nomecolonna += "_" + doc.get("tabella");
                if (column2frequency.containsKey(nomecolonna)) {
                    column2frequency.put(nomecolonna, column2frequency.get(nomecolonna) + 1);
                } else {
                    column2frequency.put(nomecolonna, 1);
                }

            }
        }
        System.out.println("\rTempo totale di ricerca: " + (searchTime/1000000) + "ms\n");
        //ordina la mappa per i valori che fanno più overlap
        column2frequency = Utility.sortByValue(column2frequency);
        //ritorna solo le prime topk colonne
        List<String> columns = new ArrayList<>(column2frequency.keySet());
        return columns.subList(0, Math.min(topk, columns.size()));
    }

    private static List<Document> search(String element) throws IOException, InterruptedException, ParseException {
        Analyzer columnDataAnalyzer = CustomAnalyzer.builder()
                .withTokenizer(KeywordTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .build();
        QueryParser qp = new QueryParser("colonna" ,columnDataAnalyzer);
        qp.setDefaultOperator(QueryParser.Operator.AND);
        return searcher.search(qp.parse("\"" + element + "\""));
    }
}

