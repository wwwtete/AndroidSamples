package com.wangw.samples.opengl;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;

/**
 * Created by wangw on 2017/3/21.
 */

public class OpenGlSampleActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultView();

        addSampleClass(FristSampleActivity.class);

    }

    @Override
    public String getSampleName() {
        return "OpenGl Samples";
    }
}
