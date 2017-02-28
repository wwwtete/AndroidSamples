package com.wangw.videocache;

import android.text.TextUtils;

import com.wangw.videocache.sourcestorage.SourceInfoStorage;
import com.wangw.videocache.sourcestorage.SourceInfoStorageFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;


/**
 * Created by wangw on 2017/2/25.
 */

public class HttpUrlSource implements Source {

    private final static int MAX_REDIRECTS = 5;
    private final SourceInfoStorage mSourceInfoStorage;
    private SourceInfo mSourceInfo;
    private HttpURLConnection mConnection;
    private BufferedInputStream mInputStream;

    public HttpUrlSource(String url) {
        this(url, SourceInfoStorageFactory.newEmptySourceInfoStorage());
    }
    public HttpUrlSource(String url, SourceInfoStorage sourceInfoStorage) {
        this.mSourceInfoStorage = sourceInfoStorage;
        SourceInfo sourceInfo =  mSourceInfoStorage.get(url);
        this.mSourceInfo = sourceInfo != null ? sourceInfo : new SourceInfo(url,Integer.MIN_VALUE,ProxyCacheUtils.getSupposablyMime(url));
    }

    public HttpUrlSource(HttpUrlSource source) {
        mSourceInfo = source.mSourceInfo;
        mSourceInfoStorage = source.mSourceInfoStorage;
    }

    public String getUrl() {
        return mSourceInfo.url;
    }

    /**
     * 获取Mime
     * （如果本地没有，则创建一个HttpURLConnection链接去请求）
     * @return
     */
    public synchronized String getMime() {
        if (TextUtils.isEmpty(mSourceInfo.mime))
            getContentinfoForNet();
        return mSourceInfo.mime;
    }

    /**
     * 从服务器获取视频的信息内容
     */
    private void getContentinfoForNet() {
        HttpURLConnection connection;
        InputStream ips = null;
        try {
            //连接网络
            connection = onConnection(0,10000);
            int length = connection.getContentLength();
            String mime = connection.getContentType();
            ips = connection.getInputStream();
            //重新构建SourceInfo
            mSourceInfo = new SourceInfo(mSourceInfo.url,length,mime);
            //保存到数据库
            this.mSourceInfoStorage.put(mSourceInfo.url,mSourceInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            ProxyCacheUtils.close(ips);
        }
    }

    /**
     * 打开并连接网络
     * @param offset 请求数据的偏移量
     * @param timeOut   超时时间
     * @return
     */
    private HttpURLConnection onConnection(int offset, int timeOut) throws Exception {
        HttpURLConnection connection;
        boolean redirected;
        int redirectedCount = 0;
        String url = mSourceInfo.url;
        do {
            connection = (HttpURLConnection) new URL(url).openConnection();
            //设置请求数据的偏移量
            if (offset > 0){
                connection.setRequestProperty("Range","bytes="+offset+"_");
            }
            //设置超时时间
            if (timeOut > 0){
                connection.setConnectTimeout(timeOut);
                connection.setReadTimeout(timeOut);
            }
            //获取请求状态码
            int code = connection.getResponseCode();
            //是否需要重试
            redirected = code == HTTP_MOVED_PERM || code == HTTP_MOVED_TEMP || code == HTTP_SEE_OTHER;
            if (redirected){
                url = connection.getHeaderField("Location");
                redirectedCount++;
                connection.disconnect();
            }
            //判断是否已到达最大重试次数
            if (redirectedCount > MAX_REDIRECTS){
                throw new Exception("连接服务器失败");
            }
        }while (redirected);
        return connection;
    }

    @Override
    public void open(int offset) throws VideoCacheException {
        try {
            mConnection = onConnection(offset,-1);
            String mime =mConnection.getContentType();
            mInputStream = new BufferedInputStream(mConnection.getInputStream());
            int length = readSourceAvailableBytes(mConnection,offset,mConnection.getResponseCode());
            this.mSourceInfo = new SourceInfo(mSourceInfo.url,length,mime);
            mSourceInfoStorage.put(mSourceInfo.url,mSourceInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VideoCacheException("在连接网络时异常",e);
        }
    }

    private int readSourceAvailableBytes(HttpURLConnection connection, int offset, int responseCode) {
        int length = mConnection.getContentLength();
        return responseCode == HTTP_OK ? length
                : responseCode == HTTP_PARTIAL ? length+offset : mSourceInfo.length;
    }

    @Override
    public int length() {
        if (mSourceInfo.length == Integer.MIN_VALUE)
            getContentinfoForNet();
        return mSourceInfo.length;
    }

    @Override
    public int read(byte[] buffer) throws VideoCacheException {
        if (mInputStream == null)
            throw new VideoCacheException("从网络读取数据时异常:"+mSourceInfo.url+"Connection是不存在的");
        try {
            return mInputStream.read(buffer,0,buffer.length);
        } catch (IOException e) {
            e.printStackTrace();
            throw new VideoCacheException("从网络读取数据时异常:"+mSourceInfo.url,e);
        }
    }

    @Override
    public void close() throws VideoCacheException {
        if (mConnection != null){
            try {
                mConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
