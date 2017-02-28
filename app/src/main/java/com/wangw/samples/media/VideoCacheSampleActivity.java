package com.wangw.samples.media;

import android.os.Bundle;
import android.widget.VideoView;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.SamplesApplication;

/**
 * Created by wangw on 2017/2/25.
 */

public class VideoCacheSampleActivity extends BaseActivity {

    private String mUrl = "https://raw.githubusercontent.com/danikula/AndroidVideoCache/master/files/orange1.mp4";
    private VideoView mVideoView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_video_cache);
        mVideoView = (VideoView) findViewById(R.id.videoview);
        onPlayVieo();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void onPlayVieo() {
        String proxyURL = SamplesApplication.getInstance().getVideoCacheServer().getProxyURL(mUrl);
        mVideoView.setVideoPath(proxyURL);
        mVideoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public String getSampleName() {
        return "边观看视频边缓存Sample";
    }
}
