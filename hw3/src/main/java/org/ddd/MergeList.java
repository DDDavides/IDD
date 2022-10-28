package org.ddd;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.ddd.concurrency.MultithreadIndexSearcher;

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
        if(topk <= 0){
            throw new Exception();
        }

        BooleanQuery.Builder qBuilder = new BooleanQuery.Builder();
        List<Document> documents;
        for (String element : columnElements) {
            qBuilder.add(new TermQuery(new Term ("colonna", element)), BooleanClause.Occur.SHOULD);
        }
        long start = System.currentTimeMillis();
        documents = searcher.search(qBuilder.build());
        long end = System.currentTimeMillis();

        System.out.println("Documenti ripresi in " + (end-start) + "ms");
        // popola la mappa con le colonne ritornate
        // tra la query e la colonna
        // riempi la mappa colonna termini contenuti in base all'overlap
        HashMap<String, Integer> column2frequency = new HashMap<>();
        for(String elem : columnElements){
            for(Document doc : documents){
                // se la colonna è già presente nella mappa
                String nomecolonna = doc.get("nomecolonna");
                nomecolonna += "_" + doc.get("tabella");
                System.out.println(nomecolonna);
                System.out.println("\t" + doc.get("colonna"));
                String[] dataColumn = doc.get("colonna").split(Utility.COLUMN_DATA_SEPARATOR);
                System.out.print("\tSplitted: ");
                for(String elemCell : dataColumn) {
                    System.out.print(elemCell + " ");
                }
                System.out.println();
                if(List.of(dataColumn).contains(elem)){
                    if (column2frequency.containsKey(nomecolonna)){
                        column2frequency.put(nomecolonna, column2frequency.get(nomecolonna) + 1);
                    }else {
                        column2frequency.put(nomecolonna, 1);
                    }
                }
            }
        }


        //ordina la mappa per i valori che fanno più overlap
        System.out.println("Mappa pre: " + column2frequency);
        column2frequency = Utility.sortByValue(column2frequency);
        System.out.println("Mappa post: " + column2frequency);
        //ritorna solo le prime topk colonne
        List<String> columns = new ArrayList<>(column2frequency.keySet());
        List<String> ret = columns.subList(0, Math.min(topk, columns.size()));
        return ret;
    }

}

