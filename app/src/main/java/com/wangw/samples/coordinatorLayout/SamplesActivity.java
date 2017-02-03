package com.wangw.samples.coordinatorLayout;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;

/**
 * Created by wangw on 2017/1/22.
 */

public class SamplesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultView();
        addSampleClass(AppBarSampleActivity.class);
    }

    @Override
    public String getSampleName() {
        return " Material Design 设计相关Sample";
    }
}
