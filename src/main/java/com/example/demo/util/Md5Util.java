package com.example.demo.util;

import org.springframework.util.DigestUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

public class Md5Util {
    private static final char[] chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public static String myMd5(String str) {
        String md5 = DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
        return to62String(md5);
    }

    public static String to62String(String num) {
        BigInteger bigNum = new BigInteger(num, 16);
        BigInteger radix = BigInteger.valueOf(chars.length);
        Stack<Character> stack = new Stack<>();
        StringBuilder result = new StringBuilder();
        while (!bigNum.equals(BigInteger.ZERO)) {
            stack.add(chars[bigNum.remainder(radix).intValue()]);
            bigNum = bigNum.divide(radix);
        }
        while (!stack.isEmpty()) {
            result.append(stack.pop());
        }
        return result.length() == 0 ? "0" : result.toString();
    }
}
