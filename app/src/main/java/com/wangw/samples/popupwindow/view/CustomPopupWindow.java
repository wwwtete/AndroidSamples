package com.wangw.samples.popupwindow.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.wangw.samples.R;

/**
 * Created by wangw on 2016/6/30.
 */
public class CustomPopupWindow {

    private Context mContext;
    private PopupWindow mPopupWindow;

    public CustomPopupWindow(Context context){
        mContext = context;
    }

    public void show(View parentView){
        initPopupWindow();
        mPopupWindow.showAsDropDown(parentView);
//        mPopupWindow.showAtLocation(parentView, Gravity.CENTER,0,0);
    }

    private void initPopupWindow() {
        mPopupWindow = new PopupWindow(mContext);
        mPopupWindow.setContentView(LayoutInflater.from(mContext).inflate(R.layout.view_popup,null));
        mPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(500);//(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopupWindow.setFocusable(true);

        ColorDrawable drawable = new ColorDrawable(0x50FF0000);
        mPopupWindow.setBackgroundDrawable(drawable);
        mPopupWindow.setTouchable(false);

        mPopupWindow.setAnimationStyle(R.style.popup_anim);

    }

    public boolean isShowing(){
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    public void dismiss(){
        if(isShowing())
            mPopupWindow.dismiss();
    }


}
