package com.wangw.samples.media.filter.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.wangw.samples.media.filter.encode.EncoderConfig;
import com.wangw.samples.media.filter.filter.FilterManager;
import com.wangw.samples.media.filter.filter.IFilter;
import com.wangw.samples.media.filter.gles.FullFrameRect;
import com.wangw.samples.media.filter.gles.GlUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoPlayRenderer implements GLSurfaceView.Renderer {

    private final Context mApplicationContext;
    private int mTextureId = GlUtil.NO_TEXTURE;
    private FullFrameRect mFullScreen;
    private SurfaceTexture mSurfaceTexture;
    private final float[] mSTMatrix = new float[16];


    private IFilter mCurrentFilter;
    private FilterManager.FilterType mCurrentFilterType;
    private FilterManager.FilterType mNewFilterType;
//    private VideoPlayEncoderTask mVideoEncoder;

//    private EncoderConfig mEncoderConfig;
    private boolean mEncoding;

    private static final int ENCODING_OFF = 0;
    private static final int ENCODING_ON = 1;
    private static final int ENCODING_RESUME = 2;
    private int mEncodingState = ENCODING_OFF;


    private float mMvpScaleX = 1f, mMvpScaleY = 1f;
    private OnSurfaceListener mOnSurfaceListener;
    private int mSurfaceWidth, mSurfaceHeight;
    private int mIncomingWidth, mIncomingHeight;
    private int mFrameNum;

    public VideoPlayRenderer(Context applicationContext) {
        mApplicationContext = applicationContext;
        mCurrentFilterType = mNewFilterType = FilterManager.FilterType.Normal;
//        mVideoEncoder = VideoPlayEncoderTask.getInstance(applicationContext);
    }

    /**
     * videoview surfaceview的创建与销毁
     *
     * @author zengliang
     */
    public interface OnSurfaceListener {
        void onSurfaceCreate(SurfaceTexture surface);

        void onSurfaceChanged(SurfaceTexture surface, int width, int height);
    }

    public void setOnSurfaceListener(OnSurfaceListener l) {
        this.mOnSurfaceListener = l;
    }

    public void setEncoderConfig(EncoderConfig encoderConfig) {
//        mEncoderConfig = encoderConfig;
    }

//    public void setEncodingEnabled(boolean encodingEnabled) {
//        mEncoding = encodingEnabled;
//    }


    public void setVideoPlayViewSize(int width, int height) {
        mIncomingWidth = width;
        mIncomingHeight = height;

        Log.i("zl", "setVideoPlayViewSize mIncomingWidth = " + mIncomingWidth + ",mIncomingHeight = " + mIncomingHeight);

        float scaleHeight = mSurfaceWidth / (width * 1f / height * 1f);
        float surfaceHeight = mSurfaceHeight;

        if (mFullScreen != null) {
            mMvpScaleX = 1f;
            mMvpScaleY = scaleHeight / surfaceHeight;
            mFullScreen.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Matrix.setIdentityM(mSTMatrix, 0);
//        mEncoding = mVideoEncoder.isEncoding();
//        if (mEncoding) {
//            mEncodingState = ENCODING_RESUME;
//        } else {
//            mEncodingState = ENCODING_OFF;
//            mVideoEncoder.initFilter(mCurrentFilterType);
//        }
        mCurrentFilter = FilterManager.getCameraFilter(mNewFilterType, mApplicationContext);
        mFullScreen = new FullFrameRect(mCurrentFilter);
        mTextureId = mFullScreen.createTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);

        Log.i("onSurfaceCreated", "onSurfaceCreated mTextureId = " + mTextureId);

        if (mOnSurfaceListener != null) {
            mOnSurfaceListener.onSurfaceCreate(mSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        Log.i("zl", "onSurfaceChanged mSurfaceWidth = " + mSurfaceWidth + ",mSurfaceHeight = " + mSurfaceHeight);
        if (gl != null) {
            gl.glViewport(0, 0, width, height);
        }
        if (mOnSurfaceListener != null) {
            mOnSurfaceListener.onSurfaceChanged(mSurfaceTexture, width, height);
        }

        setVideoPlayViewSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mFrameNum++;
//        Log.i("zl", "onDrawFrame mFrameNum = " + mFrameNum);
        mSurfaceTexture.updateTexImage();
        if (mNewFilterType != mCurrentFilterType) {
            mCurrentFilter = FilterManager.getCameraFilter(mNewFilterType, mApplicationContext);
            mFullScreen.changeProgram(mCurrentFilter);
            mCurrentFilterType = mNewFilterType;
        }
        mFullScreen.getFilter().setTextureSize(mIncomingWidth, mIncomingHeight);
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);

//        videoOnDrawFrame(mTextureId, mSTMatrix, mSurfaceTexture.getTimestamp());
    }

    private void videoOnDrawFrame(int textureId, float[] texMatrix, long timestamp) {
//        if (mEncoding && mEncoderConfig != null) {
//            switch (mEncodingState) {
//                case ENCODING_OFF:
//                    mEncoderConfig.updateEglContext(EGL14.eglGetCurrentContext());
//                    mVideoEncoder.startEncoding(mEncoderConfig);
//                    startAudioEncoding();
//                    mVideoEncoder.setTextureId(textureId);
//                    mVideoEncoder.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
//                    mEncodingState = ENCODING_ON;
//                    break;
//                case ENCODING_RESUME:
//                    mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
//                    mVideoEncoder.setTextureId(textureId);
//                    mVideoEncoder.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
//                    mEncodingState = ENCODING_ON;
//                    break;
//                case ENCODING_ON:
//
//                    break;
//            }
//
//        } else {
//            switch (mEncodingState) {
//                case ENCODING_ON:
//                case ENCODING_RESUME:
//                    mVideoEncoder.stopEncoding();
//                    mEncodingState = ENCODING_OFF;
//                    break;
//                case ENCODING_OFF:
//                    // yay
//                    break;
//                default:
//                    throw new RuntimeException("unknown status " + mEncodingState);
//            }
//        }
        Log.i("videoOnDrawFrame",mEncoding+"状态"+mEncodingState);
//        mVideoEncoder.updateFilter(mCurrentFilterType);
//        mVideoEncoder.frameAvailable(texMatrix, timestamp);
    }

//    private Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            mVideoEncoder.startAudioEncoding(mEncoderConfig);
//        }
//    };


//    private void startAudioEncoding(){
//        handler.sendEmptyMessageDelayed(0,1000);
//    }

    public void notifyPausing() {
        mEncodingState = ENCODING_OFF;
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mFullScreen != null) {
            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
            mFullScreen = null;             // to be destroyed
        }
    }

    public void notifyDestroy() {
//        mVideoEncoder.stopEncoding();
    }

    public void changeFilter(FilterManager.FilterType filterType) {
        mNewFilterType = filterType;
    }

    public void changeFilter2(FilterManager.FilterType filterType) {
        mNewFilterType = filterType;
        mCurrentFilterType = FilterManager.FilterType.Normal;
    }

    public FilterManager.FilterType getCurrentFilterType(){
        return mCurrentFilterType;
    }
}
