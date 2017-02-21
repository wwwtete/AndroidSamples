package com.wangw.samples.media.filter.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.wangw.samples.media.filter.camera.CameraInstance;
import com.wangw.samples.media.filter.camera.CameraRecordRenderer;
import com.wangw.samples.media.filter.filter.FilterManager;


public class CameraSurfaceView extends GLSurfaceView
        implements SurfaceTexture.OnFrameAvailableListener {

    private CameraRecordRenderer mCameraRenderer;

    public CameraSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        setEGLContextClientVersion(2);

        mCameraRenderer = new CameraRecordRenderer(context.getApplicationContext());
        mCameraRenderer.setOnFrameAvailableListener(this);
        setRenderer(mCameraRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public CameraRecordRenderer getRenderer() {
        return mCameraRenderer;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        CameraInstance.getInstance().releaseCamera();
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraRenderer.notifyPausing();
            }
        });

        super.onPause();
    }

    public void changeFilter(FilterManager.FilterType filterType) {
        mCameraRenderer.changeFilter(filterType);
    }

    public void changeFilter2(FilterManager.FilterType filterType) {
        mCameraRenderer.changeFilter2(filterType);
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

}