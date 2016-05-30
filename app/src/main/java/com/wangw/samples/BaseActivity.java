package com.wangw.samples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.exlogcat.L;
import com.wangw.samples.comm.SamplesModel;

import butterknife.ButterKnife;

/**
 * Created by wangw on 2016/4/14.
 */
public abstract class BaseActivity extends AppCompatActivity implements SamplesModel {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
        L.d("Activity At ("+getClass().getSimpleName()+".java:0)");
    }
}
