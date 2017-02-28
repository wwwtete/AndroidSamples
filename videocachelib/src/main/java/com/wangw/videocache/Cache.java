package com.wangw.videocache;

/**
 * Created by wangw on 2017/2/25.
 */

public interface Cache {

    /**
     * 是否已下载完成
     * @return
     */
    boolean isCompleted();

    /**
     * 有效的数据Length
     * @return
     */
    int available() throws VideoCacheException;

    void append(byte[] buffer, int readBytes) throws VideoCacheException;

    void complete() throws VideoCacheException;

    int read(byte[] buffer, long offset, int length) throws VideoCacheException;

    void close();
}
