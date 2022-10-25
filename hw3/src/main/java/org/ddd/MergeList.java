package org.ddd;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

public class MergeList {

    private static IndexSearcher searcher;
    public MergeList(IndexSearcher searcher){
        searcher = searcher;
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
                if (column2frequency.containsKey(doc.toString())){
                    column2frequency.put(doc.toString(), column2frequency.get(doc) + 1);
                }else {
                    column2frequency.put(doc.toString(), 1);
                }
            }
        }
        //ordina la mappa per i valori che fanno più overlap
        column2frequency = sortByValue(column2frequency);
        List<String> topKoverlapElements = new LinkedList<>();
        //ritorna solo le prime topk colonne
        String[] columns = (String[]) column2frequency.keySet().toArray();
        for(int i = 0; i < topk; i++){
            topKoverlapElements.add(columns[i]);
        }
        return topKoverlapElements;
    }

    private static List<Document> search(String element) throws IOException {
        List<Document> documents = new ArrayList<>();
        TotalHitCountCollector collector = new TotalHitCountCollector();
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(new PhraseQuery("colonna", element), BooleanClause.Occur.MUST).
                build();
        searcher.search(booleanQuery, collector);
        TopDocs docs = searcher.search(booleanQuery, collector.getTotalHits());
        //popola la mappa con le colonne ritornate
        for(int i = 0; i < docs.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = docs.scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            documents.add(doc);
        }
        return documents;
    }

    /**
     * Metodo che ordina in base ai campi value
     * una mappa passata come parametro
     * @param map
     * @return
     */
    private static HashMap<String, Integer> sortByValue(HashMap<String, Integer> map)
    {
        // Crea una lista di elementi con i valori della mappa
        List<Map.Entry<String, Integer> > list =
                new LinkedList<>(map.entrySet());

        // Ordina la lista
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });

        // Metti i dati ordinati della lista in una hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
