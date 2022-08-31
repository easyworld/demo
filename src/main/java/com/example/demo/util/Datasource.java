package com.example.demo.util;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class Datasource implements InitializingBean {

    private List<Map<String, String>> data;

    public List<Map<String, String>> getData() {
        return data;
    }

    public void setData(List<Map<String, String>> data) {
        this.data = data;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        data = new ArrayList<>();
        List<String> pokerCsvString = FileUtil.readAll("result.txt");
        String[] columns = pokerCsvString.get(0).split("\t");
        for (int i = 1; i < pokerCsvString.size(); i++) {
            Map<String, String> row = new LinkedHashMap<>();
            String[] rowData = pokerCsvString.get(i).split("\t");
            for (int j = 0; j < columns.length; j++) {
                if (columns[j].length() == 0) continue;
                if (j >= rowData.length) continue;
                row.put(columns[j], rowData[j]);
            }
            data.add(row);
        }
    }
}
