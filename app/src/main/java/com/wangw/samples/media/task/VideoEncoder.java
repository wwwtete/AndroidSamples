package com.wangw.samples.media.task;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.wangw.samples.SamplesApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Description:
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class VideoEncoder implements Runnable {

    private static final String TAG="VideoEncoder";
    private MediaCodec mEnc;
    private String mime="video/avc";
    private int rate=256000;
    private int frameRate=24;
    private int frameInterval=1;

    private int fpsTime;

    private Thread mThread;
    private boolean mStartFlag=false;
    private int width;
    private int height;
    private byte[] mHeadInfo=null;

    private byte[] nowFeedData;
    private long nowTimeStep;
    private boolean hasNewData=false;

    private FileOutputStream fos;
    private MediaMuxer mMuxer;
    private int mVideoTrack;
    private String mSavePath;
    private long mNanoTime;
    private NV21Convertor mConvertor;
    private Context mContext;
    private boolean mIsMp4;
    private byte[] yuv;

    public VideoEncoder(Context context,boolean isMp4){
        mContext = context;
        fpsTime=1000/frameRate;
        mIsMp4 = isMp4;
    }

    public void setMime(String mime){
        this.mime=mime;
    }

    public void setRate(int rate){
        this.rate=rate;
    }

    public void setFrameRate(int frameRate){
        this.frameRate=frameRate;
    }

    public void setFrameInterval(int frameInterval){
        this.frameInterval=frameInterval;
    }

    public void setSavePath(String path){
        this.mSavePath=path;
    }


    /**
     * 准备录制
     * @param width 视频宽度
     * @param height 视频高度
     * @throws IOException
     */
    public void prepare(int width, int height) throws IOException {
        mHeadInfo=null;
        this.width=width;
        this.height=height;
        File file=new File(mSavePath);
        File folder=file.getParentFile();
        if(!folder.exists()){
            boolean b=folder.mkdirs();
            Log.e("wuwang","create "+folder.getAbsolutePath()+" "+b);
        }
        if(file.exists()){
            boolean b=file.delete();
        }

        EncoderDebugger debugger = EncoderDebugger.debug(SamplesApplication.getInstance(), width, height);
        mConvertor = debugger.getNV21Convertor();
        if(mIsMp4)
            mMuxer = new MediaMuxer(mSavePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        else
            fos= new FileOutputStream(mSavePath);
        MediaFormat format;
        if (getDgree() == 0) {
            format = MediaFormat.createVideoFormat("video/avc", height, width);
        } else {
            format = MediaFormat.createVideoFormat("video/avc", width, height);
        }
        format.setInteger(MediaFormat.KEY_BIT_RATE,rate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE,frameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,frameInterval);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, debugger.getEncoderColorFormat());
        mEnc= MediaCodec.createEncoderByType(mime);
        mEnc.configure(format,null,null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    private int getDgree() {
        int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        return degrees;
    }

    /**
     * 开始录制
     * @throws InterruptedException
     */
    public void start() throws InterruptedException {
        if(mThread!=null&&mThread.isAlive()){
            mStartFlag=false;
            mThread.join();
        }
        mEnc.start();
        mStartFlag=true;
        mThread=new Thread(this);
        mThread.start();
    }

    /**
     * 停止录制
     */
    public void stop(){
        try {
            mStartFlag=false;
            mThread.join();
            mEnc.stop();
            mEnc.release();
            if (fos != null) {
                fos.flush();
                fos.close();
            }
            if (mMuxer != null) {
                mMuxer.stop();
                mMuxer.release();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isRuning(){
        return mStartFlag;
    }

    /**
     * 由外部喂入一帧数据
     * @param data RGBA数据
     * @param timeStep camera附带时间戳
     */
    public void feedData(final byte[] data, final long timeStep){
        hasNewData=true;
        nowFeedData=data;
        nowTimeStep=timeStep;
    }

    private ByteBuffer getInputBuffer(int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mEnc.getInputBuffer(index);
        }else{
            return mEnc.getInputBuffers()[index];
        }
    }

    private ByteBuffer getOutputBuffer(int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mEnc.getOutputBuffer(index);
        }else{
            return mEnc.getOutputBuffers()[index];
        }
    }

    //TODO 定时调用，如果没有新数据，就用上一个数据
    private void readOutputData(byte[] data, long timeStep) throws IOException {
        int index=mEnc.dequeueInputBuffer(-1);
        if(index>=0){
            if(hasNewData){
                if(yuv==null){
                    yuv=new byte[width*height*3/2];
                }
                yuv = mConvertor.convert(data);
            }
            ByteBuffer buffer=getInputBuffer(index);
            buffer.clear();
            buffer.put(yuv);
            mEnc.queueInputBuffer(index,0,yuv.length,(System.nanoTime() - mNanoTime)/1000,mStartFlag ? 0 : MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        }
        MediaCodec.BufferInfo mInfo=new MediaCodec.BufferInfo();
        int outIndex=mEnc.dequeueOutputBuffer(mInfo,0);
        if(mIsMp4)
            encordMp4(mInfo, outIndex);
        else
            encordH264(outIndex,mInfo);


    }

    private void encordMp4(MediaCodec.BufferInfo info, int outIndex) {
        do {
            if (outIndex >= 0){
                ByteBuffer outputBuffer = getOutputBuffer(outIndex);
                if (mVideoTrack >= 0 && info.size > 0&& info.presentationTimeUs > 0)
                    mMuxer.writeSampleData(mVideoTrack,outputBuffer,info);
                mEnc.releaseOutputBuffer(outIndex,false);
                outIndex = mEnc.dequeueOutputBuffer(info,0);
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                    Log.d(TAG, "编码结束");
                }
            }else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                mVideoTrack = mMuxer.addTrack(mEnc.getOutputFormat());
                mMuxer.start();
            }
        }while (outIndex >= 0);
    }

    private void encordH264(int outIndex,MediaCodec.BufferInfo info) throws IOException {
        while (outIndex>=0){
            ByteBuffer outBuf=getOutputBuffer(outIndex);
            byte[] temp=new byte[info.size];
            outBuf.get(temp);
            if(info.flags==MediaCodec.BUFFER_FLAG_CODEC_CONFIG){
                Log.e(TAG,"start frame");
                mHeadInfo=new byte[temp.length];
                mHeadInfo=temp;
            }else if(info.flags%8==MediaCodec.BUFFER_FLAG_KEY_FRAME){
                Log.e(TAG,"key frame");
                byte[] keyframe = new byte[temp.length + mHeadInfo.length];
                System.arraycopy(mHeadInfo, 0, keyframe, 0, mHeadInfo.length);
                System.arraycopy(temp, 0, keyframe, mHeadInfo.length, temp.length);
                Log.e(TAG,"other->"+info.flags);
                fos.write(keyframe,0,keyframe.length);
            }else if(info.flags==MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                Log.e(TAG,"end frame");
            }else{
                fos.write(temp,0,temp.length);
            }
            mEnc.releaseOutputBuffer(outIndex,false);
            outIndex=mEnc.dequeueOutputBuffer(info,0);
            Log.e("wuwang","outIndex-->"+outIndex);
        }
    }

    @Override
    public void run() {
        mNanoTime = System.nanoTime();
        while (mStartFlag){
            long time= System.currentTimeMillis();
            if(nowFeedData!=null){
                try {
                    readOutputData(nowFeedData,nowTimeStep);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            long lt= System.currentTimeMillis()-time;
            if(fpsTime>lt){
                try {
                    Thread.sleep(fpsTime-lt);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
