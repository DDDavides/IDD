package org.ddd;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import java.util.*;

public class MergeList {

    private static final int  K = 5;

    public List<String> topKOverlapMerge(int k, IndexSearcher searcher, List<String> columnElements) throws Exception {
        //TODO: implementare il metodo
        if(k <= 0){
            throw new Exception();
        }

        HashMap<String, Integer> column2frequency = new LinkedHashMap<>();
        for(String element: columnElements){
            TopDocs docs = searcher.search(new TermQuery(new Term(element)), 5);
            for(int i = 0; i < docs.scoreDocs.length; i++){
                ScoreDoc scoreDoc = docs.scoreDocs[i];
                Document doc = searcher.doc(scoreDoc.doc);
                if (column2frequency.containsKey(doc.toString())){
                    column2frequency.put(doc.toString(), column2frequency.get(doc) + 1);
                }else {
                    column2frequency.put(doc.toString(), 1);
                }

            }
        }
        column2frequency = sortByValue(column2frequency);
        List<String> topKoverlapElements = new LinkedList<>();
        for(int i = 0; i < k; i++){
            topKoverlapElements.add(column2frequency.get(i));
        }
        return topKoverlapElements;
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
