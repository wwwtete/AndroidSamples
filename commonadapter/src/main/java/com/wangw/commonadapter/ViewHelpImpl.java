package com.wangw.commonadapter;

import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;

/**
 * Created by wangw on 2016/4/14.
 */
public class ViewHelpImpl implements ViewHelp {

    private View mRootView;
    private SparseArray<View> mViews;

    public ViewHelpImpl(View rootView){
        this.mRootView = rootView;
        mViews = new SparseArray<>();
    }

    public <E extends View> E getView(int id){
        View child = mViews.get(id);
        try {
            if(child == null){
                child = mRootView.findViewById(id);
                mViews.put(id,child);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return (E) child;
    }

    public View getRootView(){
        return mRootView;
    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @param text
     * @return
     */
    public ViewHelpImpl setText(int viewId, CharSequence text) {
        TextView view = getView(viewId);
        if(view != null)
            view.setText(text);
        return this;
    }

    /**
     * 为TextView设置字符串，颜色
     *
     * @param viewId
     * @param text
     * @return
     */
    public ViewHelpImpl setText(int viewId, CharSequence text, int colorRresId) {
        TextView view = getView(viewId);
        view.setTextColor(colorRresId);
        view.setText(text);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param drawableId
     * @return
     */
    public ViewHelpImpl setImageResource(int viewId, int drawableId) {
        ImageView view = getView(viewId);
        view.setImageResource(drawableId);

        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param
     * @return
     */
    public ViewHelpImpl setImageBitmap(int viewId, Bitmap bm) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bm);
        return this;
    }




    public ViewHelpImpl setVisible(int viewId, int visibility) {
        View view = getView(viewId);
        view.setVisibility(visibility);
        return this;
    }

}
