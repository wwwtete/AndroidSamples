package com.wangw.samples.refreshlayout.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.wangw.samples.R;
import com.wangw.samples.refreshlayout.HomeRefreshActivity;

/**
 * Created by wangw on 2016/5/12.
 */
@Deprecated
public class SuperRefreshLayout extends LinearLayout {

    private SwipeRefreshLayout mRefreshLayout;
    private View mFooterView;

    private HomeRefreshActivity mLoadListener;

    public SuperRefreshLayout(Context context) {
        super(context);
    }

    public SuperRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuperRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInitView();
    }

    private void onInitView() {
        setOrientation(VERTICAL);
        addRefreshLayout();
        addFooterView();
    }

    private void addFooterView() {
        mFooterView = inflate(getContext(), R.layout.refreshlayout_footer,this);
    }

    private void addRefreshLayout() {
        mRefreshLayout = new SwipeRefreshLayout(getContext());
        addView(mRefreshLayout);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        checkContentView();
    }

    private void checkContentView() {
        int count = getChildCount();
        if(count > 2 ){
            throw new IllegalArgumentException("不支持XML布局方式添加TargetView,请调用setTargetView方法设置需要刷新功能的View");
        }
    }

    public void setTargetView(int resId){

    }

    public void setTargetView(View targetView) {
        mRefreshLayout.addView(targetView);
    }

    public void setLoading(boolean b) {

    }

    public void onFinish() {
        mRefreshLayout.setRefreshing(false);
        setLoading(false);
    }



}
