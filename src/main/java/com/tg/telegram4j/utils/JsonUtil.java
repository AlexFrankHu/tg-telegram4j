package com.tg.telegram4j.utils;

import com.alibaba.fastjson.JSONObject;

public class JsonUtil {

    public static JSONObject getJson(JSONObject json, String tag) {
        JSONObject ret = null;
        if(json.containsKey(tag)) {
            ret = json.getJSONObject(tag);
        }
        return ret;

    }

    public static Integer getInt(JSONObject jsonObject, String key, Integer defaultValue) {
        try {
            if (!jsonObject.containsKey(key)) {
                return defaultValue;
            }
            Integer result = jsonObject.getInteger(key);
            if (result == null) {
                return defaultValue;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultValue;
    }

    public static Long getLong(JSONObject jsonObject, String key, Long defaultValue) {
        try {
            if (!jsonObject.containsKey(key)) {
                return defaultValue;
            }
            Long result = jsonObject.getLong(key);
            if (result == null) {
                return defaultValue;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultValue;
    }

    public static Long getLong(JSONObject jsonObject, String key) {
        return getLong(jsonObject, key, 0l);
    }

    public static Integer getInt(JSONObject jsonObject, String key) {
        return getInt(jsonObject, key, 0);
    }

    public static String getString(JSONObject jsonObject, String key, String defaultValue) {
        try {
            if (!jsonObject.containsKey(key)) {
                return defaultValue;
            }
            String result = jsonObject.getString(key);
            if (result == null) {
                return defaultValue;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultValue;
    }



    public static String getString(JSONObject jsonObject, String key) {
        return getString(jsonObject, key, "");
    }

    public static JSONObject getJSONObject(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getJSONObject(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
