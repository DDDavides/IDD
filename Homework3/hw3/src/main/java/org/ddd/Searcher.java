package org.ddd;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.ddd.concurrency.LoadingThread;
import org.ddd.concurrency.searcher.MultithreadIndexSearcher;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.*;

public class Searcher {
    public static void main(String[] args) {
        MultithreadIndexSearcher searcher;
        try {
            System.out.println("Apro il reader\n");
            Thread loading = new LoadingThread(new String[]{"", ".", "..", "..."}, "Sto aprendo");
            loading.start();
            searcher = new MultithreadIndexSearcher(Utility.INDEX_PATH);
            loading.interrupt();

            // Prendo lo schema_linked da allineare
            List<String> nameColumn = new ArrayList<>();
            int i = 0;
            try (CSVReader reader = new CSVReader(new FileReader(Utility.SCHEMA_LINKED_PATH))) {
                List<String[]> rows = reader.readAll();
                for(String[] row: rows){
                    if( i == 0)
                        i++;
                    else
                        nameColumn.add(row[1]);
                }
            }
            //per ogni termine della query cerca tutte le colonne che fanno hit
            MergeList ml = new MergeList(searcher);
            //String[] stringhe = {"katab","naktubu","taktubna","taktubu","taktubāni","taktubīna","taktubūna","write","yaktubna","yaktubu","yaktubāni","yaktubūna","ʼaktubu", "Pirlo", "Write", "támeen"};

            System.out.println("\rEffettuo la query\n");
            loading = new LoadingThread(new String[]{"", ".", "..", "..."}, "Sto cercando");
            loading.start();
            List<String> topKOverlapMerge =  ml.topKOverlapMerge(50, nameColumn);
            loading.interrupt();
            System.out.println("\r" + topKOverlapMerge);

            List<String> tablesNameToMerge = new ArrayList<>();
            Map<String, String> tableId2column = new HashMap<>();
            for(String overlap: topKOverlapMerge){
                String[] overlaps = overlap.split("_");
                String column = overlaps[0];
                String tableID = overlaps[1];
                tableId2column.put(tableID, column);
                tablesNameToMerge.add(tableID);
            }
            Merger merger = new Merger(Utility.CORPUS_PATH);
            List<Table> tablesToMerge = merger.GetTablesToMergeWith(tablesNameToMerge);
            for(Table table: tablesToMerge){
                int key = table.getRow2CellContent().keySet().size() - 1;
                Set<String> columnNames = table.getRow2CellContent().get(key).keySet();
                List<String[]> lines = new LinkedList<>();
                lines.add(columnNames.toArray(new String[columnNames.size()]));
                for(Map<String, String> row: table.getRow2CellContent().values()){
                    List<String> rowToSave = new LinkedList<>();

                    for(String column: columnNames){
                        rowToSave.add(row.get(column));
                    }
                    lines.add(rowToSave.toArray(new String[rowToSave.size()]));
                }
                try (CSVWriter writer = new CSVWriter(new FileWriter(Utility.TABLE_TO_JOIN + '_' +  table.getId() + ".csv"))) {
                    writer.writeAll(lines);
                }
            }
            System.out.println(tableId2column);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
