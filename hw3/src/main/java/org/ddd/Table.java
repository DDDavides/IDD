package org.ddd;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
public class Table {

    private String id;
    private Map<String, List<String>> columns2dataColumn;

    public List<String> getDataByColumn(String columnId){
        return columns2dataColumn.get(columnId);
    }
}
