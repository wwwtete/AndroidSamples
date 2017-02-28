package com.wangw.videocache;

/**
 * Created by wangw on 2017/2/27.
 */

public class VideoCacheException extends Exception {
    public VideoCacheException(String detailMessage) {
        super(detailMessage);
    }

    public VideoCacheException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public VideoCacheException(Throwable throwable) {
        super(throwable);
    }
}
