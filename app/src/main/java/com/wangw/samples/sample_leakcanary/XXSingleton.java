package com.wangw.samples.sample_leakcanary;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangw on 2016/4/15.
 */
public class XXSingleton {

    private static XXSingleton mInstance;

    public static XXSingleton getInstance(){
        if(mInstance == null)
            mInstance = new XXSingleton();

        return mInstance;
    }

    private List<View> mViews = new ArrayList<>();

    public void setView(View view){
        this.mViews.add(view);
    }



}
