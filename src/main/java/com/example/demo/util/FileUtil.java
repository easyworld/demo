package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static List<String> readAll(String path) throws Exception {
        List<String> result = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) return Collections.emptyList();
        try (BufferedReader br = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8))) {
            for (String str = br.readLine(); str != null; str = br.readLine()) {
                result.add(str);
            }
        }
        return result;
    }

    public static void appendSave(String path, String key, String value) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path, true))) {
            pw.println(key + "$" + value + "$" + System.currentTimeMillis());
        } catch (Exception ex) {
            logger.error("Write error", ex);
        }
    }
}
