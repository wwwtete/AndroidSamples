package com.wangw.samples.media.task;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.exlogcat.L;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 使用AudioMedia录制音频，输出最原始的PCM格式音频文件
 * Created by wangw on 2017/2/3.
 */

public class AudioRecordTask implements Runnable {

    protected String mOutPutFile;
    protected int mSampleRate = 44100;    //采样率，默认44.1k
    protected int mChannelCount = 2;    //音频采样通道，默认2通道
    protected int mChannelConfig = AudioFormat.CHANNEL_IN_STEREO;   //通道设置，默认立体声
    protected int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;    //设置采样格式，默认16比特PCM
    protected int mBuffSize;  //音频录制实例化和录制过程中需要用到数据
    protected byte[] mBuffer;
    protected AudioRecord mRecord;
    protected FileOutputStream mFos;
    protected AtomicBoolean mStop = new AtomicBoolean(false);

    public AudioRecordTask(String outPutFile) {
        this.mOutPutFile = outPutFile;
        mBuffSize = AudioRecord.getMinBufferSize(mSampleRate,mChannelConfig,mAudioFormat)*2;
        mBuffer = new byte[mBuffSize];
    }

    @Override
    public void run() {
        try {
            mFos = new FileOutputStream(new File(mOutPutFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            L.e("录制失败 -> "+e.getMessage());
            return;
        }
        //实例化AudioRecord
        mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,mSampleRate,mChannelConfig,mAudioFormat,mBuffSize);

        //开始录制
        mRecord.startRecording();
        int lenth;
        while (!mStop.get()){
            //循环读取数据到buffer中，并保持buffer到文件中
            lenth = mRecord.read(mBuffer,0,mBuffSize);
            if (lenth > 0){
                try {
                    mFos.write(mBuffer,0,lenth);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            mFos.flush();
            mFos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecord.stop();
        mRecord.release();
    }

    public void stop(){
        mStop.set(true);
    }
}
