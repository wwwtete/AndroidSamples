package com.wangw.samples.density;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;

/**
 * Created by wangw on 2016/8/11.
 */
public class HomeDensityActivity extends BaseActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultView();
        addSampleClass(ImageDensityActivity.class);
    }

    @Override
    public String getSampleName() {
        return "屏幕适配，分辨率换算";
    }
}
