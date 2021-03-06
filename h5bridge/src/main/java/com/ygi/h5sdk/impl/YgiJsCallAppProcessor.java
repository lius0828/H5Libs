package com.ygi.h5sdk.impl;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.ygi.h5sdk.H5SDKHelper;
import com.ygi.h5sdk.capture.Capture;
import com.ygi.h5sdk.capture.CaptureCallback;
import com.ygi.h5sdk.capture.CaptureConfig;
import com.ygi.h5sdk.capture.CustomCaptureImpl;
import com.ygi.h5sdk.js.JsCallAppCallback;
import com.ygi.h5sdk.perm.PermChecker;
import com.ygi.h5sdk.ui.PermRequestActivity;
import com.ygi.h5sdk.ui.PhotoPreviewActivity;
import com.ygi.h5sdk.ui.WebActivity;
import com.ygi.h5sdk.utils.EncodeUtils;
import com.ygi.h5sdk.utils.ImageUtils;
import com.ygi.h5sdk.utils.UploadUtils;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ygi.h5sdk.js.JsCallAppErrorCode.ERROR_IMAGE_HANDLE;
import static com.ygi.h5sdk.js.JsCallAppErrorCode.ERROR_NO_BANNER;
import static com.ygi.h5sdk.js.JsCallAppErrorCode.ERROR_NO_BANNER_URL;
import static com.ygi.h5sdk.js.JsCallAppErrorCode.ERROR_NO_CAMERA_PERM;
import static com.ygi.h5sdk.js.JsCallAppErrorCode.ERROR_PAY_APP_NOT_INSTALL;
import static com.ygi.h5sdk.js.JsCallAppErrorCode.ERROR_REQUEST_PERM_FAIL;
import static com.ygi.h5sdk.js.JsCallAppErrorCode.ERROR_UPLOAD_FAIL;
import static com.ygi.h5sdk.js.JsCallAppErrorCode.SUCCESS;

/**
 * 2017/10/13.
 */

public class YgiJsCallAppProcessor implements YgiJsCallAppProcess {

    protected WeakReference<Context> contextRef;
    protected WeakReference<Activity> activityRef;
    protected WeakReference<Fragment> fragmentRef;

    protected String bannerUrl;
    protected BannerCallback bannerCallback;

    /**
     * 拍照参数暂存
     */
    protected ygiJs2AppInfo captureInfo;
    protected ygiJsParser captureParser;
    protected JsCallAppCallback captureCallback;
    /**
     * 支付参数暂存
     */
    protected ygiJs2AppInfo payInfo;
    protected ygiJsParser payParser;
    protected JsCallAppCallback payCallback;

    protected String cameraFilePath;//拍照图片存放地址

    protected Capture capture;

    public ygiJsCallAppProcessor(Activity activity) {
        activityRef = new WeakReference<>(activity);
        contextRef = new WeakReference<Context>(activity);
        init();
    }

    public ygiJsCallAppProcessor(Fragment fragment) {
        fragmentRef = new WeakReference<>(fragment);
        contextRef = new WeakReference<>(fragment.getContext());
        init();
    }

    protected void init() {
        capture = new CustomCaptureImpl();
    }

    /**
     * 获取广告图片地址
     *
     * @param bannerImageUrl
     * @param callback
     */
    public void setBanner(String bannerImageUrl, BannerCallback callback) {
        bannerUrl = bannerImageUrl;
        this.bannerCallback = callback;
    }

    /**
     * 设置自定义拍照实现
     *
     * @param capture
     */
    public void setCapture(Capture capture) {
        this.capture = capture;
    }

