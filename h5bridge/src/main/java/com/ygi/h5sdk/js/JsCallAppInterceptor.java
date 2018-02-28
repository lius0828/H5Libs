package com.ygi.h5sdk.js;

/**
 * 拦截器
 */

public interface JsCallAppInterceptor {
    /**
     * 拦截
     *
     * @param js2AppInfo
     * @return true 拦截，false，不拦截
     */
    boolean intercept(Js2AppInfo js2AppInfo);
}
