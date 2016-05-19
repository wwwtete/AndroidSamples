package com.wangw.samples.sample_leakcanary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.exlogcat.L;
import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.comm.SamplesModel;

import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by wangw on 2016/4/15.
 */
public class LeakCanaryActivity extends BaseActivity implements SamplesModel {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leakcanary);
    }

    @OnClick(R.id.btn)
    public void onClick(View view){
        startActivity(new Intent(LeakCanaryActivity.this, LeakCanaryTestActivy.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Observable.empty()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        startActivity(new Intent(LeakCanaryActivity.this, LeakCanaryTestActivy.class));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    @Override
    public String getSampleName() {
        return "LeakCanary:检测内存泄漏工具";
    }

    @Override
    protected void finalize() throws Throwable {
        L.e("调用了LeakCanaryActivity的finalize函数");
        super.finalize();
    }
}
