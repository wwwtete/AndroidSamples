package com.wangw.samples.coordinatorLayout;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.comm.CommonTestAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wangw on 2017/1/22.
 */

public class AppBarSampleActivity extends BaseActivity {

    @Bind(R.id.lv)
    RecyclerView mLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appbar_sample);
        ButterKnife.bind(this);

        onInitView();
    }

    private void onInitView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mLv.setLayoutManager(layoutManager);
        CommonTestAdapter adapter = new CommonTestAdapter(30);
        mLv.setAdapter(adapter);
    }

    @Override
    public String getSampleName() {
        return "AppBarLayout";
    }


}