    /**
     * 拍照
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void cameraGetImage(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback) {
        captureInfo = js2AppInfo;
        captureParser = parser;
        captureCallback = callback;
        String[] perms = new String[]{Manifest.permission.CAMERA};
        if (PermChecker.hasPermissions(contextRef.get(), perms)) {
            if (!startCapturePic()) {
                if (captureCallback != null) {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("code", ERROR_NO_CAMERA_PERM.getCode());
                    errorData.put("message", ERROR_NO_CAMERA_PERM.getMsg());
                    ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                    captureCallback.jsCallAppFail(captureInfo, app2JsInfo, captureParser);
                }
            }
        } else {
            //直接请求
            if (contextRef != null && contextRef.get() != null) {
                PermRequestActivity.requestPermissions(contextRef.get(), perms, new PermChecker.RequestPermCallback() {
                    @Override
                    public void onRequestSuccess() {
                        if (!startCapturePic()) {
                            if (captureCallback != null) {
                                Map<String, Object> errorData = new HashMap<>();
                                errorData.put("code", ERROR_NO_CAMERA_PERM.getCode());
                                errorData.put("message", ERROR_NO_CAMERA_PERM.getMsg());
                                ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                                captureCallback.jsCallAppFail(captureInfo, app2JsInfo, captureParser);
                            }
                        }
                    }

                    @Override
                    public void onRequestFail() {
                        if (captureCallback != null) {
                            Map<String, Object> errorData = new HashMap<>();
                            errorData.put("code", ERROR_NO_CAMERA_PERM.getCode());
                            errorData.put("message", ERROR_NO_CAMERA_PERM.getMsg());
                            ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                            captureCallback.jsCallAppFail(captureInfo, app2JsInfo, captureParser);
                        }
                    }
                });
            }
        }
    }

    /**
     * 原图预览
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void previewImage(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback) {
        JSONObject dataObj = js2AppInfo.getDataObj();
        if (dataObj != null) {
            String path = dataObj.optString("localId");
            if (!TextUtils.isEmpty(path)) {
                if (contextRef != null && contextRef.get() != null) {
                    Intent intent = new Intent(contextRef.get(), PhotoPreviewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("path", path);
                    contextRef.get().startActivity(intent);
                    if (callback != null) {
                        Map<String, Object> successData = new HashMap<>();
                        successData.put("code", SUCCESS.getCode());
                        successData.put("message", SUCCESS.getMsg());
                        ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(successData);
                        callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
                    }
                    return;
                }
                if (callback != null) {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("code", "-1");
                    errorData.put("message", "APP页面不存在");
                    ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                    callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                }
                return;
            }
        }
        if (callback != null) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("code", "-1");
            errorData.put("message", "H5参数错误");
            ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
            callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
        }
    }

    /**
     * 图片上传
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void uploadImage(final ygiJs2AppInfo js2AppInfo, final ygiJsParser parser, final JsCallAppCallback callback) {
        JSONObject dataObj = js2AppInfo.getDataObj();
        if (dataObj != null) {
            int width = dataObj.optInt("width", Integer.MAX_VALUE);
            int height = dataObj.optInt("height", Integer.MAX_VALUE);
            String token = dataObj.optString("token");
            String key = dataObj.optString("key");
            String file = dataObj.optString("file");
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(file)) {
                Bitmap bitmap = ImageUtils.getBitmap(file, width, height);
                if (bitmap != null) {
                    UploadUtils.getUploadManager().put(ImageUtils.bitmap2Bytes(bitmap, Bitmap.CompressFormat.JPEG), key, token, new UpCompletionHandler() {
                        @Override
                        public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                            if (callback != null) {
                                if (responseInfo.isOK()) {
                                    ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(jsonObject.toString());
                                    callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
                                } else {
                                    Map<String, Object> errorData = new HashMap<>();
                                    errorData.put("code", ERROR_UPLOAD_FAIL.getCode());
                                    errorData.put("message", ERROR_UPLOAD_FAIL.getMsg());
                                    ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                                    callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                                }
                            }
                        }
                    }, new UploadOptions(null, null, false, null, null, null));
                } else {
                    if (callback != null) {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", "-1");
                        errorData.put("message", "本地图片不存在");
                        ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                        callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                    }
                }
                return;
            }
        }
        if (callback != null) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("code", "-1");
            errorData.put("message", "H5参数错误");
            ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
            callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
        }
    }

    /**
     * 拉起支付 App
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void openPayApp(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback) {
        payInfo = js2AppInfo;
        payParser = parser;
        payCallback = callback;
        JSONObject dataObj = js2AppInfo.getDataObj();
        if (dataObj != null) {
            String url = dataObj.optString("url");
            String referer = dataObj.optString("redirectUrl");
            String scheme = dataObj.optString("scheme");
            if (!TextUtils.isEmpty(scheme)) {
                Intent schemeIntent = new Intent(Intent.ACTION_VIEW);
                schemeIntent.setData(Uri.parse(scheme));
                PackageManager packageManager = contextRef.get().getPackageManager();
                if (schemeIntent.resolveActivity(packageManager) == null) {
                    if (payCallback != null) {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", ERROR_PAY_APP_NOT_INSTALL.getCode());
                        errorData.put("message", ERROR_PAY_APP_NOT_INSTALL.getMsg());
                        ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                        payCallback.jsCallAppFail(payInfo, app2JsInfo, payParser);
                    }
                    return;
                }
            }
            if (!TextUtils.isEmpty(url)) {
                if (contextRef != null && contextRef.get() != null) {
                    WebActivity.startPay(contextRef.get(), url, referer, new WebActivity.Callback() {
                        @Override
                        public void onSuccess() {
                            if (payCallback != null) {
                                Map<String, Object> successData = new HashMap<>();
                                successData.put("code", SUCCESS.getCode());
                                successData.put("message", SUCCESS.getMsg());
                                ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(successData);
                                payCallback.jsCallAppSuccess(payInfo, app2JsInfo, payParser);
                            }
                        }

                        @Override
                        public void onFail(String errCode, String errMsg) {
                            if (payCallback != null) {
                                Map<String, Object> errorData = new HashMap<>();
                                errorData.put("code", errCode);
                                errorData.put("message", errMsg);
                                ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                                payCallback.jsCallAppFail(payInfo, app2JsInfo, payParser);
                            }
                        }
                    });
                    return;
                }
                if (callback != null) {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("code", "-1");
                    errorData.put("message", "APP页面不存在");
                    ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                    callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                }
                return;
            }
        }
        if (callback != null) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("code", "-1");
            errorData.put("message", "H5参数错误");
            ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
            callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
        }
    }

    /**
     * 设置广告图片
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void getAdBannerURL(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback) {
        if (!TextUtils.isEmpty(bannerUrl)) {
            if (callback != null) {
                Map<String, Object> successData = new HashMap<>();
                successData.put("url", bannerUrl);
                ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(successData);
                callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
            }
        } else {
            if (callback != null) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("code", ERROR_NO_BANNER_URL.getCode());
                errorData.put("message", ERROR_NO_BANNER_URL.getMsg());
                ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
            }
        }

    }

    /**
     * 广告图片点击事件
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void adClick(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback) {
        if (callback != null) {
            Map<String, Object> successData = new HashMap<>();
            successData.put("code", SUCCESS.getCode());
            successData.put("message", SUCCESS.getMsg());
            ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(successData);
            callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
        }
        if (bannerCallback != null) {
            bannerCallback.onBannerClick();
        } else {
            if (callback != null) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("code", ERROR_NO_BANNER.getCode());
                errorData.put("message", ERROR_NO_BANNER.getMsg());
                ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
            }
        }
    }

    /**
     * 获取SDK版本信息
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void getAppInfo(ygiJs2AppInfo js2AppInfo, ygiJsParser parser, JsCallAppCallback callback) {
        if (callback != null) {
            Map<String, Object> successData = new HashMap<>();
            successData.put("version", H5SDKHelper.getSdkVersion());
            ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(successData);
            callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
        }
    }

    /**
     * 申请权根（拍照、拍视频）
     *
     * @param js2AppInfo
     * @param parser
     * @param callback
     */
    @Override
    public void authorization(final ygiJs2AppInfo js2AppInfo, final ygiJsParser parser, final JsCallAppCallback callback) {
        JSONObject dataObj = js2AppInfo.getDataObj();
        if (dataObj != null) {
            List<String> permList = new ArrayList<>();
            boolean image = dataObj.optBoolean("image");
            boolean video = dataObj.optBoolean("video");
            if (image) {
                if (!permList.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    permList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                if (!permList.contains(Manifest.permission.CAMERA)) {
                    permList.add(Manifest.permission.CAMERA);
                }
            }
            if (video) {
                if (!permList.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    permList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                if (!permList.contains(Manifest.permission.CAMERA)) {
                    permList.add(Manifest.permission.CAMERA);
                }
                if (!permList.contains(Manifest.permission.RECORD_AUDIO)) {
                    permList.add(Manifest.permission.RECORD_AUDIO);
                }
            }
            if (!permList.isEmpty()) {
                if (contextRef != null && contextRef.get() != null) {
                    String[] perms = permList.toArray(new String[permList.size()]);
                    if (PermChecker.hasPermissions(contextRef.get(), perms)) {
                        if (callback != null) {
                            Map<String, Object> successData = new HashMap<>();
                            successData.put("code", SUCCESS.getCode());
                            successData.put("message", SUCCESS.getMsg());
                            ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(successData);
                            callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
                        }
                    } else {
                        PermRequestActivity.requestPermissions(contextRef.get(), perms, new PermChecker.RequestPermCallback() {
                            @Override
                            public void onRequestSuccess() {
                                if (callback != null) {
                                    Map<String, Object> successData = new HashMap<>();
                                    successData.put("code", SUCCESS.getCode());
                                    successData.put("message", SUCCESS.getMsg());
                                    ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(successData);
                                    callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
                                }
                            }

                            @Override
                            public void onRequestFail() {
                                if (callback != null) {
                                    Map<String, Object> errorData = new HashMap<>();
                                    errorData.put("code", ERROR_REQUEST_PERM_FAIL.getCode());
                                    errorData.put("message", ERROR_REQUEST_PERM_FAIL.getMsg());
                                    ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                                    callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                                }
                            }
                        });
                    }
                } else {
                    if (callback != null) {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", ERROR_REQUEST_PERM_FAIL.getCode());
                        errorData.put("message", ERROR_REQUEST_PERM_FAIL.getMsg());
                        ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                        callback.jsCallAppFail(js2AppInfo, app2JsInfo, parser);
                    }
                }
            } else {
                if (callback != null) {
                    Map<String, Object> successData = new HashMap<>();
                    successData.put("code", SUCCESS.getCode());
                    successData.put("message", SUCCESS.getMsg());
                    ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(successData);
                    callback.jsCallAppSuccess(js2AppInfo, app2JsInfo, parser);
                }
            }
        }
    }

    /**
     * 调用相机拍照
     */
    public boolean startCapturePic() {
        if (contextRef.get() != null) {
            File cacheDir = contextRef.get().getExternalCacheDir();
            if (cacheDir == null) {
                cacheDir = contextRef.get().getCacheDir();
            }
            cameraFilePath = cacheDir.getAbsolutePath() + File.separator + "image_" + System.currentTimeMillis();
            CaptureConfig captureConfig = new CaptureConfig(false, false, 0);
            JSONObject dataObj = captureInfo.getDataObj();
            int thumbWidth = dataObj.optInt("thumbWidth", Integer.MAX_VALUE);
            int defaultDirection = dataObj.optInt("defaultDirection", Integer.MAX_VALUE);
            if (!dataObj.has("imageType")) {//没有类型以缩略图宽度来区分
                if (thumbWidth > 480) {
                    captureConfig.landscape = false;
                    captureConfig.imageType = CaptureConfig.IMAGE_TYPE_IN_HAND;
                } else {
                    captureConfig.landscape = true;
                    captureConfig.imageType = CaptureConfig.IMAGE_TYPE_FRONT;//这里没法判断是身份证正面还是反面，默认正面
                }
            } else {
                int imageType = dataObj.optInt("imageType", CaptureConfig.IMAGE_TYPE_IN_HAND);
                captureConfig.imageType = imageType;
                if (imageType == CaptureConfig.IMAGE_TYPE_IN_HAND) {
                    captureConfig.landscape = false;
                } else {
                    captureConfig.landscape = true;
                }
            }
            if (defaultDirection == 0) {
                captureConfig.defaultFrontCamera = true;
            } else {
                captureConfig.defaultFrontCamera = false;
            }

            CaptureCallback callback = new CaptureCallback() {
                @Override
                public void onSuccess(String savePath) {
                    //拍照成功
                    JSONObject dataObj = captureInfo.getDataObj();
                    int thumbWidth = dataObj.optInt("thumbWidth", Integer.MAX_VALUE);
                    int thumbHeight = dataObj.optInt("thumbHeight", Integer.MAX_VALUE);
                    Bitmap bitmap = ImageUtils.getBitmap(cameraFilePath, thumbWidth, thumbHeight);
                    if (bitmap != null) {
                        byte[] bytes = ImageUtils.bitmap2Bytes(bitmap, Bitmap.CompressFormat.JPEG);
                        bitmap.recycle();
                        String base64Encode2String = EncodeUtils.base64Encode2String(bytes);
                        if (captureCallback != null) {
                            Map<String, Object> successData = new HashMap<>();
                            successData.put("base64", base64Encode2String);
                            successData.put("localId", cameraFilePath);
                            ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(successData);
                            captureCallback.jsCallAppSuccess(captureInfo, app2JsInfo, captureParser);
                        }
                    } else {
                        if (captureCallback != null) {
                            Map<String, Object> errorData = new HashMap<>();
                            errorData.put("code", ERROR_IMAGE_HANDLE.getCode());
                            errorData.put("message", ERROR_IMAGE_HANDLE.getMsg());
                            ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                            captureCallback.jsCallAppFail(captureInfo, app2JsInfo, captureParser);
                        }
                    }
                }

                @Override
                public void onFail(String errCode, String errMsg) {
                    if (captureCallback != null) {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", errCode);
                        errorData.put("message", errMsg);
                        ygiApp2JsInfo app2JsInfo = new ygiApp2JsInfo(errorData);
                        captureCallback.jsCallAppFail(captureInfo, app2JsInfo, captureParser);
                    }
                }
            };
            if (capture != null) {
                capture.startCapture(contextRef.get(), captureConfig, cameraFilePath, callback);
            }
        }
        return true;
    }
}
