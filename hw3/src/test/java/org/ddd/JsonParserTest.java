package org.ddd;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.StringReader;

public class JsonParserTest {
    private static JsonParser jsonParser;

    @BeforeAll
    private static void init() {
        jsonParser = new JsonParser();
    }

    private String makeMultipleJsonObjectString() {
        String simpleJson =   "{\"nome\": \"gallo\", \"A\": { \"B\": [ \"aaaa\" ], \"C\": [ ] }            }\n"
                            + "{\"nome\": \"gatto\", \"A\": { \"B\": [ ],          \"C\": [ ] }            }\n"
                            + "{\"nome\": \"moli\",  \"A\": { \"B\": [ ],          \"C\": [\"il viola\"] } }";
        return simpleJson;
    }

    @Test
    public void testParse() {
        String jsonString = makeMultipleJsonObjectString();
        StringReader reader = new StringReader(jsonString);

        assertEquals(3, jsonParser.parse(reader).size());
    }
}
