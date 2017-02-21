package com.wangw.samples.media.filter.video;

/**
 * Created by xingliao_zgl on 16/8/26.
 */
public interface OnVideoProcessCallback {
    void onVideoProcessComplete();

    void onVideoProcessSuccess();

    void onVideoProcessFailed(String errMsg);

    void onVideoProcessCanceled();
}
