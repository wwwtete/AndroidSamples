package com.wangw.samples.recyclerview.stickyheader;

import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by wangw on 2017/2/20.
 */

public abstract class StickyHeaderClickListener implements RecyclerView.OnItemTouchListener {

    private GestureDetector mDetector;
    private RecyclerView mRecyclerView;
    private StickyHeaderDecoration mHeaderDecoration;

    public StickyHeaderClickListener(RecyclerView recyclerView, StickyHeaderDecoration headerDecoration) {
        mRecyclerView = recyclerView;
        mHeaderDecoration = headerDecoration;
        mDetector = new GestureDetector(mRecyclerView.getContext(),new SingleTapDetector());
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        boolean detectorResponse = mDetector.onTouchEvent(e);
        if (detectorResponse)
            return true;
        if (e.getAction() == MotionEvent.ACTION_DOWN){
           return mHeaderDecoration.findHeaderPostionUnder(rv,(int)e.getX(),(int) e.getY()) != -1;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    private View getHeaderView(float x,float y,int position){
        return mHeaderDecoration.getHeaderView(mRecyclerView.findChildViewUnder(x,y),mRecyclerView,position);
    }

    public abstract void onClickHeader(int position);

    private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int position = mHeaderDecoration.findHeaderPostionUnder(mRecyclerView,(int)e.getX(),(int)e.getY());
            if (position != -1){
                onClickHeader(position);
                return true;
            }
            return false;
        }
    }

}
