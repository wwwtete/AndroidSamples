package com.wangw.samples.sample_leakcanary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.exlogcat.L;
import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class LeakCanaryTestActivy extends BaseActivity {

    @Bind(R.id.tv_content)
    TextView mTvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leakcanarytest_activy);
        ButterKnife.bind(this);


        onInit();
    }

    private void onInit() {
        XXSingleton.getInstance().setView(mTvContent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Observable.empty()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        LeakCanaryTestActivy.this.finish();
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
    protected void onDestroy() {
        super.onDestroy();
        L.d("调用了LeakCanaryTestActivy的onDestroy函数");
    }

    @Override
    protected void finalize() throws Throwable {
        L.e("LeakCanaryTestActivy被回收了！");
        super.finalize();
    }

    @Override
    public String getSampleName() {
        return null;
    }
}
