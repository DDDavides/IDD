package org.ddd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    private final Gson gson;
    private JsonStreamParser jsonStreamParser;

    public JsonParser() {
        this.gson = new GsonBuilder().registerTypeAdapter(Table.class, new JsonTableDeserializer()).create();
    }

    public JsonParser(Reader reader) {
        this.gson = new GsonBuilder().registerTypeAdapter(Table.class, new JsonTableDeserializer()).create();
        this.jsonStreamParser = new JsonStreamParser(reader);
    }

    public JsonParser(String filePath) throws FileNotFoundException {
        this.gson = new GsonBuilder().registerTypeAdapter(Table.class, new JsonTableDeserializer()).create();
        Reader reader = new FileReader(filePath);
        this.jsonStreamParser = new JsonStreamParser(reader);
    }

    public boolean hasNext() {
        return this.jsonStreamParser.hasNext();
    }

    public List<Table> next(int limit) {
        List<Table> tables = new ArrayList<>();

        for (int i = 0; i < limit && this.jsonStreamParser.hasNext(); i++) {
            JsonElement jsonElement = this.jsonStreamParser.next();
            if (jsonElement.isJsonObject()) {
                tables.add(gson.fromJson(jsonElement, Table.class));
            }

        }
        return tables;
    }

    public List<Table> parse() {
        List<Table> tables = new ArrayList<>();

        while(this.jsonStreamParser.hasNext()) {
            JsonElement jsonElement = this.jsonStreamParser.next();
            if (jsonElement.isJsonObject()) {
                Table table = gson.fromJson(jsonElement, Table.class);
                tables.add(table);

            }
        }

        return tables;
    }

    public List<Table> parse(String stringPath) throws FileNotFoundException {
        Reader reader = new FileReader(stringPath);
        this.jsonStreamParser = new JsonStreamParser(reader);
        return parse();
    }
}
