package com.hyphenate.easeuisimpledemo.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hyphenate.easeuisimpledemo.R;

/**
 * Created by lius on 2018/1/19.
 */

public class AnrSimpleActivity extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler);
        Log.d("Tag","current Thread's name "+Thread.currentThread().getName());

        Thread thread = Thread.currentThread();
        try {
            thread.sleep(5*1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
