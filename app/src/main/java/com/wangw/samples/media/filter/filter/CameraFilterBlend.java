package com.wangw.samples.media.filter.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.wangw.samples.R;
import com.wangw.samples.media.filter.gles.GlUtil;

import java.nio.FloatBuffer;

public class CameraFilterBlend extends CameraFilter {

    protected int mExtraTextureId;
    protected int maExtraTextureCoordLoc;
    protected int muExtraTextureLoc;

    public CameraFilterBlend(Context context, Bitmap bitmap) {
        super(context);
        mExtraTextureId = GlUtil.createTexture(GLES20.GL_TEXTURE_2D, bitmap);
    }


    public CameraFilterBlend(Context context, Bitmap bitmap, boolean isForCamera) {
        super(context,isForCamera);
        mExtraTextureId = GlUtil.createTexture(GLES20.GL_TEXTURE_2D, bitmap);
    }

    @Override
    protected int createProgram(Context applicationContext) {
        return GlUtil.createProgram(applicationContext, R.raw.vertex_shader_two_input,
                R.raw.fragment_shader_ext_blend, mIsForCamera);
    }

    @Override
    protected void getGLSLValues() {
        super.getGLSLValues();
        maExtraTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aExtraTextureCoord");
        muExtraTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "uExtraTexture");
    }

    @Override
    protected void bindTexture(int textureId) {
        super.bindTexture(textureId);
        if(mExtraTextureId != -1){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mExtraTextureId);
            GLES20.glUniform1i(muExtraTextureLoc, 1);
        }
    }

    @Override
    protected void bindGLSLValues(float[] mvpMatrix, FloatBuffer vertexBuffer, int coordsPerVertex,
                                  int vertexStride, float[] texMatrix, FloatBuffer texBuffer, int texStride) {
        super.bindGLSLValues(mvpMatrix, vertexBuffer, coordsPerVertex, vertexStride, texMatrix,
                texBuffer, texStride);
        GLES20.glEnableVertexAttribArray(maExtraTextureCoordLoc);
        GLES20.glVertexAttribPointer(maExtraTextureCoordLoc, 2, GLES20.GL_FLOAT, false, texStride,
                texBuffer);
    }

    @Override
    protected void unbindGLSLValues() {
        super.unbindGLSLValues();

        GLES20.glDisableVertexAttribArray(maExtraTextureCoordLoc);
    }

    @Override
    protected void unbindTexture() {
        super.unbindTexture();
        if(mExtraTextureId != -1){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        }
    }
}