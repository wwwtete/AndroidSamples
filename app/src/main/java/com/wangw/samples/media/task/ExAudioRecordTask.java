package com.wangw.samples.media.task;

import android.annotation.TargetApi;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;

import com.exlogcat.L;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 使用AudioRecord + MediaCodec 录制aac格式音频
 * Created by wangw on 2017/2/3.
 */

public class ExAudioRecordTask extends AudioRecordTask {

    private String mMime = "audio/mp4a-latm";    //录音编码的mime
    private int mRate = 256000; //编码的key bit rate
    private MediaCodec mEnc;
    MediaCodec.BufferInfo mInfo = new MediaCodec.BufferInfo();


    public ExAudioRecordTask(String outPutFile) {
        super(outPutFile);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        try {
            mFos = new FileOutputStream(mOutPutFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            L.e("创建文件失败 ->"+e.getMessage());
            return;
        }
        //初始化编码器实例
        MediaFormat format = MediaFormat.createAudioFormat(mMime,mSampleRate,mChannelCount);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE,mRate);

        try {
            //创建一个编解码
            mEnc = MediaCodec.createEncoderByType(mMime);
        } catch (IOException e) {
            e.printStackTrace();
            L.e("创建编码器失败 ->"+e.getMessage());
            return;
        }
        //设置为编码器
        mEnc.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

        //初始化AudioRecord
        mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,mSampleRate,mChannelConfig,mAudioFormat,mBuffSize);

        //在开始录音之前，也要设置编码器开始工作
        mEnc.start();
        //开始录音
        mRecord.startRecording();
        //先将录制的音频数据取出来进行处理，再写入文件
        int index;
        int length;
        while (!mStop.get()){
            index = mEnc.dequeueInputBuffer(-1);
            if (index >= 0){
                final ByteBuffer buffer = mEnc.getInputBuffer(index);
                buffer.clear();
                length = mRecord.read(buffer,mBuffSize);
                if (length > 0){
                    mEnc.queueInputBuffer(index,0,length,System.nanoTime()/1000,0);
                }
            }


            int outIndex =0;
            do {
                outIndex = mEnc.dequeueOutputBuffer(mInfo,0);
                if (outIndex >= 0){
                    ByteBuffer buffer = mEnc.getOutputBuffer(outIndex);
                    buffer.position(mInfo.offset);
                    //AAC编码，需要加数据头，AAC编码数据头固定为7个字节
                    byte[] temp = new byte[mInfo.size+7];
                    buffer.get(temp,7,mInfo.size);
                    addADTStoPacket(temp,temp.length);
                    try {
                        mFos.write(temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mEnc.releaseOutputBuffer(outIndex,false);
                }else if(outIndex == MediaCodec.INFO_TRY_AGAIN_LATER){

                }else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){

                }
            }while (outIndex >= 0);
        }

        //结束时 发送编码结束标志，循环结束，停止并释放编码器
        mEnc.stop();
        mRecord.stop();
        mEnc.release();
        mRecord.release();
        try {
            mFos.flush();
            mFos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 给编码出的aac裸流添加adts头字段
     * @param temp
     * @param length
     */
    private void addADTStoPacket(byte[] temp, int length) {
        int profile = 2;    //AAC LC
        int freqIdx = 4;    //44.1KHz
        int chanCfg = 2;    //CPE
        temp[0] = (byte) 0xFF;
        temp[1] = (byte) 0xF9;
        temp[2] = (byte) (((profile-1)<<6) + (freqIdx<<2) + (chanCfg>>2));
        temp[3] = (byte) (((chanCfg&3)<<6) + (length>>11));
        temp[4] = (byte) ((length&0x7FF)>>3);
        temp[5] = (byte) (((length&7)<<5) + 0x1f);
        temp[6] = (byte) 0xFC;

    }
}
