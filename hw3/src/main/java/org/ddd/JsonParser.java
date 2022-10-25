package org.ddd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import com.google.gson.internal.LinkedTreeMap;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonParser {

    private Gson gson;
    public JsonParser() {
        gson = new GsonBuilder().registerTypeAdapter(Table.class, new JsonTableDeserializer()).create();
    }

    public List<Table> parse(Reader reader) {
        List<Table> tables = new ArrayList<>();
        JsonStreamParser jsp = new JsonStreamParser(reader);
        while(jsp.hasNext()) {
            JsonElement jsonElement = jsp.next();
            if (jsonElement.isJsonObject()) {
                Table table = gson.fromJson(jsonElement, Table.class);
                tables.add(table);
            }
        }

        return tables;
    }


    // TODO: da scrivere
    public List<Table> parse(String stringPath) throws FileNotFoundException {
        Reader reader = new FileReader(stringPath);
        return parse(reader);
    }
}
