package com.wangw.videocache;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.wangw.videocache.file.FileCache;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wangw.videocache.Preconditions.checkNotNull;

/**
 * Created by wangw on 2017/2/25.
 */

public class VideoCacheProxyServerClient {

    private final AtomicInteger mClientsCount = new AtomicInteger(0);
    private final String mUrl;
    private volatile HttpProxyCache mProxyCache;
    private final List<CacheListener> mCacheListeners = new CopyOnWriteArrayList<>();
    private final CacheListener mUICacheListener;
    private final Config mConfig;

    public VideoCacheProxyServerClient(String url, Config config) {
        mUrl = checkNotNull(url);
        mConfig = checkNotNull(config);
        mUICacheListener = new UIListenerHandler(mUrl,mCacheListeners);

    }

    public void processRequest(GetRequest request, Socket socket) throws IOException {
        //初始化HttpProxyCache
        try {
            onStartProcessRequest();
        } catch (VideoCacheException e) {
            e.printStackTrace();
        }
        try {
            //ClientsCount +1
            mClientsCount.incrementAndGet();
            //处理请求
            mProxyCache.processRequest(request,socket);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            finishProcessRequest();
        }
    }

    private void finishProcessRequest() {
        if (mClientsCount.decrementAndGet() <= 0){
            mProxyCache.shutdown();
            mProxyCache = null;
        }
    }

    private void onStartProcessRequest() throws IOException, VideoCacheException {
        mProxyCache = mProxyCache == null ? newHttpProxyCache() : mProxyCache;

    }

    private HttpProxyCache newHttpProxyCache() throws IOException, VideoCacheException {
        HttpUrlSource source = new HttpUrlSource(mUrl,mConfig.sourceInfoStorage);
        FileCache cache = new FileCache(mConfig.generateCacheFile(mUrl),mConfig.diskUsage);
        HttpProxyCache proxyCache = new HttpProxyCache(source,cache);
        proxyCache.registerCacheListener(mUICacheListener);
        return proxyCache;
    }


    private static final class UIListenerHandler extends Handler implements CacheListener{

        private final String mUrl;
        private final List<CacheListener> mListeners;

        public UIListenerHandler(String url, List<CacheListener> listeners) {
            super(Looper.getMainLooper());
            mUrl = url;
            mListeners = listeners;
        }

        @Override
        public void handleMessage(Message msg) {
            for (CacheListener listener : mListeners) {
                listener.onCacheAvailable((File) msg.obj,mUrl,msg.arg1);
            }

        }

        @Override
        public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
            Message msg= obtainMessage();
            msg.arg1 = percentsAvailable;
            msg.obj = cacheFile;
            sendMessage(msg);
        }
    }
}
