package com.wangw.samples.popupwindow;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;

/**
 * PopupWindow Samples
 */
public class HomePopupWindowActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultView();
        addSampleClass(DefaultPopupWindowSample.class);
    }

    @Override
    public String getSampleName() {
        return "各种PopupWindow Samples";
    }
}
