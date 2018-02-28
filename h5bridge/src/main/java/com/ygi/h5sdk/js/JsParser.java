package com.ygi.h5sdk.js;

public interface JsParser<T extends Js2AppInfo, R extends App2JsInfo> {
    /**
     * 将String解析成Js2AppInfo
     *
     * @param params
     * @return
     */
    T decode(String params);

    /**
     * 将App2JsInfo封装成String
     *
     * @param success
     * @param js2AppInfo
     * @param app2JsInfo
     * @return
     */
    String encode(boolean success, T js2AppInfo, R app2JsInfo);
}
