package com.wangw.samples.refreshlayout;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wangw on 2016/5/12.
 */
public abstract class BaseRefreshActivity extends BaseActivity {

    ListView mListview;
    protected ArrayAdapter<String> mAdapter;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        initListView();
    }

    private void initListView(){
        mListview = (ListView) findViewById(R.id.listview);
        mAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mAdapter.addAll(getData());

        mListview.setAdapter(mAdapter);
    }

    protected List<String> getData() {
        List<String> arr = new ArrayList<>();
        for (int i=0;i<5;i++){
            arr.add( "item"+(mAdapter.getCount()+i));
        }
        return arr;
    }

    private void onUpdateData(final boolean isMore){
        Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                subscriber.onNext(getData());
                subscriber.onCompleted();
            }
        })
                .delay(3000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> strings) {
                        if(!isMore){
                            mAdapter.clear();
                        }
                        mAdapter.addAll(strings);
                        onComplete();
                    }
                });
    }

    protected abstract void onComplete();


    public void onRefresh() {
        mAdapter.clear();
        onUpdateData(false);
    }

    public void onLoad() {
        onUpdateData(true);
    }

}
