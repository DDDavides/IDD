package org.ddd;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.*;
import java.util.*;

@Data
public class Table {
    private String id;
    private Map<String, Set<String>> columns2dataColumn;
    private String context;
    private int numRows;
    private int numColumns;

    public Table(String id){
        this.id = id;
        this.columns2dataColumn = new HashMap<>();
    }

    public Table(String id, String context){
        this.id = id;
        this.columns2dataColumn = new HashMap<>();
        this.context = context;
    }

    public Table(String id, String context, int numRows, int numCols){
        this.id = id;
        this.columns2dataColumn = new HashMap<>();
        this.context = context;
        this.numRows = numRows;
        this.numColumns = numCols;
    }

    public Set<String> getDataByColumn(String columnId) {
        return columns2dataColumn.get(columnId);
    }
    public void addElemToColumn(String columnKey, String elem) {
        Set<String> dataColumn;

        if (columns2dataColumn.containsKey(columnKey)) {
            dataColumn = columns2dataColumn.get(columnKey);
        } else {
            dataColumn = new TreeSet<>();
            columns2dataColumn.put(columnKey, dataColumn);
        }

        dataColumn.add(elem);
    }

    /**
     * Stampa tutti i dati di una colonna concatenati con ";;"
     * @param columnName nome della colonna
     * @return stringa che rappresenta i dati di una colonna concatenati con ";;"
     */
    public String columnToString(String columnName){
        StringBuilder strBuilder = new StringBuilder();
        for(String data : columns2dataColumn.get(columnName)){
            strBuilder.append(data+Utility.COLUMN_DATA_SEPARATOR);
        }
        return strBuilder.toString();
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append("----------------------------------\n");
        str.append("Table id: " + getId() + "\n");
        str.append("\tcontext: " + getContext() + "\n");
        str.append("\tnumRows: " + getNumRows() + "\n");
        str.append("\tnumColumns: " + getNumColumns() + "\n");
        str.append("\tColumnsName: " + getColumns2dataColumn().keySet().toString());
        return str.toString();
    }

}
