package com.wangw.samples.popupwindow;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.popupwindow.view.CustomPopupWindow;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 *
 */
public class DefaultPopupWindowSample extends BaseActivity {

    @Bind(R.id.btn_show)
    Button mBtnShow;
    @Bind(R.id.activity_default_popup_window_sample)
    View mActivityDefaultPopupWindowSample;
    private CustomPopupWindow mPopupWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_popup_window_sample);
        mPopupWindow = new CustomPopupWindow(this);
    }

    @OnClick(R.id.btn_show)
    void onClikc() {
        mPopupWindow.show(mBtnShow);//(mActivityDefaultPopupWindowSample);
    }

    @OnClick(R.id.btn_dismiss)
    void onDismiss(){
        mPopupWindow.dismiss();
    }


    @Override
    public String getSampleName() {
        return "PopupWindow默认的使用方式";
    }
}
