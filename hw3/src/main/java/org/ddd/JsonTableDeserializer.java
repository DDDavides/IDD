package org.ddd;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonTableDeserializer implements JsonDeserializer<Table> {

    @Override
    public Table deserialize(JsonElement jsonElement, Type type,
                             JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        // prendo il jsonElement da deserializzare come un jesonObject
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // prendo l'indice interno di MongoDB come codice univoco per la mia tabella
        String id = jsonObject.get("_id").getAsJsonObject().get("$oid").getAsString();

        // creo la struttura dati pre la Tabella
        Table result = new Table(id);

        // mi serve solo per sapere quante sono le colonne
        ArrayList<String> headers = jsonDeserializationContext.deserialize(
                jsonObject.get("headersCleaned").getAsJsonArray(), List.class);
        int headerSize = headers.size(); // -> mi prendo il numero di colonne

        // allocco la memoria per un array associativo: indice -> colonna
        String[] columns = new String[headerSize];

        // mappa delle celle che non sono state assegnate ad una colonna nella prima passata
        Map<String, Integer> unmappedCells = new HashMap<>();

        JsonObject joCell = null;           // cella della tabella da deserializzare
        String columnKey = "";              // chiave della colonna cui si riferisce una cella
        JsonObject coordinates = null;      // JsonObject relativo alle coordinate della cella nella tabella (row, column)
        int column = 0;                     // numero della colonna cui si riferisce una cella
        boolean isHeader = false;           // definisce se la cella è un header o meno
        String cleanedText = "";            // testo contenuto in una cella


        for (JsonElement jeCell : jsonObject.get("cells").getAsJsonArray()) {
            // esprimo il jsonElement come un jsonObject
            joCell = jeCell.getAsJsonObject();
            coordinates = joCell.get("Coordinates").getAsJsonObject();
            column = coordinates.get("column").getAsInt();
            isHeader = joCell.get("isHeader").getAsBoolean();
            cleanedText = joCell.get("cleanedText").getAsString();

            if (columns[column] == null && isHeader)
                columns[column] = cleanedText;

            if(!isHeader) {
                columnKey = columns[column];
                if (columnKey != null) {
                    result.add(columnKey, cleanedText);
                } else {
                    unmappedCells.put(cleanedText, column);
                }
            }
        }

        for (Map.Entry<String, Integer> cell : unmappedCells.entrySet()) {
            result.add(columns[cell.getValue()], cell.getKey());
        }

        return result;
    }
}
