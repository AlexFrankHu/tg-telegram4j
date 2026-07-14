package com.tg.telegram4j.model;

import lombok.Data;

@Data
public class ResultInfo {

    private Integer code;
    private String msg;
    private Object object;

    public ResultInfo(Integer code, String msg, Object object) {
        this.code = code;
        this.msg = msg;
        this.object = object;
    }

    public boolean isSuccess() {
        return code.intValue() == 0;
    }

    public static ResultInfo success() {
        return new ResultInfo(0,"", null);
    }

    public static ResultInfo success(Object object) {
        return new ResultInfo(0,"", object);
    }

    public static ResultInfo error(Integer code, String msg) {
        return new ResultInfo(code, msg, null);
    }

    public static ResultInfo error(String msg) {
        return new ResultInfo(1000, msg, null);
    }

}
