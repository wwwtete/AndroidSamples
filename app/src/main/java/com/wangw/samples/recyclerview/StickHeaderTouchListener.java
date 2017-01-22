package com.wangw.samples.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by wangw on 2016/12/15.
 */

public class StickHeaderTouchListener implements RecyclerView.OnItemTouchListener {

    private MyItemDecoration mDecoration;
    private GestureDetector mDetector;
    private RecyclerView mRecyclerView;
    private OnHeaderClickListener mClickListener;

    public StickHeaderTouchListener(RecyclerView recyclerView,MyItemDecoration decoration){
        mDecoration = decoration;
        mRecyclerView = recyclerView;
        mDetector = new GestureDetector(mRecyclerView.getContext(),new SingleTapDetector());
    }

    public OnHeaderClickListener getClickListener() {
        return mClickListener;
    }

    public void setClickListener(OnHeaderClickListener clickListener) {
        mClickListener = clickListener;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if(mClickListener != null){
            boolean tapDetectorResponse = mDetector.onTouchEvent(e);
            if(tapDetectorResponse){
                return true;
            }
            if(e.getAction() == MotionEvent.ACTION_DOWN){
                return mDecoration.isClickHeader((int) e.getX(),(int) e.getY());
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }


    private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            boolean flag =  mDecoration.isClickHeader((int) e.getX(), (int) e.getY());
            if(flag && mClickListener != null){
                mClickListener.onHeaderClick();
            }
            return flag;
//            if (position != -1) {
//                View headerView = mDecor.getHeaderView(mRecyclerView, position);
//                long headerId = getAdapter().getHeaderId(position);
//                mOnHeaderClickListener.onHeaderClick(headerView, position, headerId);
//                mRecyclerView.playSoundEffect(SoundEffectConstants.CLICK);
//                headerView.onTouchEvent(e);
//                return true;
//            }
//            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    }


    public interface OnHeaderClickListener {
        void onHeaderClick();
    }
}
