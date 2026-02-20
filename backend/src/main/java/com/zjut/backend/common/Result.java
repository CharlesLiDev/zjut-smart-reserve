package com.zjut.backend.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;   //状态码
    private String msg; //提示信息
    private T data; //实际信息（用户信息）

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<T>();
        result.setCode(400);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> forcePasswordChange(T data) {
        Result<T> result = new Result<T>();
        result.setCode(403);    //需要强制修改密码的错误代码
        result.setMsg("首次登录，请修改密码。");
        result.setData(data);
        return result;
    }
}
