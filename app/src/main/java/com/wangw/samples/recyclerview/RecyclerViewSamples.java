package com.wangw.samples.recyclerview;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;

/**
 * Created by wangw on 2016/12/14.
 */

public class RecyclerViewSamples extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultView();
        addSampleClass(StickyHeaderActivity.class);
        addSampleClass(ItemDecorActivity.class);
    }

    @Override
    public String getSampleName() {
        return "RecyclerView高级应用";
    }
}
