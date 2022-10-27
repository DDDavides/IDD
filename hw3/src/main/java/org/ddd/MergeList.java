package org.ddd;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.*;

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
        if(topk <= 0){
            throw new Exception();
        }

        BooleanQuery.Builder qBuilder = new BooleanQuery.Builder();
        List<Document> documents;
        for (String element : columnElements) {
            qBuilder.add(new TermQuery(new Term ("colonna", element)), BooleanClause.Occur.SHOULD);
        }
        documents = searcher.search(qBuilder.build());

        // popola la mappa con le colonne ritornate
        // tra la query e la colonna
        // riempi la mappa colonna termini contenuti in base all'overlap
        HashMap<String, Integer> column2frequency = new HashMap<>();
        for(Document doc : documents){
            // se la colonna è già presente nella mappa
            String nomecolonna = doc.get("nomecolonna");
            nomecolonna += "_" + doc.get("tabella");
            if (column2frequency.containsKey(nomecolonna)){
                column2frequency.put(nomecolonna, column2frequency.get(nomecolonna) + 1);
            }else {
                column2frequency.put(nomecolonna, 1);
            }
        }

        //ordina la mappa per i valori che fanno più overlap
        column2frequency = Utility.sortByValue(column2frequency);
        //ritorna solo le prime topk colonne
        List<String> columns = new ArrayList<>(column2frequency.keySet());
        return columns.subList(0, Math.min(topk, columns.size()));
    }

}

