package com.wangw.samples.materialrangebar;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;

public class HomeMaterialrangebarActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultView();
        addSampleClass(DefaultMaterialrangebarActivity.class);
    }

    @Override
    public String getSampleName() {
        return "范围拖拽控件";
    }
}
