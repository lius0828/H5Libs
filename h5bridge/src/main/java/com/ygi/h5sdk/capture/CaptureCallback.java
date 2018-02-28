package com.ygi.h5sdk.capture;

/**
 * 拍照回调
 *
 * @date 2017/11/28
 */

public interface CaptureCallback {
    /**
     * 拍照成功
     *
     * @param savePath
     */
    void onSuccess(String savePath);

    /**
     * 拍照失败
     *
     * @param errCode
     * @param errMsg
     */
    void onFail(String errCode, String errMsg);
}
