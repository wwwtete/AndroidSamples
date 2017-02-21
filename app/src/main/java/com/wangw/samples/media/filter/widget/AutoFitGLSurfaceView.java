package com.wangw.samples.media.filter.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

public class AutoFitGLSurfaceView extends GLSurfaceView {

    protected int mRatioWidth = 0;
    protected int mRatioHeight = 0;

    public AutoFitGLSurfaceView(Context context) {
        super(context);
    }

    public AutoFitGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                int _height = width * mRatioHeight / mRatioWidth;
                Log.i("zl","111,width: " + width + ",_height:" + _height);
                setMeasuredDimension(width, _height);
            } else {
                int _width = height * mRatioWidth / mRatioHeight;
                Log.i("zl","222,_width: " + _width + ",height:" + height);
                setMeasuredDimension(_width, height);
            }
        }
    }
}
