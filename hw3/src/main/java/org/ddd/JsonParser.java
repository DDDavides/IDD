package org.ddd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonParser {

    public JsonParser() { }

    // name = _id
    // cells -> Coordinates -> 0 => header


    public List<Object> parse(Reader reader) {

        Gson gson = new GsonBuilder().create();
        JsonStreamParser jsp = new JsonStreamParser(reader);

        while(jsp.hasNext()) {
            JsonElement jsonElement = jsp.next();

            if (jsonElement.isJsonObject()) {
//                for(String key : gson.fromJson(jsonElement, Map.class)) {
//
//                }
            }

        }

        return Collections.emptyList();
    }


    public Map<String, List<String>> parse(String stringPath) {
        return Collections.emptyMap();
    }
}
