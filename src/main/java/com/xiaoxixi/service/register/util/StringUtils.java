package com.xiaoxixi.service.register.util;

public class StringUtils {

    private StringUtils(){}

    public static boolean isEmpty(String source) {
        return source == null || source.trim().length() == 0;
    }

}