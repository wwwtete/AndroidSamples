package com.wangw.samples;

import android.app.Application;

import com.exlogcat.L;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by wangw on 2016/4/14.
 */
public class SamplesApplication extends Application {

    private static SamplesApplication mInstance;

    public static SamplesApplication getInstance(){
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        initLog();
        initLeakCanary();
    }

    /**
     * 初始化检测内存泄漏lib包
     */
    private void initLeakCanary() {
        LeakCanary.install(this);
    }

    /**
     * 初始化Log工具包
     */
    private void initLog() {
        L.init("samples")
                .methodCount(2)
                .hideThreadInfo();
    }
}
