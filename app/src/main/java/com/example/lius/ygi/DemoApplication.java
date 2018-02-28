package com.example.lius.ygi;

import android.app.Application;

import com.hyphenate.easeui.EaseUI;


public class DemoApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        EaseUI.getInstance().init(this, null);
    }
    
}
