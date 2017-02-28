package com.wangw.videocache;

/**
 * 文件信息
 * Created by wangw on 2017/2/25.
 */

public class SourceInfo {
    public final String url;
    public final int length;
    public final String mime;

    public SourceInfo(String url, int length, String mime) {
        this.url = url;
        this.length = length;
        this.mime = mime;
    }

    @Override
    public String toString() {
        return "SourceInfo{" +
                "url='" + url + '\'' +
                ", length=" + length +
                ", mime='" + mime + '\'' +
                '}';
    }
}
