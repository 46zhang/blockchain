package com.gdut.fundraising.util;

import java.util.HashMap;
import java.util.Map;

/**
. */
public class JsonResult<T> {
    private int code;
    private T data;
    private String msg;
    private Map<String, Object> add;

    public JsonResult() {
    }

    public JsonResult(int code, String msg, T data) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.add = new HashMap<>();

    }

    public static <T> JsonResult<T> success(T data) {
        return new JsonResult<T>(100, "success",data);
    }

    public static <T> JsonResult<T> error(int code, String msg) {
        return new JsonResult<T>(code, msg, null);
    }
    public static <T> JsonResult<T> error(int code, String msg, T data) {
        return new JsonResult<T> (code, msg, data);
    }

    public JsonResult addKey(String key, Object value){
        this.add.put(key, value);
        return this;
    }

    //生成返回内容
    public Map<String, Object> result(){
        this.add.put("code", this.code);
        this.add.put("data", this.data);
        this.add.put("message", this.msg);
        return this.add;
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    public Map<String, Object> getAdd() {
        return add;
    }

    public void setAdd(Map<String, Object> add) {
        this.add = add;
    }

}
