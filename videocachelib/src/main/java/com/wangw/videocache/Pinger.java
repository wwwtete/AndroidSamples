package com.wangw.videocache;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.wangw.videocache.Preconditions.checkArgument;

/**
 * Created by wangw on 2017/2/25.
 */

public class Pinger {
    private static final String PING_REQUEST = "ping";
    private static final String PING_RESPONSE = "ping ok";

    private final ExecutorService pingExecutor = Executors.newSingleThreadExecutor();
    private final String mHost;
    private final int mProt;

    public Pinger(String proxyHost, int port) {
        this.mHost = proxyHost;
        this.mProt = port;
    }

    /**
     * 是否为Ping的请求
     * @param url
     * @return
     */
    public boolean isPingRequest(String url) {
        return PING_REQUEST.equals(url);
    }

    /**
     * 响应ping的请求
     * @param socket
     * @throws IOException
     */
    public void responseToPin(Socket socket) throws IOException {
        OutputStream ops = socket.getOutputStream();
        ops.write("HTTP/1.1 200 OK\n\n".getBytes());
        ops.write(PING_RESPONSE.getBytes());

    }

    public boolean ping(int maxAttempts, int startTimeout) {
        checkArgument(maxAttempts >= 1);
        checkArgument(startTimeout > 0);
        int timeout = startTimeout;
        int attempts = 0;
        while (attempts < maxAttempts){
            try {
                Future<Boolean> pingFuture = pingExecutor.submit(new PingCallable());
                boolean pinged = pingFuture.get(timeout, TimeUnit.MILLISECONDS);
                if (pinged){
                    return true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            attempts ++;
            timeout *=2;
        }
        return false;
    }

    private class PingCallable implements Callable<Boolean>{
        @Override
        public Boolean call() throws Exception {
            return pinServer();
        }
    }

    private boolean pinServer() throws VideoCacheException {
        String pinURL = getPingUrl();
        HttpUrlSource source = new HttpUrlSource(pinURL);
        try {
            byte[] expectedResponse = PING_RESPONSE.getBytes();
            source.open(0);
            byte[] response = new byte[expectedResponse.length];
            source.read(response);
            return Arrays.equals(expectedResponse,response);
        } catch (VideoCacheException e) {
            e.printStackTrace();
        }finally {
            source.close();
        }
        return false;
    }

    private String getPingUrl() {
        return String.format(Locale.US, "http://%s:%d/%s", mHost, mProt, PING_REQUEST);
    }
}
