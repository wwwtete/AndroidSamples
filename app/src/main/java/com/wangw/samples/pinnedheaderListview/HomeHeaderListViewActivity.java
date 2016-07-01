package com.wangw.samples.pinnedheaderListview;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;

/**
 * Created by wangw on 2016/7/1.
 */
public class HomeHeaderListViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultView();
        addSampleClass(DefaultPinnedheaderlistViewSample.class);
        addSampleClass(ExPinnedHeaderListViewActivity.class);
    }

    @Override
    public String getSampleName() {
        return "仿照外卖菜品列表的Sample";
    }
}
