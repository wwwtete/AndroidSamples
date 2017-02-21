package com.wangw.samples.media;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.comm.CameraView;
import com.wangw.samples.comm.FrameCallback;
import com.wangw.samples.media.task.VideoEncoder;
import com.wangw.samples.media.task.VideoRecordTask;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wangw on 2017/2/8.
 */

public class VideoSamplesActivity extends BaseActivity implements FrameCallback {

    @Bind(R.id.camera)
    CameraView mCamera;
    @Bind(R.id.btn_recod)
    Button mBtnRecod;

    private VideoRecordTask mTask;
    private VideoEncoder mVideoEncoder;
    private String mOutputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_sapmes);
        ButterKnife.bind(this);
        onInitView();
    }

    private void onInitView() {
        mCamera.setCallback(this);
    }

    @OnClick({R.id.btn_recod})
    public void onClick(View view){
        onRecordVideo();
    }

    private void onRecordVideo() {
        if (mVideoEncoder == null || !mVideoEncoder.isRuning()){
            mOutputFile = SampleActivity.ROOT_DIR + "/" + System.currentTimeMillis() + ".h264";
            mVideoEncoder = new VideoEncoder(this,true);
            try {
                mVideoEncoder.setSavePath(mOutputFile);
                mVideoEncoder.prepare(720,1280);
                mVideoEncoder.start();
                mBtnRecod.setText("stop");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            mVideoEncoder.stop();
            mBtnRecod.setText("Record");
            playVideo();
        }

    }

    private void playVideo(){
        if (TextUtils.isEmpty(mOutputFile))
            return;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndTypeAndNormalize(Uri.fromFile(new File(mOutputFile)),"video/*");
        startActivity(intent);
    }



    @Override
    public String getSampleName() {
        return "视频录制相关Samples";
    }

    @Override
    public void onFrame(byte[] bytes, long time) {
        if (mTask != null)
            mTask.feedData(bytes,time);

        if (mVideoEncoder != null){
            mVideoEncoder.feedData(bytes,time);
        }
    }
}
