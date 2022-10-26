package org.ddd;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

public class MergeList {

    private static IndexSearcher searcher;
    public MergeList(IndexSearcher searcher){
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
    public Map<String, Integer> topKOverlapMerge(int topk, List<String> columnElements) throws Exception {
        //se il numero di elementi richiesti topk è minore
        //o uguale di 0 lancia un eccezione
        if(topk <= 0){
            throw new Exception();
        }
        //riempi la mappa colonna termini contenuti in base all'overlap
        //tra la query e la colonna
        Map<String, Integer> column2frequency = new HashMap<>();
        // per ogni elemento nella colonna di query
        for(String element: columnElements){
            // cerca tutti i documenti su cui quella colonna fa hit
            List<Document> documents = search(element);
            //popola la mappa con le colonne ritornate
            for(Document doc : documents){
                String nomecolonna = doc.get("nomecolonna");
                nomecolonna += "_" + doc.get("tabella");
                //se la colonna è già presente nella mappa
                if (column2frequency.containsKey(nomecolonna)){
                    column2frequency.put(nomecolonna, column2frequency.get(nomecolonna) + 1);
                }else {
                    column2frequency.put(nomecolonna, 1);
                }
            }
        }
        //ordina la mappa per i valori che fanno più overlap
        column2frequency = Utility.sortByValue(column2frequency);
        Map<String, Integer> topKoverlapElements = new HashMap<>();
        //ritorna solo le prime topk colonne
        ArrayList<String> columns = new ArrayList<>(column2frequency.keySet());
        for(int i = 0; i < topk && i < columns.size(); i++){
            topKoverlapElements.put(columns.get(i), column2frequency.get(columns.get(i)));
        }
        return Utility.sortByValue(topKoverlapElements);
    }

    private static List<Document> search(String element) throws IOException {
        // crea la term query per l'elemento della colonna
        Query booleanQuery = new BooleanQuery.Builder()
                .add(new TermQuery(new Term("colonna", element)), BooleanClause.Occur.MUST).
                build();

        // prendi le totalhits del termine della colonna da cercare
        TotalHitCountCollector collector = new TotalHitCountCollector();
        System.out.println("Searching documents for: " + element);
        searcher.search(booleanQuery, collector);
        int totalHits = collector.getTotalHits();
        if(totalHits == 0){
            booleanQuery = new MatchNoDocsQuery();
            totalHits = 1;
        }
        // cerca tutti i documenti che fanno hit
        TopDocs docs = searcher.search(booleanQuery, totalHits);
        List<Document> documents = new ArrayList<>();
        for(int i = 0; i < docs.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = docs.scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            documents.add(doc);
        }
        return documents;
    }
}
