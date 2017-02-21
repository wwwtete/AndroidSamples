package com.wangw.samples.media.filter.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.wangw.samples.R;
import com.wangw.samples.media.filter.gles.GlUtil;

import java.nio.FloatBuffer;

public class CameraFilterBeauty extends CameraFilter {

    protected int mSingleStepOffsetLocation;
    protected int mParamsLocation;

    private int mBeautyLevel = 5;
    protected int mBeautyTextureId;

    /**
     * @param context
     * @param beautyLevel 美颜级别,1~5
     */
    public CameraFilterBeauty(Context context, int beautyLevel) {
        super(context);
        mBeautyLevel = beautyLevel;
        mBeautyTextureId = GlUtil.createTexture(GLES20.GL_TEXTURE_2D);
    }

    public CameraFilterBeauty(Context context, int beautyLevel, boolean isForCamera) {
        super(context, isForCamera);
        mBeautyLevel = beautyLevel;
        mBeautyTextureId = GlUtil.createTexture(GLES20.GL_TEXTURE_2D);
    }

    @Override
    protected int createProgram(Context applicationContext) {
        return GlUtil.createProgram(applicationContext, R.raw.vertex_shader_default,
                R.raw.fragment_shader_ext_beauty,mIsForCamera);
    }

    @Override
    protected void getGLSLValues() {
        super.getGLSLValues();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(mProgramHandle, "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(mProgramHandle, "params");
    }

    @Override
    protected void bindTexture(int textureId) {
        super.bindTexture(textureId);
    }

    @Override
    protected void bindGLSLValues(float[] mvpMatrix, FloatBuffer vertexBuffer, int coordsPerVertex,
                                  int vertexStride, float[] texMatrix, FloatBuffer texBuffer, int texStride) {
        super.bindGLSLValues(mvpMatrix, vertexBuffer, coordsPerVertex, vertexStride, texMatrix, texBuffer, texStride);
        setBeautyLevel(mBeautyLevel);
        if (mIncomingWidth != 0 && mIncomingHeight != 0) {
//            setFloatVec2(mSingleStepOffsetLocation, new float[] {2.0f / mIncomingWidth, 2.0f / mIncomingHeight});
            GLES20.glUniform2fv(mSingleStepOffsetLocation, 1, FloatBuffer.wrap(new float[]{2.0f / mIncomingWidth, 2.0f / mIncomingHeight}));
        }
    }

    @Override
    public void setTextureSize(int width, int height) {
        super.setTextureSize(width, height);
    }

    @Override
    protected void unbindGLSLValues() {
        super.unbindGLSLValues();
    }

    @Override
    protected void unbindTexture() {
        super.unbindTexture();
    }

    public void setBeautyLevel(int level) {
        switch (level) {
            case 1:
//                GLES20.glUniform1f(mParamsLocation, 1.0f);
//                setFloatVec4(mParamsLocation, new float[] {1.0f, 1.0f, 0.15f, 0.15f});
                GLES20.glUniform4fv(mParamsLocation, 1, FloatBuffer.wrap(new float[]{1.0f, 1.0f, 0.15f, 0.15f}));
                break;
            case 2:
//                GLES20.glUniform1f(mParamsLocation, 0.8f);
//                setFloatVec4(mParamsLocation, new float[] {0.8f, 0.9f, 0.2f, 0.2f});
                GLES20.glUniform4fv(mParamsLocation, 1, FloatBuffer.wrap(new float[]{0.8f, 0.9f, 0.2f, 0.2f}));
                break;
            case 3:
//                GLES20.glUniform1f(mParamsLocation, 0.6f);
//                setFloatVec4(mParamsLocation, new float[] {0.6f, 0.8f, 0.25f, 0.25f});
                GLES20.glUniform4fv(mParamsLocation, 1, FloatBuffer.wrap(new float[]{0.6f, 0.8f, 0.25f, 0.25f}));
                break;
            case 4:
//                GLES20.glUniform1f(mParamsLocation, 0.4f);
//                setFloatVec4(mParamsLocation, new float[] {0.4f, 0.7f, 0.38f, 0.3f});
                GLES20.glUniform4fv(mParamsLocation, 1, FloatBuffer.wrap(new float[]{0.4f, 0.7f, 0.38f, 0.3f}));
                break;
            case 5:
//                GLES20.glUniform1f(mParamsLocation, 0.2f);
//                setFloatVec4(mParamsLocation, new float[] {0.33f, 0.63f, 0.4f, 0.35f});
                GLES20.glUniform4fv(mParamsLocation, 1, FloatBuffer.wrap(new float[]{0.33f, 0.63f, 0.4f, 0.35f}));
                break;
            default:
                break;
        }
    }
}