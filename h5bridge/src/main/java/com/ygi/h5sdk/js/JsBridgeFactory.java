package com.ygi.h5sdk.js;

import android.webkit.WebView;

public interface JsBridgeFactory {
    /**
     * 提供JsBridge
     *
     * @param webView
     */
    Object createJsBridge(WebView webView);
}
