package com.example.demo.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ItemCache implements InitializingBean {

    @Value("${duration.days}")
    private Integer durationDays;

    private static final Logger logger = LoggerFactory.getLogger(ItemCache.class);

    Cache<String, String> itemCache = null;

    private static final String DB_FILE_NAME = "db.txt";

    @Override
    public void afterPropertiesSet() throws Exception {
        itemCache = CacheBuilder.newBuilder()
                .maximumSize(100000)
                .expireAfterWrite(durationDays, TimeUnit.DAYS)
                .build();
        int count = 0;
        List<String> strings = FileUtil.readAll(DB_FILE_NAME);
        long currentTimeMillis = System.currentTimeMillis();
        for (String string : strings) {
            String[] split = string.split("\\$");
            if (split.length == 3 && split[2].matches("\\d+")
                    && (currentTimeMillis - Long.parseLong(split[2]) <= 1000 * 3600 * 24L * durationDays)) {
                count++;
                itemCache.put(split[0], split[1]);
            }
        }
        logger.info("Load {} data from db.txt", count);
    }

    public void put(String key, String value) {
        itemCache.put(key, value);
        synchronized (this) {
            FileUtil.appendSave(DB_FILE_NAME, key, value);
        }
    }

    public String get(String key) {
        return itemCache.getIfPresent(key);
    }
}
