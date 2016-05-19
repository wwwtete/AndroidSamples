package com.wangw.samples.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by wangw on 2016/4/14.
 */
public class SnackUtils {



    public static void showSnack(CharSequence title,View.OnClickListener onClickListener,CharSequence msg,View view){
        Snackbar.make(view,msg,Snackbar.LENGTH_SHORT)
                .setAction(title,onClickListener)
                .show();
    }

}
