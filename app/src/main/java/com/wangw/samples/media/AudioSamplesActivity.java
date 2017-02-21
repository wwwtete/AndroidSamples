package com.wangw.samples.media;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.media.task.AudioRecordTask;
import com.wangw.samples.media.task.ExAudioRecordTask;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wangw on 2017/2/3.
 */

public class AudioSamplesActivity extends BaseActivity {

    @Bind(R.id.btn_au_record)
    Button mBtnAuRecord;
    @Bind(R.id.btn_au_play)
    Button mBtnAuPlay;
    @Bind(R.id.btn_md_record)
    Button mBtnMdRecord;
    @Bind(R.id.btn_md_play)
    Button mBtnMdPlay;

    private File mCurrFile;
    private AudioRecordTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_samples);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_au_record,R.id.btn_au_play,R.id.btn_md_record,R.id.btn_md_play})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_au_record:

                onRecordForPCM();
                break;
            case R.id.btn_au_play:
                onPlayPcm();
                break;
            case R.id.btn_md_record:
                onRecordForAAC();
                break;
            case R.id.btn_md_play:
                onPlayAAC();
                break;
        }
    }

    private void onPlayAAC() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(mCurrFile),"audio/*");
        startActivity(intent);
    }

    private void onRecordForAAC() {
        if(mTask == null) {
            mCurrFile = new File(SampleActivity.ROOT_DIR, System.currentTimeMillis() + ".aac");
            mTask = new ExAudioRecordTask(mCurrFile.getAbsolutePath());
            new Thread(mTask).start();
            mBtnMdRecord.setText("stop");
        }else {
            mTask.stop();
            mTask = null;
            mBtnMdRecord.setText("Record");
        }
    }

    private void onPlayPcm() {
        showToast("暂未实现");
    }

    private void onRecordForPCM() {
        if(mTask == null) {
            mCurrFile = new File(SampleActivity.ROOT_DIR, System.currentTimeMillis() + ".pcm");
            mTask = new AudioRecordTask(mCurrFile.getAbsolutePath());
            new Thread(mTask).start();
            mBtnAuRecord.setText("stop");
        }else {
            mTask.stop();
            mTask = null;
            mBtnAuRecord.setText("Record");
        }
    }

    @Override
    public String getSampleName() {
        return "音频相关Sample";
    }
}
