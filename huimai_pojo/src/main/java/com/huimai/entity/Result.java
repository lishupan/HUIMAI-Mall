package com.huimai.entity;

import java.io.Serializable;

/**
 * 操作结果封装类
 */
public class Result implements Serializable {

    //操作结果是否成功  true、false
    private boolean success;

    //返回消息提示
    private String message;

    public Result() {
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
