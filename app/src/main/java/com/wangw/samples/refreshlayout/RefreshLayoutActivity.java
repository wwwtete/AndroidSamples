package com.wangw.samples.refreshlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

public class RefreshLayoutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_layout);
    }

    @Override
    public String getSampleName() {
        return "扩展SwipeRefreshLayout，支持各种View";
    }
}
