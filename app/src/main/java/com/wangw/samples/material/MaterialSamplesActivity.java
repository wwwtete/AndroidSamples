package com.wangw.samples.material;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;

/**
 * Created by wangw on 2016/12/8.
 */

public class MaterialSamplesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultView();
        addSampleClass(ScrollingActivity.class);
    }

    @Override
    public String getSampleName() {
        return "Material设计的一些Sample";
    }
}
