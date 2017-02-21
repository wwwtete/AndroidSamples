package com.wangw.samples;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.exlogcat.L;
import com.wangw.samples.comm.SampleListView;
import com.wangw.samples.comm.SamplesModel;

import butterknife.ButterKnife;

/**
 * Created by wangw on 2016/4/14.
 */
public abstract class BaseActivity extends AppCompatActivity implements SamplesModel {


    protected SampleListView mListView;
    protected ProgressDialog mProgressDialog;

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

    protected void setDefaultView(){
        setContentView(R.layout.activity_home);
        mListView = (SampleListView) findViewById(R.id.listview);
    }

    protected void addSampleClass(Class<? extends BaseActivity> clz){
        if(mListView != null){
            mListView.addSample(clz);
        }
    }

    public void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    protected void showLoading(){
        showLoading(true);
    }

    protected void showLoading(boolean cancelable)
    {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("正在处理中...");
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.show();
    }
    protected void closeLoading()
    {
        if( mProgressDialog!=null){
            mProgressDialog.cancel();
            mProgressDialog=null;
        }
    }
}
