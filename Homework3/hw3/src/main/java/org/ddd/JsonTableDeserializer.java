package org.ddd;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JsonTableDeserializer implements JsonDeserializer<Table> {

    @Override
    public Table deserialize(JsonElement jsonElement, Type type,
                             JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        // prendo il jsonElement da deserializzare come un jesonObject
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // prendo l'indice interno di MongoDB come codice univoco per la mia tabella
        String id = jsonObject.get("_id").getAsJsonObject().get("$oid").getAsString();

        // prendo la pagina wikipedia da cui la tabella è stata presa
        String context = jsonObject.get("referenceContext").getAsString();

        // prendo il numero di righe della tabella
        int numColumns = jsonObject.get("maxDimensions").getAsJsonObject().get("column").getAsInt() + 1;

        // prendo il numero di righe della tabella
        int numRows = jsonObject.get("maxDimensions").getAsJsonObject().get("row").getAsInt() + 1;

        // creo la struttura dati per la Tabella
        Table result = new Table(id, context, numRows, numColumns);

        // mi serve solo per sapere quante sono le colonne
        // ArrayList<String> headers = jsonDeserializationContext.deserialize(
        //        jsonObject.get("headersCleaned").getAsJsonArray(), List.class);
        int headerSize = numColumns; // -> mi prendo il numero di colonne

        // allocco la memoria per un array associativo: indice -> colonna
        String[] columns = new String[headerSize];

        // mappa delle celle che non sono state assegnate ad una colonna nella prima passata
        Map<String, Integer> unmappedCells = new HashMap<>();

        JsonObject joCell = null;           // cella della tabella da deserializzare
        String columnKey = "";              // chiave della colonna cui si riferisce una cella
        JsonObject coordinates = null;      // JsonObject relativo alle coordinate della cella nella tabella (row, column)
        int columnCell = 0;                 // numero della colonna cui si riferisce una cella
        boolean isHeader = false;           // definisce se la cella è un header o meno
        String cleanedText = "";            // testo contenuto in una cella


        for (JsonElement jeCell : jsonObject.get("cells").getAsJsonArray()) {
            // esprimo il jsonElement come un jsonObject
            joCell = jeCell.getAsJsonObject();
            coordinates = joCell.get("Coordinates").getAsJsonObject();
            columnCell = coordinates.get("column").getAsInt();
            isHeader = joCell.get("isHeader").getAsBoolean();
            cleanedText = joCell.get("cleanedText").getAsString();

            if (columns[columnCell] == null && isHeader)
                columns[columnCell] = cleanedText;

            if(!isHeader) {
                columnKey = columns[columnCell];
                if (columnKey != null) {
                    result.addElemToColumn(columnKey, cleanedText);
                } else {
                    unmappedCells.put(cleanedText, columnCell);
                }
            }
        }

        for (Map.Entry<String, Integer> cell : unmappedCells.entrySet()) {
            if (columns[cell.getValue()]==null)
                columns[cell.getValue()] = cell.getValue().toString();
            result.addElemToColumn(columns[cell.getValue()], cell.getKey());
        }

        return result;
    }
}
