package com.wangw.samples.media.task;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import com.wangw.samples.SamplesApplication;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 使用MediaReader录制视频
 * Created by wangw on 2017/2/8.
 */

public class VideoRecordTask implements Runnable {

    private final static String MIME = "video/avc";  //编码的MIME
    private final static int RATE = 256000;  //波特率 256kb
    private final static int FRAMERATE = 24; //帧率 24帧
    private final static int FRAMEINTERVAL = 1;     //关键帧 1秒1帧

    protected String mOutputFile;
    protected int mWidht;
    protected int mHeight;
    private int mFpsTime;   //帧率间隔时间
    private byte[] mYUV;
    private byte[] mHeadInfo;   //视频文件头
    private boolean mHasNewData;    //是否有数据
    private byte[] mNowFeedData;    //当前数据
    private long mNowTimeStep;       //当前时间戳
    private int mVideoTrack = -1;   //视频轨道索引
    private MediaMuxer mMuxer;
    private MediaCodec mEnc;
    private boolean mStartFlag = false;
    private long mNanoTIme;
    private MediaCodecInfo mCodecInfo;
    private EncoderDebugger mDebugger;

    public VideoRecordTask(String outputFile,int width,int height) {
        this.mOutputFile = outputFile;
        mWidht = width;
        mHeight = height;
        mFpsTime = 1000/FRAMERATE;
    }



