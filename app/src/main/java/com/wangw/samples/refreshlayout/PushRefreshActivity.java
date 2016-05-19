package com.wangw.samples.refreshlayout;

import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;

import com.wangw.samples.R;
import com.wangw.samples.refreshlayout.view.PushLoadLayout;

import butterknife.Bind;

public class PushRefreshActivity extends BaseRefreshActivity implements PushLoadLayout.OnLoadListener, SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.refreshview)
    PushLoadLayout mRefreshview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_refresh);
        mRefreshview.setOnLoadListener(this);
        mRefreshview.setOnRefreshListener(this);
    }

    @Override
    protected void onComplete() {
        mRefreshview.onFinish();
    }

    @Override
    public String getSampleName() {
        return "PushLoadLayout:扩展SwipeRefreshLayout，只支持ListView";
    }
}
