package com.wangw.samples.jni;

/**
 * Created by wangw on 2017/3/6.
 */

public class StructInfo {

    public String info;
    public long size;

    public StructInfo() {
    }

    public StructInfo(String info, long size) {
        this.info = info;
        this.size = size;
    }

    @Override
    public String toString() {
        return "info = "+info
                +" |"
                +"size = "
                +size;
    }
}
