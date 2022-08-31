package com.example.demo.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ItemCache implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ItemCache.class);

    Cache<String, String> itemCache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .expireAfterWrite(3, TimeUnit.DAYS)
            .build();

    private static final String DB_FILE_NAME = "db.txt";

    @Override
    public void afterPropertiesSet() throws Exception {
        int count = 0;
        List<String> strings = FileUtil.readAll(DB_FILE_NAME);
        for (String string : strings) {
            String[] split = string.split("\\$");
            if (split.length == 2) {
                count++;
                itemCache.put(split[0], split[1]);
            }
        }
        logger.info("Load {} data from db.txt", count);
    }

    public void put(String key, String value) {
        itemCache.put(key, value);
        FileUtil.appendSave(DB_FILE_NAME, key, value);
    }

    public String get(String key) {
        return itemCache.getIfPresent(key);
    }
}
