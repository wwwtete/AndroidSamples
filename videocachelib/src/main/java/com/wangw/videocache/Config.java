package com.wangw.videocache;

import com.wangw.videocache.file.DiskUsage;
import com.wangw.videocache.file.FileNameGenerator;
import com.wangw.videocache.sourcestorage.SourceInfoStorage;

import java.io.File;

/**
 * Created by wangw on 2017/2/25.
 */

public class Config {

    public final File cacheRoot;
    public final FileNameGenerator fileNameGenerator;
    public final DiskUsage diskUsage;
    public final SourceInfoStorage sourceInfoStorage;

    Config(File cacheRoot, FileNameGenerator fileNameGenerator, DiskUsage diskUsage, SourceInfoStorage sourceInfoStorage) {
        this.cacheRoot = cacheRoot;
        this.fileNameGenerator = fileNameGenerator;
        this.diskUsage = diskUsage;
        this.sourceInfoStorage = sourceInfoStorage;
    }

    File generateCacheFile(String url) {
        String name = fileNameGenerator.generate(url);
        return new File(cacheRoot, name);
    }
}
