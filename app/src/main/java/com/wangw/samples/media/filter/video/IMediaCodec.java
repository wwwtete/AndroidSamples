package com.wangw.samples.media.filter.video;

import android.content.Context;

/**
 * Created by xingliao_zgl on 16/8/26.
 */
public interface IMediaCodec {
    void setOnVideoProcessCallback(OnVideoProcessCallback callback);
    void startProcessVideo(Context context, CodecConfig config);
    void cancelProcessVideo();

}
