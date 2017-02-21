package com.wangw.samples.media.filter.filter;

import android.content.Context;
import android.graphics.Bitmap;

import com.wangw.samples.R;
import com.wangw.samples.media.filter.gles.GlUtil;


public class CameraFilterBlendSoftLight extends CameraFilterBlend {

    public CameraFilterBlendSoftLight(Context context, Bitmap bitmap) {
        super(context, bitmap);
    }

    public CameraFilterBlendSoftLight(Context context, Bitmap bitmap, boolean isForCamera) {
        super(context, bitmap, isForCamera);
    }

    @Override
    protected int createProgram(Context applicationContext) {

        return GlUtil.createProgram(applicationContext, R.raw.vertex_shader_two_input,
                R.raw.fragment_shader_ext_blend_soft_light, mIsForCamera);
    }
}