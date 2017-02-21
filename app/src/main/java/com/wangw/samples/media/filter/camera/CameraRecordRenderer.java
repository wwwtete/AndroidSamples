package com.wangw.samples.media.filter.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.wangw.samples.media.filter.encode.EncoderConfig;
import com.wangw.samples.media.filter.encode.VideoRecordEncoderTask;
import com.wangw.samples.media.filter.filter.FilterManager;
import com.wangw.samples.media.filter.gles.FullFrameRect;
import com.wangw.samples.media.filter.gles.GlUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRecordRenderer implements GLSurfaceView.Renderer, CommonHandlerListener{

    private static final String TAG = CameraRecordRenderer.class.getSimpleName();
    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;

    private final Context mApplicationContext;

    private int mTextureId = GlUtil.NO_TEXTURE;
    private FullFrameRect mFullScreen;
    private SurfaceTexture mSurfaceTexture;
    private final float[] mSTMatrix = new float[16];

    private FilterManager.FilterType mCurrentFilterType;
    private FilterManager.FilterType mNewFilterType;
    private VideoRecordEncoderTask mVideoEncoder;

    private boolean mRecordingEnabled;
    private int mRecordingStatus;
    private EncoderConfig mEncoderConfig;

    private SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener;
    private float mMvpScaleX = 1f, mMvpScaleY = 1f;
    private int mSurfaceWidth, mSurfaceHeight;
    private int mIncomingWidth, mIncomingHeight;

    public int maxPreviewWidth = 1280;
    public int maxPreviewHeight = 1280;
    private boolean mIsFacingFront;


    private HandlerThread mHandlerThread;
    private CameraRecordHandler mHandlerCameraRecord;

    public CameraRecordRenderer(Context applicationContext) {
        mApplicationContext = applicationContext;
        mCurrentFilterType = FilterManager.FilterType.Normal;
        mNewFilterType = FilterManager.FilterType.Normal;
        mVideoEncoder = VideoRecordEncoderTask.getInstance(applicationContext);


        mHandlerThread = new HandlerThread("CameraRecordHandlerThread");
        mHandlerThread.start();
        mHandlerCameraRecord = new CameraRecordHandler(mHandlerThread.getLooper(), this);
    }

    public void setEncoderConfig(EncoderConfig encoderConfig) {
        mEncoderConfig = encoderConfig;
    }

    public void setRecordingEnabled(boolean recordingEnabled) {
        mRecordingEnabled = recordingEnabled;
    }

    private OnCameraStartCallback mOnCameraStartCallback;

    public void setOnCameraStartCallback(OnCameraStartCallback callback) {
        mOnCameraStartCallback = callback;
    }

    public interface OnCameraStartCallback {
        void onCameraStart(boolean success);
    }

    public interface OnRecordingStartCallback {
        void onRecordingStart(boolean success);
    }

    public interface OnRecordingEndCallback {
        void onRecordingEnd(boolean success);
    }


    public synchronized void startRecord(EncoderConfig encoderConfig, OnRecordingStartCallback callback) {
        setEncoderConfig(encoderConfig);
        setRecordingEnabled(true);
        mVideoEncoder.setOnRecordingStartCallback(callback);
    }

    public synchronized void stopRecord(OnRecordingEndCallback callback) {
        setRecordingEnabled(false);
        mVideoEncoder.setOnRecordingEndCallback(callback);
    }


    public void setOnFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener l) {
        this.mOnFrameAvailableListener = l;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Matrix.setIdentityM(mSTMatrix, 0);
        mRecordingEnabled = mVideoEncoder.isRecording();
        if (mRecordingEnabled) {
            mRecordingStatus = RECORDING_RESUMED;
        } else {
            mRecordingStatus = RECORDING_OFF;
            mVideoEncoder.initFilter(mCurrentFilterType);
        }
        mFullScreen = new FullFrameRect(
                FilterManager.getCameraFilter(mCurrentFilterType, mApplicationContext));
        mTextureId = mFullScreen.createTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);

        mHandlerCameraRecord.sendMessage(mHandlerCameraRecord.obtainMessage(CameraRecordHandler.MSG_OPEN_CAMERA));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        Log.i(TAG,"surfaceChanged,width="+width+"height="+height);

        if (gl != null) {
            gl.glViewport(0, 0, width, height);
        }
    }

    public void setCameraDrawSize(int width, int height) {
        mIncomingWidth = width;
        mIncomingHeight = height;
        Log.i(TAG,"宽度="+width+"高度="+height);

        float scaleHeight = mSurfaceWidth / (width * 1f / height * 1f);
        float surfaceHeight = mSurfaceHeight;

        if (mFullScreen != null) {
            mMvpScaleX = 1f;
            mMvpScaleY = scaleHeight / surfaceHeight;
            mFullScreen.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
            Log.i("zl", "setCameraPreviewSize mMvpScaleX = " + mMvpScaleX + ",mMvpScaleY = " + mMvpScaleY);
        }
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        mSurfaceTexture.updateTexImage();
        if (mNewFilterType != mCurrentFilterType) {
            mFullScreen.changeProgram(FilterManager.getCameraFilter(mNewFilterType, mApplicationContext));
            mCurrentFilterType = mNewFilterType;
            Log.i("zl","onDrawFrame() mCurrentFilterType: " + mCurrentFilterType);
        }
        mFullScreen.getFilter().setTextureSize(mIncomingWidth, mIncomingHeight);
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);

        Log.i(TAG,mSTMatrix[0]+"textMatrix,onDrawFrame mTextureId = " + mTextureId + ",mRecordingEnabled = " + mRecordingEnabled + ",mRecordingStatus = " + mRecordingStatus);

        videoOnDrawFrame(mTextureId, mSTMatrix, mSurfaceTexture.getTimestamp());
    }

    private void videoOnDrawFrame(int textureId, float[] texMatrix, long timestamp) {
        if (mRecordingEnabled && mEncoderConfig != null) {
            switch (mRecordingStatus) {
                case RECORDING_OFF:
                    mEncoderConfig.updateEglContext(EGL14.eglGetCurrentContext());
                    mVideoEncoder.startRecording(mEncoderConfig);
                    mVideoEncoder.setTextureId(textureId);
                    mVideoEncoder.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
                    mRecordingStatus = RECORDING_ON;

                    break;
                case RECORDING_RESUMED:
                    mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                    mVideoEncoder.setTextureId(textureId);
                    mVideoEncoder.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
                    mRecordingStatus = RECORDING_ON;
                    break;
                case RECORDING_ON:
                    // yay
                    mVideoEncoder.updateFilter(mCurrentFilterType);
                    mVideoEncoder.frameAvailable(texMatrix, timestamp);
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        } else {
            switch (mRecordingStatus) {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                    mVideoEncoder.stopRecording();
                    mRecordingStatus = RECORDING_OFF;
                    break;
                case RECORDING_OFF:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        }
    }


    public void notifyPausing() {

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
        mVideoEncoder.stopRecording();
    }

    public void changeFilter(FilterManager.FilterType filterType) {
        mNewFilterType = filterType;
    }

    public void changeFilter2(FilterManager.FilterType filterType) {
        mNewFilterType = filterType;
        mCurrentFilterType = FilterManager.FilterType.Normal;
    }


    public static class CameraRecordHandler extends Handler {
        public static final int MSG_OPEN_CAMERA = 1001;
        public static final int MSG_SWITCH_CAMERA = 1002;
        private CommonHandlerListener listener;

        public CameraRecordHandler(Looper looper, CommonHandlerListener listener) {
            super(looper);
            this.listener = listener;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            listener.handleMessage(msg);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case CameraRecordHandler.MSG_OPEN_CAMERA:
                if (mOnFrameAvailableListener != null) {
                    mSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
                }
                handleOpenCamera();
                setCameraDrawSize(cameraInstance().previewHeight(), cameraInstance().previewWidth());
                break;
            case CameraRecordHandler.MSG_SWITCH_CAMERA:
                handleSwitchCamera();
                break;
        }
    }

    public CameraInstance cameraInstance() {
        return CameraInstance.getInstance();
    }

    private void handleOpenCamera() {
        if (!cameraInstance().isCameraOpened()) {
            setRecordingSize(720, 1280);
            boolean cameraState = cameraInstance().tryOpenCamera(getFacing());
            Log.i(TAG, "handleOpenCamera,cameraState: " + cameraState);
            if (cameraState && !cameraInstance().isPreviewing()) {
                cameraInstance().startPreview(mSurfaceTexture);
            }
            if (mOnCameraStartCallback != null) {
                mOnCameraStartCallback.onCameraStart(cameraState);
            }
        }
    }

    public void switchCamera() {
        mHandlerCameraRecord.sendMessage(mHandlerCameraRecord.obtainMessage(CameraRecordHandler.MSG_SWITCH_CAMERA));
    }

    private void handleSwitchCamera() {
        mIsFacingFront = !mIsFacingFront;
        cameraInstance().releaseCamera();
        boolean cameraState = cameraInstance().tryOpenCamera(getFacing());
        if (cameraState && !cameraInstance().isPreviewing()) {
            cameraInstance().setDisplayOrientation(getRotationCorrection());
            Log.i(TAG, "handleSwitchCamera() ok startPreviewing...");
            cameraInstance().startPreview(mSurfaceTexture);
        }
    }

    public void resumePreview() {
        Log.i(TAG, "resumePreview() mSurfaceTexture: " + mSurfaceTexture);
        if (mSurfaceTexture == null) {
            return;
        }
        if (!cameraInstance().isCameraOpened()) {
            boolean cameraState = cameraInstance().tryOpenCamera(getFacing());
            if (cameraState && !cameraInstance().isPreviewing()) {
                if (!cameraInstance().isPreviewing()) {
                    Log.i(TAG, "resumePreview() ok startPreviewing...");
                    cameraInstance().startPreview(mSurfaceTexture);
                }
            }
        }
    }

    /**
     * 注意,focusAtPoint 会强制 focus mode 为 FOCUS_MODE_AUTO
     * 如果有自定义的focus mode， 请在 AutoFocusCallback 里面重设成所需的focus mode。
     * x,y 取值范围: [0, 1]， 一般为 touchEventPosition / viewSize.
     *
     * @param x
     * @param y
     * @param focusCallback
     */
    public void focusAtPoint(float x, float y, Camera.AutoFocusCallback focusCallback) {
        cameraInstance().focusAtPoint(y, 1.0f - x, focusCallback);
    }

    private int getFacing() {
        return mIsFacingFront ? CameraInstance.CAMERA_FRONT : CameraInstance.CAMERA_BACK;
    }

    private void setRecordingSize(int width, int height) {
        if (width > maxPreviewWidth || height > maxPreviewHeight) {
            float scaling = Math.min(maxPreviewWidth / (float) width, maxPreviewHeight / (float) height);
            width = (int) (width * scaling);
            height = (int) (height * scaling);
        }
        cameraInstance().setPreferPreviewSize(width, height);
        int rotationCorrection = getRotationCorrection();
        cameraInstance().setDisplayOrientation(rotationCorrection);
        Log.i(TAG, String.format("presetRecordingSize,%d x %d, rotationCorrection,%d", width, height, rotationCorrection));
    }


    private int getRotationCorrection() {
        Display display = ((WindowManager) mApplicationContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int displayRotation = display.getRotation() * 90;
        return (cameraInstance().getCameraOrientation(getFacing()) - displayRotation + (mIsFacingFront ? 180 : 360)) % 360;
    }
}
