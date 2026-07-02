package com.tg.telegram4j.utils;

import com.alibaba.fastjson2.JSONObject;

/**
 * JSON工具类。
 */
public class JsonUtil {

    public static JSONObject parseObject(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) {
            return new JSONObject();
        }
        return JSONObject.parseObject(jsonStr);
    }

    public static String getString(JSONObject json, String key) {
        if (json == null || key == null) return null;
        return json.getString(key);
    }

    public static Integer getInt(JSONObject json, String key) {
        if (json == null || key == null) return null;
        return json.getInteger(key);
    }

    public static Long getLong(JSONObject json, String key) {
        if (json == null || key == null) return null;
        return json.getLong(key);
    }

    public static Boolean getBoolean(JSONObject json, String key) {
        if (json == null || key == null) return null;
        return json.getBoolean(key);
    }
}
