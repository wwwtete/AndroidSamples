package com.wangw.samples.greendao.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by wangw on 2017/2/22.
 */
@Entity(active = false)
public class User {
    @Id
    public long uid;
    public String name;
    public int sex;

    @Transient
    public String test;
    @Generated(hash = 989422554)
    public User(long uid, String name, int sex) {
        this.uid = uid;
        this.name = name;
        this.sex = sex;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public long getUid() {
        return this.uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return this.sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

}
