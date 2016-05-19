package com.wangw.commonadapter;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by wangw on 2016/4/14.
 */
public interface ViewHelp {

    <E extends View> E getView(int id);

    ViewHelp setText(int viewId, CharSequence text);

    ViewHelp setText(int viewId, CharSequence text, int colorRresId);

    ViewHelp setImageResource(int viewId, int drawableId);

    ViewHelp setImageBitmap(int viewId, Bitmap bm);

    ViewHelp setVisible(int viewId, int visibility);

}
