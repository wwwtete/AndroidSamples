package com.wangw.samples.refreshlayout.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.wangw.samples.R;

/**
 * 扩展SwipeRefreshLayout，支持上拉加载功能，但这个版本只支持ListView或其子View
 * Created by wangw on 2016/5/12.
 */
public class PushLoadLayout extends SwipeRefreshLayout{

    private ListView mListView;
    private View mViewFooter;
    private int mFooterHeight;
    private OnLoadListener mListener;
    private int mDownY;
    private boolean mIsLoading;
    private boolean mLoadEnabled = false;
    private boolean mCanLoad = false;


    public PushLoadLayout(Context context) {
        super(context);
    }

    public PushLoadLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInitView();
    }

    private void onInitView() {
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        onCheckContentView();
        onAddFooterView();
    }

    private void onAddFooterView() {
        mViewFooter = LayoutInflater.from(getContext()).inflate(R.layout.refreshlayout_footer, null,false);
        FrameLayout layout = new FrameLayout(getContext());
        layout.addView(mViewFooter);
        mListView.addFooterView(layout);
        measureView(mViewFooter);
        mFooterHeight = mViewFooter.getMeasuredHeight();
        mViewFooter.setVisibility(GONE);
    }

    private void measureView(View view){
        LayoutParams params = view.getLayoutParams();
        if(params == null){
            params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        }
        int widhtSpac = getChildMeasureSpec(0,0,params.width);
        int height = params.height;
        int heightSpac;
        if(height > 0 ){
            heightSpac = MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY);
        }else {
            heightSpac = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        }
        view.measure(widhtSpac,heightSpac);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void onCheckContentView() {
        int count = getChildCount();
        if(count < 1){
            throw new IllegalArgumentException("PushLoadLayout必须包含一个子View");
        }
        for (int i=0;i<count;i++){
            View child = getChildAt(i);
            if(child instanceof ListView){
                mListView = (ListView) child;
                break;
            }
        }
        if (mListView == null){
            throw new IllegalArgumentException("PushLoadLayout上拉加载功能只支持ListView或其子类，如果只需要刷新功能请使用SwipeRefreshLayout");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(!isAllowanLoad() || !isChildScrollToBottom()){
            if(!isChildScrollToBottom())
                showFooter(false);
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int y = (int) ev.getRawY();
                int offset = y - mDownY;
                boolean flag = offset < 0 && Math.abs(offset) > mFooterHeight ;
                mCanLoad = flag;
                showFooter(flag);
                break;
            case MotionEvent.ACTION_UP:
                if(mCanLoad) {
                    onLoad();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isLoading())
            return false;
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 是否允许加载
     * @return
     */
    private boolean isAllowanLoad(){
        return isEnabled() &&  !isRefreshing() && !isLoading();
    }

    /**
     * 判断ListView是否滑动底部
     */
    private boolean isChildScrollToBottom(){
        ListAdapter adapter = mListView.getAdapter();
        if (adapter != null) {
            int lastPostion = mListView.getLastVisiblePosition();
            int count = adapter.getCount();
            return lastPostion > 0 && count > 0 && (lastPostion == count - 1 || lastPostion == count - 2);
        }
        return false;
    }


    public void setLoading(boolean flag){
        mIsLoading = flag;
        showFooter(flag);
    }

    private void showFooter(boolean flag){
        if(flag){
            mViewFooter.setVisibility(VISIBLE);
        }else {
            mViewFooter.setVisibility(GONE);
        }
    }

    public boolean isLoadEnabled() {
        return mLoadEnabled;
    }

    public void setLoadEnabled(boolean mLoadEnabled) {
        this.mLoadEnabled = mLoadEnabled;
    }

    public boolean isLoading(){
        return mIsLoading;
    }

    private void onLoad(){
        if(mListener != null)
            mListener.onLoad();
        setLoading(true);
        mCanLoad = false;
    }

    public void setOnLoadListener(OnLoadListener listener) {
        setLoadEnabled(listener != null);
        mListener = listener;
    }

    public void onFinish() {
        setRefreshing(false);
        setLoading(false);
    }

    public interface OnLoadListener{
        void onLoad();
    }

}