    @Override
    public void run() {
        try {
            onPrepare();
            while (mStartFlag){
                long time = System.currentTimeMillis();
                if (mNowFeedData != null){
                    readOutputData(mNowFeedData,mNowTimeStep);
                }
                long lt = System.currentTimeMillis() - time;
                if (mFpsTime > lt){
                    Thread.sleep(mFpsTime-lt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (mEnc != null){
                mEnc.stop();
                mEnc.release();
            }
            if (mMuxer != null){
                try {
                    mMuxer.stop();
                    mMuxer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 定时调用，如果没有数据则用上一个数据
     * @param data
     * @param timeStep
     */
    private void readOutputData(byte[] data,long timeStep) throws IOException {
        int index = mEnc.dequeueInputBuffer(-1);
        if (index >= 0){
            ByteBuffer buffer = getInputBuffer(index);
            buffer.clear();
            if (mHasNewData){
//                if (mYUV == null) {
//                    mYUV = new byte[mWidht*mHeight*3/2];
//                }
//                nv21Tonv12(data,mYUV,mWidht,mHeight);
//                rgbaToYuv(data,mWidht,mHeight,mYUV);
                mYUV = mDebugger.getNV21Convertor().convert(data);
            }
//            buffer.put(data);

            mEnc.queueInputBuffer(index,0,mYUV.length,(System.nanoTime() - timeStep)/1000,mStartFlag ? 0 : MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        }
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int outputIndex = mEnc.dequeueOutputBuffer(info,0);
        do {
            if (outputIndex >= 0){
                ByteBuffer outputBuffer = getOutputBuffer(outputIndex);
                if (mVideoTrack >= 0 && info.size > 0 && info.presentationTimeUs > 0)
                    mMuxer.writeSampleData(mVideoTrack,outputBuffer,info);
                mEnc.releaseOutputBuffer(outputIndex,false);
                outputIndex = mEnc.dequeueOutputBuffer(info,0);
                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                    log("编码结束");
                }
            }else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                mVideoTrack = mMuxer.addTrack(mEnc.getOutputFormat());
                mMuxer.start();
                log("添加视频轨 index = "+mVideoTrack);
            }
        }while (outputIndex >= 0);
//        while (outputIndex >= 0){
//            log("输出Buffer index= "+outputIndex);
//            byte[] temp = new byte[info.size];
//            outputBuffer.get(temp);
//            if (info.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG){
//                log("配置信息 length="+temp.length);
//                mHeadInfo = new byte[temp.length];
//                mHeadInfo = temp;
//            }else if (info.flags % 8 == MediaCodec.BUFFER_FLAG_KEY_FRAME){
//                log("关键帧 header="+mHeadInfo.length);
//                //关键帧比普通帧多了个帧头，保持编码信息
//                byte[] keyFrame = new byte[temp.length+mHeadInfo.length];
//                System.arraycopy(mHeadInfo,0,keyFrame,0,mHeadInfo.length);
//                System.arraycopy(temp,0,keyFrame,mHeadInfo.length,temp.length);
//            }else if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM){
//                log("结束帧");
//            }else {
//                log("普通帧 length="+temp.length);
//                mFos.write(temp,0,temp.length);
//            }
//            mEnc.releaseOutputBuffer(outputIndex,false);
//            outputIndex = mEnc.dequeueOutputBuffer(info,0);
//        }
    }

    private void rgbaToYuv(byte[] rgba,int width,int height,byte[] yuv){
        final int frameSize = width * height;

        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + frameSize/4;

        int R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                index = j * width + i;
                if(rgba[index*4]>127||rgba[index*4]<-128){
                    Log.e("color","-->"+rgba[index*4]);
                }
                R = rgba[index*4]&0xFF;
                G = rgba[index*4+1]&0xFF;
                B = rgba[index*4+2]&0xFF;

                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                yuv[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv[uIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                    yuv[vIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                }
            }
        }
    }

    private void nv21Tonv12(byte[] nv21,byte[] nv12,int width,int height){
        if(nv21 == null || nv12 == null)return;
        int framesize = width*height;
        int i = 0,j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for(i = 0; i < framesize; i++){
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize/2; j+=2)
        {
            nv12[framesize + j-1] = nv21[j+framesize];
        }
        for (j = 0; j < framesize/2; j+=2)
        {
            nv12[framesize + j] = nv21[j+framesize-1];
        }
    }

    private void onPrepare() throws Exception {
        mCodecInfo  = getCodecInfo(MIME);
        if (mCodecInfo == null){
            Log.e("VideoRecorder","没有找到合适的MediaCodecInfo");
            return;
        }
        mHeadInfo = null;
        File file = new File(mOutputFile);
        if (file.exists()) {
            file.delete();
        }
        mDebugger = EncoderDebugger.debug(SamplesApplication.getInstance(),mWidht,mHeight);
        int colorFormat = mDebugger.getEncoderColorFormat();//getColorFrmat(mCodecInfo,MIME);
        //设置编码格式，并初始化编码器
        MediaFormat format = MediaFormat.createVideoFormat(MIME,mWidht,mHeight);
        format.setInteger(MediaFormat.KEY_BIT_RATE,RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE,FRAMERATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,FRAMEINTERVAL);
        //TODO 正常逻辑应该检测手机支持的颜色空间，为了简单写成国定的ColorFormat
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        mEnc = MediaCodec.createEncoderByType(MIME);
        mEnc.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mEnc.start();
        mNanoTIme = System.nanoTime();
        mMuxer = new MediaMuxer(mOutputFile,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mStartFlag = true;
    }

    private int getColorFrmat(MediaCodecInfo codecInfo, String mime) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mime);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)){
                return colorFormat;
            }
        }
        return 0;
    }

    private boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    public void stop(){
        mStartFlag = false;
    }

    /**
     * 由外部喂数据
     * @param data  RGBA数据
     * @param timeStep  camera附带的时间戳
     */
    public void feedData(byte[] data,long timeStep){
        this.mHasNewData = true;
        this.mNowFeedData = data;
        this.mNowTimeStep = timeStep;
    }

    public ByteBuffer getInputBuffer(int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return mEnc.getInputBuffer(index);
        else
            return mEnc.getInputBuffers()[index];
    }

    public ByteBuffer getOutputBuffer(int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return mEnc.getOutputBuffer(index);
        else
            return mEnc.getOutputBuffers()[index];
    }

    public static MediaCodecInfo getCodecInfo(String mimeType){
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder())
                continue;
            String[] types = info.getSupportedTypes();
            for (int i1 = 0; i1 < types.length; i1++) {
                if (types[i1].equalsIgnoreCase(mimeType))
                    return info;
            }
        }
        return null;
    }

    public boolean isRuning() {
        return mStartFlag;
    }

    private void log(String msg){
        Log.d("VideoRecorder",msg);
    }

}
