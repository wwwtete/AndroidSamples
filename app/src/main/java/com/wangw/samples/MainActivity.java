package com.wangw.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.wangw.commonadapter.RecyclerViewAdapter;
import com.wangw.samples.comm.SamplesAdapter;
import com.wangw.samples.comm.SamplesModel;
import com.wangw.samples.exoplayer.ExoPlayerSamplesActivity;
import com.wangw.samples.refreshlayout.RefreshSamplesActivity;
import com.wangw.samples.sample_leakcanary.LeakCanaryActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Android项目Sample
 * Created by wangw on 2016/4/14.
 */
public class MainActivity extends BaseActivity implements RecyclerViewAdapter.OnItemClickListener<SamplesModel> {


    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerview;

    private SamplesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initRecyclerView();

        addSample(LeakCanaryActivity.class);
        addSample(RefreshSamplesActivity.class);
        addSample(ExoPlayerSamplesActivity.class);

    }

    private void addSample(Class clz){
        try {
            mAdapter.add((SamplesModel) clz.newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private void initRecyclerView() {
        mAdapter = new SamplesAdapter(this);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerview.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(View view, SamplesModel data, int position) {

        if(BaseActivity.class.isAssignableFrom(data.getClass()))
            startActivity(new Intent(MainActivity.this, data.getClass()));
    }

    @Override
    public String getSampleName() {
        return null;
    }
}
