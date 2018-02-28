package com.ygi.h5sdk.impl;

import com.ygi.h5sdk.js.App2JsInfo;

/**
 * APP回应JS调用所传对象
 */

public class YgiApp2JsInfo<T> extends App2JsInfo {
    private T data;

    public YgiApp2JsInfo(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
