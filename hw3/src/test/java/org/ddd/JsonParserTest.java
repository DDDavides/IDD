package org.ddd;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import com.google.gson.internal.LinkedTreeMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

public class JsonParserTest {
    private static JsonParser jsonParser;

    private String makeMultipleJsonObjectString() {
        String simpleJson =   "{\"nome\": \"gallo\", \"A\": { \"B\": [ \"aaaa\" ], \"C\": [ ] }            }\n"
                            + "{\"nome\": \"gatto\", \"A\": { \"B\": [ ],          \"C\": [ ] }            }\n"
                            + "{\"nome\": \"moli\",  \"A\": { \"B\": [ ],          \"C\": [\"il viola\"] } }";
        return simpleJson;
    }

    @Test
    public void testParse() throws FileNotFoundException {
        Reader reader = new FileReader("./test.json");
        jsonParser = new JsonParser(reader);
        Table t = jsonParser.parse().get(0);

        System.out.println(t);

//        assertEquals(0, jsonParser.parse(reader).size());
    }
}
