package com.wangw.samples.media;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;

import java.io.File;

/**
 * 多媒体相关的列子
 * Created by wangw on 2017/2/3.
 */

public class SampleActivity extends BaseActivity {

    public final static String ROOT_DIR = "/sdcard/mediademo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultView();
        onInitDir();
        addSampleClass(VideoCacheSampleActivity.class);
        addSampleClass(MediaCodecSampleActivity.class);
        addSampleClass(AudioSamplesActivity.class);
        addSampleClass(VideoSamplesActivity.class);
        addSampleClass(VideoAndAudioSampleActivity.class);
    }

    private void onInitDir() {
        File file = new File(ROOT_DIR);
        if (!file.exists()){
            file.mkdir();
        }
    }

    @Override
    public String getSampleName() {
        return "音视频多媒体相关的例子";
    }
}
