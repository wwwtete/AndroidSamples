package com.wangw.samples.recyclerview.stickyheader;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wangw on 2017/2/18.
 */

public class DimensionUtils {

    public static void initMargins(Rect margins, View view){
        ViewGroup.LayoutParams params = view.getLayoutParams();

        if (params instanceof ViewGroup.MarginLayoutParams){
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
            initMarginRect(margins,marginLayoutParams);
        } else {
            margins.set(0,0,0,0);
        }
    }

    public static void initMarginRect(Rect marginRect, ViewGroup.MarginLayoutParams marginLayoutParams) {
        marginRect.set(
                marginLayoutParams.leftMargin,
                marginLayoutParams.topMargin,
                marginLayoutParams.rightMargin,
                marginLayoutParams.bottomMargin
        );
    }
}
