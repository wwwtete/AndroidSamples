package com.wangw.videocache.file;

import com.wangw.videocache.Cache;
import com.wangw.videocache.VideoCacheException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by wangw on 2017/2/25.
 */

public class FileCache implements Cache {

    private static final String TEMP_POSTFIX = ".dwl";
    private final DiskUsage mDiskUsage;
    private RandomAccessFile mDataFile;
    private File mFile;

    public FileCache(File file, DiskUsage diskUsage) throws IOException {
            if (diskUsage == null)
                throw new NullPointerException("DiskUsage 对象不能为空");
            this.mDiskUsage = diskUsage;
            File dir = file.getParentFile();
            Files.makeDir(dir);
            boolean completed = file.exists();
//            mFile = new File(dir,"test.mp4");
            this.mFile = completed ? file : new File(file.getParentFile(),file.getName()+TEMP_POSTFIX);
            //构建一个随机读写的IO流
            this.mDataFile = new RandomAccessFile(this.mFile,completed ? "r" : "rw");
    }

    @Override
    public synchronized boolean isCompleted() {
        return !isTempFile(mFile);
    }

    private boolean isTempFile(File file) {
        return file.getName().endsWith(TEMP_POSTFIX);
    }

    @Override
    public synchronized int available() throws VideoCacheException {
        try {
            return (int) mDataFile.length();
        } catch (IOException e) {
            e.printStackTrace();
            throw new VideoCacheException("读取本地文件Length异常",e);
        }
    }

    @Override
    public synchronized void append(byte[] buffer, int readBytes) throws VideoCacheException {
        try {
            if (isCompleted()){
                throw new VideoCacheException("追加文件数据错误,缓存文件"+mFile+"已经缓存完成了");
            }
            mDataFile.seek(available());
            mDataFile.write(buffer,0,readBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new VideoCacheException("追加文件数据错误,缓存文件"+mFile,e);
        }
    }

    @Override
    public synchronized void complete() throws VideoCacheException {
        if (isCompleted())
            return;
        close();
        String fileName = mFile.getName().substring(0,mFile.getName().length()-TEMP_POSTFIX.length());
        File completeFile = new File(mFile.getParentFile(),fileName);
        boolean renamed = mFile.renameTo(completeFile);
        if (!renamed){
            throw new VideoCacheException("文件重命名是异常:将"+mFile+"重命名为"+completeFile+"");
        }
        mFile = completeFile;
        try {
            mDataFile = new RandomAccessFile(mFile,"r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new VideoCacheException("打开文件异常:"+mFile,e);
        }
    }

    @Override
    public synchronized int read(byte[] buffer, long offset, int length) throws VideoCacheException {
        try {
            mDataFile.seek(offset);
            return mDataFile.read(buffer,0,length);
        } catch (IOException e) {
            e.printStackTrace();
            throw new VideoCacheException("读取缓存文件异常");
        }
    }

    @Override
    public void close() {
        try{
            mDataFile.close();
            mDiskUsage.touch(mFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
