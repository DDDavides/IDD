package org.ddd;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
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
    //TODO: sostituire searcher con lista di risultati e
    // creare apposita classe searcher che effettua la
    // ricerca della query e gli passa tale lista di risultati
    public List<String> topKOverlapMerge(int topk, List<String> columnElements) throws Exception {
        //se il numero di elementi richiesti topk è minore
        //o uguale di 0 lancia un eccezione
        if(topk <= 0){
            throw new Exception();
        }
        //riempi la mappa colonna termini contenuti in base all'overlap
        //tra la query e la colonna
        HashMap<String, Integer> column2frequency = new HashMap<>();
        for(String element: columnElements){
            List<Document> documents = search(element);
            //popola la mappa con le colonne ritornate
            for(Document doc : documents){
                //se la colonna è già presente nella mappa
                String nomecolonna = doc.get("nomecolonna");
                nomecolonna += "_" + doc.get("tabella");
                if (column2frequency.containsKey(nomecolonna)){
                    column2frequency.put(nomecolonna, column2frequency.get(nomecolonna) + 1);
                }else {
                    column2frequency.put(nomecolonna, 1);
                }
            }
        }
        //ordina la mappa per i valori che fanno più overlap
        column2frequency = Utility.sortByValue(column2frequency);
        List<String> topKoverlapElements = new LinkedList<>();
        //ritorna solo le prime topk colonne
        ArrayList<String> columns = new ArrayList<>(column2frequency.keySet());
        for(int i = 0; i < topk && i < columns.size(); i++){
            topKoverlapElements.add(columns.get(i));
        }
        return topKoverlapElements;
    }

    private static List<Document> search(String element) throws IOException {
        List<Document> documents = new ArrayList<>();
        TotalHitCountCollector collector = new TotalHitCountCollector();
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(new TermQuery(new Term("colonna", element)), BooleanClause.Occur.MUST).
                build();
        return searcher.search(booleanQuery);
//        TopDocs docs = searcher.search(booleanQuery, collector.getTotalHits());
//        //popola la mappa con le colonne ritornate
//        for(int i = 0; i < docs.scoreDocs.length; i++) {
//            ScoreDoc scoreDoc = docs.scoreDocs[i];
//            Document doc = searcher.doc(scoreDoc.doc);
//            documents.add(doc);
//        }
//        return documents;
    }
}
