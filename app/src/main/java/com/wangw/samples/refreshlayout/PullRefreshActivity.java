package com.wangw.samples.refreshlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.refreshlayout.pullrefresh.PullRefreshLayout;

import butterknife.Bind;

public class PullRefreshActivity extends BaseRefreshActivity implements PullRefreshLayout.OnRefreshListener, PullRefreshLayout.OnLoadListener {

    @Bind(R.id.pullrefreshview)
    PullRefreshLayout mPullView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_refresh);

        mPullView.setOnLoadListener(this);
        mPullView.setOnRefreshListener(this);
    }

    @Override
    public String getSampleName() {
        return "PullRefreshLayout:继承自LinearLayout，可以支持AdapterView或ScrollView类型的子View";
    }

    @Override
    protected void onComplete() {
        mPullView.onCompleted();
    }

    @Override
    public void onRefresh(PullRefreshLayout refreshLayout) {
        onRefresh();
    }

    @Override
    public void onLoad(PullRefreshLayout refreshLayout) {
        onLoad();
    }
}
