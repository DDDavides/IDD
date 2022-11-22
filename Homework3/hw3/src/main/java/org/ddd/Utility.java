package org.ddd;

import java.util.*;

public class Utility {
    public static final String STATS_DIR_PATH = "../stats/";
    public static final String STATS_FILE = STATS_DIR_PATH + "stats.json";
    public static final String INDEX_PATH = "../index/";
    public static final String PREFIX_IDX = "idx_";
    //public static final String CODEC = "org.apache.lucene.codecs.simpletext.SimpleTextCodec";
    public static final String CODEC = "org.apache.lucene.codecs.lucene94.Lucene94Codec";
    public static final String TEST_INDEX_TEST_PATH = "../index_test/";
    public static final String TEST_JSON_TABLE_PATH = "tables_trunc.json";
    public static final String COLUMN_DATA_SEPARATOR = ";;";
    public static int CORE_TO_USE = 1;

    // solo per Moli
//    public static final String CORPUS_PATH = "/Volumes/ssd esterno/IDD/tables.json";
    public static final int TABLES_CHUNKS = 10000;
    //public static final String CORPUS_PATH = "./tables_test.json";
    public  static String CORPUS_PATH = "../corpus/tables.json";
//   public  static String CORPUS_PATH = "./tables_trunc.json";
//    public  static String CORPUS_PATH = "./test.json";

    /**
     * Metodo che ordina in base ai campi value
     * una mappa passata come parametro
     * @param map
     * @return
     */
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> map)
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
