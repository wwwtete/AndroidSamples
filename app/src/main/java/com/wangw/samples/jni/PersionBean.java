package com.wangw.samples.jni;

/**
 * Created by wangw on 2017/3/6.
 */

public class PersionBean {

    public int id;
    public String name;

    public PersionBean() {
    }

    public PersionBean(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return "id="+id
                +" | "
                +"name = "+name;
    }
}
