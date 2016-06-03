package com.wangw.samples.exoplayer;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.comm.SampleListView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ExoPlayerSamplesActivity extends BaseActivity {

    @Bind(R.id.listview)
    SampleListView mListview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player_samples);
        ButterKnife.bind(this);
        onInitView();
    }

    private void onInitView() {
        mListview.addSample(SimpleExoPlayerActivity.class);
    }

    @Override
    public String getSampleName() {
        return "ExoPlayer Samples";
    }
}
