package com.example.demo;

import com.example.demo.util.Datasource;
import com.example.demo.util.ItemCache;
import com.example.demo.util.Md5Util;
import com.example.demo.util.Page;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@CrossOrigin(origins = "*")
@RestController
public class TestController {
    @Autowired
    private Datasource datasource;

    @Autowired
    private ItemCache itemCache;

    @GetMapping("/test")
    public String test() {
        return "hello";
    }

    @PostMapping("/search")
    @ResponseBody
    public Object search(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "50") Integer count) {
        Assert.isTrue(!StringUtils.isEmptyOrWhitespace(q), "q should not be blank");
        Assert.isTrue(page > 0, "page should greater than 0");
        Assert.isTrue(count < 100 && count > 0, "count should in range (0,100)");
        List<Map<String, String>> data = datasource.getData();
        long totalCount = data.stream().filter(m -> m.get("schi").contains(q)).count();
        List<Map<String, String>> result = data.stream().filter(m -> m.get("schi").contains(q)).skip((long) (page - 1) * count).limit(count).collect(Collectors.toList());
        Page<Map<String, String>> pageResult = new Page<>();
        pageResult.setData(result);
        pageResult.setTotalCount(totalCount);
        pageResult.setPageNo(page);
        pageResult.setTotalPage((int) ((totalCount % count == 0) ? totalCount / count : totalCount / count + 1));
        return pageResult;
    }

    private static final byte[] emptyPadding = {(byte) 0xFE, (byte) 0xFF, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};

    @PostMapping(path = "/genCode")
    public String genCode(String param) {
        String rawKey = System.currentTimeMillis() + param;
        String md5 = Md5Util.myMd5(rawKey);
        itemCache.put(md5, param);
        return md5;
    }

    @PostMapping(path = "/getParam")
    public String downloadByCode(String code) {
        return itemCache.get(code);
    }

    @PostMapping(path = "/download")
    public ResponseEntity<Resource> download(String param) throws IOException {

        if (param == null || param.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
        Arrays.stream(param.split(";"))
                .map(str -> str.split(","))
                .filter(s -> s.length == 3 && s[0].startsWith("0x") && s[1].matches("\\d+")).collect(Collectors.toList())
                .forEach(s -> {
                    int id = Integer.parseUnsignedInt(s[0].replaceAll("0x", ""), 16);
                    short extra = 0;
                    if (s[2].matches("\\d+")) {
                        extra = Short.parseShort(s[2]);
                    }
                    int hex = Integer.parseUnsignedInt(s[1]);

                    ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    byteArrayBuilder.write(byteBuffer.putInt(id).putShort((short) hex).putShort(extra).array());
                });
        // padding
        int d = (byteArrayBuilder.size() / 8) % 40;
        if (d != 0) {
            int times = 40 - d;
            for (int i = 0; i < times; i++) {
                byteArrayBuilder.write(emptyPadding);
            }
        }
        String fileName;
        byte[] bytes;
        if (byteArrayBuilder.size() / 8 <= 40) {
            fileName = "0.nhi";
            bytes = byteArrayBuilder.toByteArray();
        } else {
            fileName = "0.zip";
            byte[] sourceBytes = byteArrayBuilder.toByteArray();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            int loopTimes = sourceBytes.length / 8 / 40;
            for (int i = 0; i < loopTimes; i++) {
                ZipEntry entry = new ZipEntry(i + ".nhi");
                entry.setSize(40 * 8);
                zos.putNextEntry(entry);
                byte[] tmp = new byte[40 * 8];
                System.arraycopy(sourceBytes, i * 40 * 8, tmp, 0, 40 * 8);
                zos.write(tmp);
                zos.closeEntry();
            }
            zos.close();
            bytes = baos.toByteArray();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
