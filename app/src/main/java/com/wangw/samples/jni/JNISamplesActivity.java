package com.wangw.samples.jni;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

/**
 * Created by wangw on 2017/3/4.
 */

public class JNISamplesActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jni_sample);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_frist:
                onDemo();
                break;
            case R.id.btn_two:
                break;
        }
    }

    private void onDemo() {
        showToast(NativeMethods.hellowWord());
        NativeMethods.stringToJni("Hello To JNI");
        showStr("sum = "+NativeMethods.sum(20.2f,30.0f));
        showStr("数组总和"+NativeMethods.intArrayToJni(new int[]{10,20,30,40}));

        String[] arr = NativeMethods.stringArrayFromJni(new String[]{"Hello","Java"});
        for (int i = 0; i < arr.length; i++) {
            showStr(arr[i]);
        }

        PersionBean bean = new PersionBean("JAVA",0);
        showStr("返回对象:"+NativeMethods.persionFromJni(bean).toString()+"\n 原对象:"+bean.toString());

        showStr(NativeMethods.getStructInfo().toString());
    }


    private void showStr(String str){
//        showToast(str);
        Log.d("JNILOG","[JAVALOG] --> "+str);
    }

    @Override
    public String getSampleName() {
        return "JNISample";
    }
}
