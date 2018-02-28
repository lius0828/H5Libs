package com.ygi.h5sdk.js;


public interface JsCallAppCallback {
    /**
     * 调用成功
     *
     * @param js2AppInfo
     * @param app2JsInfo
     */
    void jsCallAppSuccess(Js2AppInfo js2AppInfo, App2JsInfo app2JsInfo, JsParser encoder);

    /**
     * 调用失败
     *
     * @param js2AppInfo
     * @param app2JsInfo
     */
    void jsCallAppFail(Js2AppInfo js2AppInfo, App2JsInfo app2JsInfo, JsParser encoder);
}
