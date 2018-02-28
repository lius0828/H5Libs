package com.ygi.h5sdk.capture;

import android.content.Context;

import com.ygi.h5sdk.ui.CaptureActivity;

/**
 * 系统相机
 *
 * @date 2017/11/28
 */

public class SystemCaptureImpl implements Capture {

    @Override
    public void startCapture(Context context, CaptureConfig captureConfig, String savePath, CaptureCallback captureCallback) {
        CaptureActivity.startCapture(context, savePath, captureCallback);
    }
}
