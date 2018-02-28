package com.ygi.h5sdk.js;

import android.webkit.WebView;

public interface WebViewClientDelegate {
    boolean shouldOverrideUrlLoading(WebView view, String url);
}
