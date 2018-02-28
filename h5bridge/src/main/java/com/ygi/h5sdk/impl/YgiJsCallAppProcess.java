package com.ygi.h5sdk.impl;


import com.ygi.h5sdk.js.JsCallAppCallback;
import com.ygi.h5sdk.js.JsCallAppProcess;


public interface YgiJsCallAppProcess extends JsCallAppProcess {
    /**
     * 拍照
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void cameraGetImage(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback);

    /**
     * 原图预览
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void previewImage(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback);

    /**
     * 图片上传
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void uploadImage(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback);

    /**
     * 拉起 App
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void openPayApp(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback);

    /**
     * 获取广告图片地址
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void getAdBannerURL(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback);

    /**
     * 广告图片点击事件
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void adClick(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback);

    /**
     * 获取SDK版本信息
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void getAppInfo(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback);

    /**
     * 申请权根（拍照、拍视频）
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    void authorization(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback);
}