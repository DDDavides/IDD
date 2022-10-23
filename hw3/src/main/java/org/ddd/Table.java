package org.ddd;

import lombok.Data;

import java.util.*;

@Data
public class Table {
    private String id;
    private Map<String, Set<String>> columns2dataColumn;

    public Table(String id){
        this.id = id;
        this.columns2dataColumn = new HashMap<>();
    }
    public Set<String> getDataByColumn(String columnId) {
        return columns2dataColumn.get(columnId);
    }
    public void add(String columnKey, String elem) {
        Set<String> dataColumn;

        if (columns2dataColumn.containsKey(columnKey)) {
            dataColumn = columns2dataColumn.get(columnKey);
        } else {
            dataColumn = new TreeSet<>();
            columns2dataColumn.put(columnKey, dataColumn);
        }

        dataColumn.add(elem);
    }
}
