package com.wangw.samples.media.filter.encode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by xingliao_zgl on 16/8/18.
 */
public class MediaMuxerWrapper {

    private static final String TAG = "zl";
    public static final int TRACK_VIDEO = 1;
    public static final int TRACK_AUDIO = 2;
    private Object lock = new Object();

    private MediaMuxer mMuxer;
    private int mTrackIndex;
    private boolean mMuxerStarted;

    public boolean mIsVideoTrackAdd, mIsAudioTrackAdd;
    public int mVideoTrackIndex, mAudioTrackIndex;
    private boolean mCopyVideo = true;
    private boolean mCopyAudio = true;

    private boolean mVideoEncodingComplete;
    public boolean getVideoEncodingComplete(){
        return mVideoEncodingComplete;
    }

    public void setVideoEncodingComplete(boolean videoEncodingComplete){
        mVideoEncodingComplete = videoEncodingComplete;
    }


    public boolean isMuxerStart() {
        return mMuxerStarted;
    }

    public Object getLock() {
        return lock;
    }

    public MediaMuxerWrapper(String outputFilePath) throws IOException {
        mMuxer = new MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mTrackIndex = -1;
        mMuxerStarted = false;
    }


    public void addTrackIndex(int index, MediaFormat mediaFormat) {
        if (mMuxerStarted) {
            return;
        }
        switch (index) {
            case TRACK_AUDIO:
                /*if(!mIsVideoTrackAdd){
                    synchronized (this){//保证先添加video track,再添加audio track
                        while (!mIsVideoTrackAdd) {
                            try {
                                wait(100);
                            } catch (final InterruptedException e) {
                                break;
                            }
                        }
                    }
                }*/
                mAudioTrackIndex = mMuxer.addTrack(mediaFormat);
                mIsAudioTrackAdd = true;
                Log.e(TAG, "add audio track...");
                break;
            case TRACK_VIDEO:
                mVideoTrackIndex = mMuxer.addTrack(mediaFormat);
                mIsVideoTrackAdd = true;
                Log.e(TAG, "add video track...");
                /*synchronized (this){
                    if(!mIsAudioTrackAdd){
                        notifyAll();
                    }
                }*/
                break;
        }
    }

    public boolean startMuxer() {
        if (mMuxerStarted) {
            return true;
        }
        synchronized (lock) {
            if ((!mCopyVideo || mIsVideoTrackAdd) && (!mCopyAudio|| mIsAudioTrackAdd)) {
                mMuxer.start();
                mMuxerStarted = true;
                lock.notifyAll();
            }
        }

        return mMuxerStarted;
    }

    public void writeSampleData(int index, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        synchronized (lock) {
            mMuxer.writeSampleData(index == TRACK_VIDEO ? mVideoTrackIndex : mAudioTrackIndex, byteBuf, bufferInfo);
        }
    }

    public void stopMuxer() {
        Log.d(TAG, "start releasing MediaMuxer...");
        if (mMuxer != null) {
            // TODO: stop() throws an exception if you haven't fed it any data.  Keep track
            //       of frames submitted, and don't call stop() if we haven't written anything.
            mMuxerStarted = false;
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }


}
