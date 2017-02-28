package com.wangw.videocache;

/**
 * Created by wangw on 2017/2/25.
 */

public interface Source {

    /**
     * 链接网络
     * @param offset 数据偏移量
     * @throws VideoCacheException
     */
    void open(int offset) throws VideoCacheException;

    /**
     * 获取源文件的长度
     * @return
     */
    int length();


    int read(byte[] buffer) throws VideoCacheException;

    void close() throws VideoCacheException;
}
