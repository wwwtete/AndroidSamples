package com.wangw.samples.media;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.comm.CameraView;
import com.wangw.samples.comm.FrameCallback;
import com.wangw.samples.media.task.ExAudioRecordTask;
import com.wangw.samples.media.task.VideoEncoder;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wangw on 2017/2/9.
 */

public class VideoAndAudioSampleActivity extends BaseActivity implements FrameCallback {

    @Bind(R.id.camera)
    CameraView mCamera;
    @Bind(R.id.btn_recod)
    Button mBtnRecod;

    private boolean mRecoding;
    private VideoEncoder mVideoEncoder;
    private ExAudioRecordTask mAudioRecordTask;


    private String mVideoPath;
    private String mAudioPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_audio);
        ButterKnife.bind(this);

        mCamera.setCallback(this);

    }

    @OnClick({R.id.btn_recod})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_recod:
                onRecod();
                break;
        }
    }

    private void onRecod() {
        if (mRecoding){
            mRecoding = false;
            onStopRecod();
            mBtnRecod.setText("Recod");
        }else {
            mRecoding = true;
            onStartRecod();
            mBtnRecod.setText("Stop");
        }
    }

    private void onStartRecod() {
        mVideoEncoder = new VideoEncoder(this,true);
        mVideoPath = getPath(true);
        try {
            mVideoEncoder.setSavePath(mVideoPath);
            mVideoEncoder.prepare(720,1280);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAudioPath = getPath(false);
        mAudioRecordTask = new ExAudioRecordTask(mAudioPath);

        try {
            mVideoEncoder.start();
            new Thread(mAudioRecordTask).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onStopRecod() {
        mVideoEncoder.stop();
        mAudioRecordTask.stop();
        showToast("录制结束\n vido ="+mVideoPath+"\n audio"+mAudioPath);
    }

    private String getPath(boolean isVideo){
        String fileName = isVideo ? "video_"+System.currentTimeMillis()+".mp4" : "audio_"+System.currentTimeMillis()+".aac";
        return SampleActivity.ROOT_DIR+"/"+fileName;
    }

    @Override
    public String getSampleName() {
        return "分别录制音视频，然后再合并";
    }

    @Override
    public void onFrame(byte[] bytes, long time) {
        if (mVideoEncoder != null)
            mVideoEncoder.feedData(bytes,time);
    }
}
