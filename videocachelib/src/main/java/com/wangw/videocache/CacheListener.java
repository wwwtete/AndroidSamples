package com.wangw.videocache;

import java.io.File;

/**
 * Created by wangw on 2017/2/25.
 */

public interface CacheListener {

    /**
     * 缓存回调
     * @param cacheFile 缓存文件
     * @param url  请求URL
     * @param percentsAvailable 已经缓存的百分比
     */
    void onCacheAvailable(File cacheFile,String url,int percentsAvailable);
}
