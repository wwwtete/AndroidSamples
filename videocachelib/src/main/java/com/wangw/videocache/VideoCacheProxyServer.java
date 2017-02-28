package com.wangw.videocache;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.wangw.videocache.file.DiskUsage;
import com.wangw.videocache.file.FileNameGenerator;
import com.wangw.videocache.file.Md5FileNameGenerator;
import com.wangw.videocache.file.TotalSizeLruDiskUsage;
import com.wangw.videocache.sourcestorage.SourceInfoStorage;
import com.wangw.videocache.sourcestorage.SourceInfoStorageFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.wangw.videocache.Preconditions.checkNotNull;

/**
 * Created by wangw on 2017/2/25.
 */

public class VideoCacheProxyServer {

    public static final String TAG = "VideoCacheLog";
    private static final String PROXY_HOST = "127.0.0.1";

    private final Object mClientsLock = new Object();
    private final ExecutorService mSocketProcessor = Executors.newFixedThreadPool(8);
    private final Map<String,VideoCacheProxyServerClient> mClientsMap = new ConcurrentHashMap<>();
    private final ServerSocket mServerSocket;
    private final int mPort;
    private final Thread mWaitConnectionThread;
    private final Config mConfig;
    private final Pinger mPinger;

    public VideoCacheProxyServer(Context context) {
        this(new Builder(context).buildConfig());
    }

    private VideoCacheProxyServer(Config config){
        this.mConfig = config;
        try {
            InetAddress inetAddress = InetAddress.getByName(PROXY_HOST);
            this.mServerSocket = new ServerSocket(0,8,inetAddress);
            this.mPort = mServerSocket.getLocalPort();
            CountDownLatch latch = new CountDownLatch(1);
            mWaitConnectionThread = new Thread(new WaitRequestRunn(latch));
            mWaitConnectionThread.start();
            //冻结当前线程，直到Sokect处理线程初始化完成后唤醒
            latch.await();
            this.mPinger = new Pinger(PROXY_HOST,mPort);
        } catch (Exception e) {
            mSocketProcessor.shutdown();
            throw new IllegalStateException("初始化VideoCacheProxyServer异常:",e);
        }
    }

    public String getProxyURL(String url){
        return getProxyUrl(url,true);
    }

    /**
     * 获取代理URL
     * @param url 原始URL
     * @param allowCachedFileUri 是否允许以 file://开头的协议
     * @return
     */
    public String getProxyUrl(String url, boolean allowCachedFileUri) {
        if (allowCachedFileUri && isCached(url)){
            File cacheFile = getCacheFile(url);
            touchFileSafely(cacheFile);
            return Uri.fromFile(cacheFile).toString();
        }
        return isAlive() ? appendToProxyUrl(url) : url;
    }

    private String appendToProxyUrl(String url) {
        return String.format(Locale.US, "http://%s:%d/%s", PROXY_HOST, mPort, ProxyCacheUtils.encode(url));
    }

    private boolean isAlive() {
        return true;//mPinger.ping(3,70);
    }

    private void touchFileSafely(File cacheFile) {
        try {
            mConfig.diskUsage.touch(cacheFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean isCached(String url) {
        checkNotNull(url,"Url不能为空");
        return getCacheFile(url).exists();
    }

    private File getCacheFile(String url) {
        File cacheDir = mConfig.cacheRoot;
        String fileName = mConfig.fileNameGenerator.generate(url);
        return new File(url,fileName);
    }

    private class WaitRequestRunn implements Runnable {

        private final CountDownLatch mCountDownLatch;


        public WaitRequestRunn(CountDownLatch latch) {
            mCountDownLatch = latch;
        }

        @Override
        public void run() {
            mCountDownLatch.countDown();
            waitForRequest();
        }
    }

    /**
     * 等待客户端Socket连接
     */
    private void waitForRequest() {
        try {
            while (!Thread.currentThread().isInterrupted()){
                Socket socket = mServerSocket.accept();
                mSocketProcessor.submit(new SocketProcessorRunn(socket));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }






    private class SocketProcessorRunn implements  Runnable {
        private final  Socket mSocket;
        public SocketProcessorRunn(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            processSocket(mSocket);
        }
    }

    private static int test= 0;

    /**
     * 处理客户端请求过来的Socket
     * @param socket
     */
    private void processSocket(Socket socket) {
        try {
            GetRequest request = GetRequest.read(socket.getInputStream());
            String url = ProxyCacheUtils.decode(request.mUri);
            if (mPinger.isPingRequest(url)){
                mPinger.responseToPin(socket);
            }else {
                test ++;
                Log.d(TAG,"test = "+test);
                if(test > 1)
                    return;
                VideoCacheProxyServerClient client = getClient(url);
                client.processRequest(request,socket);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            releaseSocket(socket);
        }
    }

    private VideoCacheProxyServerClient getClient(String url) {
        synchronized (mClientsLock){
            VideoCacheProxyServerClient client = mClientsMap.get(url);
            if (client == null){
                client = new VideoCacheProxyServerClient(url,mConfig);
                mClientsMap.put(url,client);
            }
            return client;
        }
    }

    /**
     * 释放Socket
     * @param socket
     */
    private void releaseSocket(Socket socket) {
        try {
            //关闭输入流
            if (!socket.isInputShutdown()){
                socket.shutdownInput();
            }
            //关闭输出流
            if (!socket.isOutputShutdown()){
                socket.shutdownOutput();
            }
            //关闭Socket
            if (!socket.isClosed()){
                socket.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static final class Builder{

        private static final long DEFAULT_MAX_SIZE = 512 * 1024 * 1024;

        private File mCacheRoot;
        private FileNameGenerator mNameGenerator;
        private DiskUsage mDiskUsage;
        private SourceInfoStorage mSourceInfoStorage;

        public Builder(Context context) {
            this.mSourceInfoStorage = SourceInfoStorageFactory.newSourceInfoStorage(context);
            this.mCacheRoot = StorageUtils.getIndividualCacheDirectory(context);
            this.mDiskUsage = new TotalSizeLruDiskUsage(DEFAULT_MAX_SIZE);
            this.mNameGenerator = new Md5FileNameGenerator();
        }

        /**
         * 设置缓存目录
         * @param cacheRoot
         * @return
         */
        public Builder setCacheRoot(File cacheRoot) {
            mCacheRoot = checkNotNull(cacheRoot);
            return this;
        }

        public Builder setNameGenerator(FileNameGenerator nameGenerator) {
            mNameGenerator = checkNotNull(nameGenerator);
            return this;
        }

        public Builder setDiskUsage(DiskUsage diskUsage) {
            mDiskUsage = diskUsage;
            return this;
        }

        public Builder setSourceInfoStorage(SourceInfoStorage sourceInfoStorage) {
            mSourceInfoStorage = sourceInfoStorage;
            return this;
        }

        public VideoCacheProxyServer build(){
            Config config = buildConfig();
            return new VideoCacheProxyServer(config);
        }

        private Config buildConfig(){
            return new Config(mCacheRoot,mNameGenerator,mDiskUsage,mSourceInfoStorage);
        }

    }
}
