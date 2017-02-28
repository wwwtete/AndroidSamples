package com.wangw.videocache;

import android.text.TextUtils;

import com.wangw.videocache.file.FileCache;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.wangw.videocache.ProxyCacheUtils.DEFAULT_BUFFER_SIZE;

/**
 * 真正的读取文件和保存文件
 * Created by wangw on 2017/2/25.
 */

public class HttpProxyCache extends ProxyCache{

    private static final float NO_CACHE_BARRIER = .2f;
    private final HttpUrlSource mSource;
    private final FileCache mCache;
    private CacheListener mListener;

    public HttpProxyCache(HttpUrlSource source, FileCache cache) {
        super(source,cache);
        mSource = source;
        mCache = cache;
    }

    /**
     * 注册监听缓存回调事件
     * @param uiCacheListener
     */
    public void registerCacheListener(CacheListener uiCacheListener) {
        mListener = uiCacheListener;
    }

    /**
     * 处理请求
     * @param request
     * @param socket
     */
    public void processRequest(GetRequest request, Socket socket) throws IOException, VideoCacheException {
        //1.将Socket的输出流转换成BufferOutPutStream输出流
        OutputStream ops = new BufferedOutputStream(socket.getOutputStream());
        //2.构建一个Response响应头字符串
        String responseHeader = buildResponseHeader(request);
        //3.写入到输出流中
        ops.write(responseHeader.getBytes("UTF-8"));
        //获取偏移量
        long offset = request.mRangeOffset;
        //判断是否需要用缓存
        if (isUseCache(request)){
            responseWithCache(ops,offset);
        }else {
            responseWithoutCache(ops,offset);
        }

    }

    /**
     * 不使用缓存
     * @param ops
     * @param offset
     */
    private void responseWithoutCache(OutputStream ops, long offset) throws VideoCacheException {
        HttpUrlSource newSourceNoCache = new HttpUrlSource(mSource);
        try {
            newSourceNoCache.open((int) offset);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int readBytes;
            while ((readBytes = newSourceNoCache.read(buffer)) != -1){
                ops.write(buffer,0,readBytes);
                offset += readBytes;
            }
            ops.flush();
        } catch (VideoCacheException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            newSourceNoCache.close();
        }
    }

    /**
     * 读取缓存数据
     * @param ops
     * @param offset
     * @throws VideoCacheException
     * @throws IOException
     */
    private void responseWithCache(OutputStream ops, long offset) throws VideoCacheException, IOException {
        //缓存使用Buffer
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int readBytes;
        //循环读取数据
        while ((readBytes = read(buffer, offset, buffer.length)) != -1) {
            //将读取到的数据写入到输出流中
            ops.write(buffer,0,readBytes);
            //更新Offset
            offset += readBytes;
        }
        //刷新输出流
        ops.flush();
    }

    private boolean isUseCache(GetRequest request) throws VideoCacheException {
        int sourceLength = mSource.length();
        boolean sourceLengthKnown = sourceLength > 0;
        int cacheAvailable  = mCache.available();
        // do not use cache for partial requests which too far from available cache. It seems user seek video.
        return !sourceLengthKnown || !request.mPartial || request.mRangeOffset <= cacheAvailable + sourceLength*NO_CACHE_BARRIER;
    }

    /**
     * 构建一个响应头字符串
     * @param request
     * @return
     */
    private String buildResponseHeader(GetRequest request) throws VideoCacheException {
        //从HttpUrlSource对象中获取Mime（）
        String mime = mSource.getMime();
        boolean mimeKnown = !TextUtils.isEmpty(mime);
        int length = mCache.isCompleted() ? mCache.available() : mSource.length();
        boolean lengthKnown = length >= 0;
        long contentLength = request.mPartial ? length - request.mRangeOffset : length;
        boolean addRange = lengthKnown && request.mPartial;
        return new StringBuffer()
                .append(request.mPartial ? "HTTP/1.1 206 PARTIAL CONTENT\n" : "HTTP/1.1 200 OK\n")
                .append("Accept-Ranges: bytes\n")
                .append(lengthKnown ? String.format("Content-Length: %d\n",contentLength) : "")
                .append(addRange ? String.format("Content-Range: bytes %d-%d/%d\n", request.mRangeOffset, length - 1, length) : "")
                .append(mimeKnown ? String.format("Content-Type: %s\n", mime) : "")
                .append("\n") // headers end
                .toString();
    }


}
