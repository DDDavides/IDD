package org.ddd;

import com.google.gson.JsonElement;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Merger {
    JsonParser parser;
    public Merger(String pathWithTablesToMerge) {
        Reader reader = null;
        try {
            reader = new FileReader(pathWithTablesToMerge);
        } catch (FileNotFoundException e) {
            System.out.println("Impossibile aprire il file.\n");
        }
        this.parser = new JsonParser(reader);
    }

    public List<Table> GetTablesToMergeWith(List<String> tableNames){
        List<Table> tablesToJoin = new ArrayList<>();
        while (parser.hasNext()) {

            List<Table> tables = parser.next(Utility.TABLES_CHUNKS);
            for(Table table: tables) {
                if (tableNames.contains(table.getId())) {
                    tablesToJoin.add(table);
                }
            }
        }
        return tablesToJoin;
    }

}
