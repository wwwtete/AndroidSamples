package com.wangw.samples.fresco;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;

/**
 * Created by wangw on 2016/12/24.
 */

public class FrescoSampleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultView();
        addSampleClass(FristSampleActiity.class);
    }

    @Override
    public String getSampleName() {
        return "Fresco图片加载框架Sample";
    }
}
