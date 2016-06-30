package com.wangw.samples.refreshlayout;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.comm.SampleListView;

import butterknife.Bind;

public class HomeRefreshActivity extends BaseActivity {


    @Bind(R.id.listview)
    SampleListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("RefreshLayout Samples");

        initView();
    }

    private void initView() {
        mListView.addSample(PushRefreshActivity.class);
        mListView.addSample(PullRefreshActivity.class);
        mListView.addSample(RefreshLayoutActivity.class);
    }

    @Override
    public String getSampleName() {
        return "各种下拉刷新组件功能列表";
    }
}
