package com.wangw.videocache;

import android.util.Log;

import com.wangw.videocache.file.FileCache;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangw on 2017/2/27.
 */

public class ProxyCache {

    private static final int MAX_READ_SOURCE_ATTEMPTS = 1;

    private final Source mSource;
    private final Cache mCache;
    private final Object mWC = new Object();
    private final Object mStopLock = new Object();
    private final AtomicInteger mReadSourceErrorsCount;
    private volatile Thread mSourceReaderThread;
    private volatile boolean mStoped;
    private volatile Thread mSourceReadThread;
    private volatile int mPercentAvailable = -1;

    public ProxyCache(HttpUrlSource source, FileCache cache) {
        mSource= source;
        mCache= cache;
        mReadSourceErrorsCount = new AtomicInteger();
    }

    public int read(byte[] buffer, long offset, int length) throws VideoCacheException {
//        Log.d("SSSSSS","[start] offset ="+offset+" length="+length);
        //检查Buffer的参数值
        ProxyCacheUtils.assertBuffer(buffer,offset,length);
        //如果没有下载完并且本地已保存的数据小于请求的数据值并且没有Stop则从网络获取
        while (!mCache.isCompleted() && mCache.available() < (offset + length) && !mStoped) {
            //开启从网络获取数据的线程
            readSourceAsync();
            //暂停当前线程1秒等待从网络读取到数据后唤醒当前线程
            waitForSourceData();
            checkReadSourceErrorsCount();
        }
        Log.d("SSSSSS2"," av="+mCache.available());
        int read = mCache.read(buffer,offset,length);
        Log.d("SSSSSS","[end] offset ="+offset+" length="+length+" read="+read+" av="+mCache.available());
        if (mCache.isCompleted() && mPercentAvailable != 100){
            mPercentAvailable = 100;
            onCachePercentsAvailableChanged(100);
        }
        return read;
    }

    private void checkReadSourceErrorsCount() throws VideoCacheException {
        int errsCount = mReadSourceErrorsCount.get();
        if (errsCount >= MAX_READ_SOURCE_ATTEMPTS){
            mReadSourceErrorsCount.set(0);
            throw new VideoCacheException("读取数据异常");
        }
    }

    private void waitForSourceData() throws VideoCacheException {
        synchronized (mWC){
            try {
                mWC.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new VideoCacheException("等待读取数据的线程被中断",e);
            }
        }
    }

    private synchronized void readSourceAsync() {
        //判断线程是否已经开启在读取数据了
        boolean readingInProgress = mSourceReadThread != null && mSourceReadThread.getState() != Thread.State.TERMINATED;
        //如果没有Stop并且没有下载完视频并且没有开启线程，则开启读取线程
        if (!mStoped && !mCache.isCompleted() && !readingInProgress){
            mSourceReadThread = new Thread(new SourceReaderRunn(),"异步读取数据源线程");
            mSourceReadThread.start();
        }
    }

    public void shutdown() {
        synchronized (mStopLock){
            mStoped = true;
            if (mSourceReaderThread != null){
                mSourceReaderThread.interrupt();
            }
            mCache.close();
        }
    }

    private class SourceReaderRunn implements Runnable{
        @Override
        public void run() {
            readSource();
        }
    }

    /**
     * 从网络读取数据
     */
    private void readSource() {
        int sourceAvailable = -1;
        int offset = 0;
        try {
            //本地已缓存的数据
            offset = mCache.available();
            //连接网络，并从网络获取数据源信息
            mSource.open(offset);
            //剩余的文件size
            sourceAvailable = mSource.length();
            //构建一个缓存Buffer
            byte[] buffer = new byte[ProxyCacheUtils.DEFAULT_BUFFER_SIZE];
            int readBytes;
            //循环从网络读取数据，直到读完或stop后停止循环
            while ((readBytes = mSource.read(buffer)) != -1){
                synchronized (mStopLock){
                    //判断是否已经Stop了
                    if (isStoped()){
                        return;
                    }
                    //将从网络读取到的数据追加到缓存文件中
                    mCache.append(buffer,readBytes);
                }
                //增加缓存偏移量
                offset += readBytes;
                //通知新的缓存数据已经可用了
                notifyNewCacheDataAvailable(offset,sourceAvailable);
            }
            //尝试将文件更改为完成状态
            tryComplete();
            //更改缓存百分比
            onSourceRead();
        } catch (Exception e) {
            e.printStackTrace();
            mReadSourceErrorsCount.incrementAndGet();
        }finally {
            closeSource();
            notifyNewCacheDataAvailable(offset,sourceAvailable);
        }
    }

    private void closeSource() {
        try {
            mSource.close();
        } catch (VideoCacheException e) {
            e.printStackTrace();
        }
    }

    private void onSourceRead() {
        mPercentAvailable = 100;
        onCachePercentsAvailableChanged(mPercentAvailable);
    }

    private void tryComplete() throws VideoCacheException {
        synchronized (mStopLock){
            if (!isStoped() && mCache.available() == mSource.length()){
                mCache.complete();
            }
        }
    }

    /**
     * 通知新的缓存数据可用了
     * @param cacheAvailable
     * @param sourceAvailable
     */
    private void notifyNewCacheDataAvailable(int cacheAvailable, int sourceAvailable) {
        onCacheAvailable(cacheAvailable,sourceAvailable);
        //唤醒线程
        synchronized (mWC){
            mWC.notifyAll();
        }
    }

    /**
     * 计算缓存百分比并回调Listener
     * @param cacheAvailable
     * @param sourceAvailable
     */
    private void onCacheAvailable(int cacheAvailable, int sourceAvailable) {
        boolean zeroLengthSource = sourceAvailable == 0;
        int percents = zeroLengthSource ? 100 : (int) (cacheAvailable * 100 / sourceAvailable);
        boolean percentsChanged = percents != mPercentAvailable;
        boolean sourceLengthKnown = sourceAvailable >= 0;
        if (sourceLengthKnown && percentsChanged)
            onCachePercentsAvailableChanged(percents);
        mPercentAvailable = percents;
    }

    private void onCachePercentsAvailableChanged(int percents) {
    }

    private boolean isStoped() {
        return Thread.currentThread().isInterrupted() || mStoped;
    }
}
