package com.wangw.videocache;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wangw.videocache.Preconditions.checkNotNull;

/**
 * Created by wangw on 2017/2/25.
 */

public class GetRequest {
    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("[R,r]ange:[ ]?bytes=(\\d*)-");
    private static final Pattern URL_PATTERN = Pattern.compile("GET /(.*) HTTP");

    public final String mUri;
    public final  long mRangeOffset;
    public final  boolean mPartial;

    public GetRequest(String request) {
        checkNotNull(request);
        long offset = findRanageOffset(request);
        mRangeOffset = Math.max(0,offset);
        mPartial = offset >= 0;
        mUri = findUri(request);

    }

    private long findRanageOffset(String request) {
        Matcher matcher = RANGE_HEADER_PATTERN.matcher(request);
        if (matcher.find()) {
            String range = matcher.group(1);
            return Long.valueOf(range);
        }
        return -1;
    }

    private String findUri(String request) {
        Matcher matcher= URL_PATTERN.matcher(request);
        if (matcher.find())
            return matcher.group(1);
        throw new IllegalArgumentException("无效的请求："+request+" :没有找到URL");
    }

    public static GetRequest read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
        StringBuilder builder = new StringBuilder();
        String line;
        while (!(TextUtils.isEmpty(line = reader.readLine()))){
            builder.append(line);
        }
        return new GetRequest(builder.toString());
    }
}
