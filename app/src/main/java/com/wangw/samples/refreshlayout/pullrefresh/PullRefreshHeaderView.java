package com.wangw.samples.refreshlayout.pullrefresh;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.wangw.samples.R;


/**
 * 下拉刷新时的动画View
 * Created by wangw on 2016/3/25.
 */
public class PullRefreshHeaderView extends RelativeLayout {


    private AnimationDrawable mAnim;

    public PullRefreshHeaderView(Context context) {
        super(context);
        initView();
    }

    public PullRefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PullRefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {
        inflate(getContext(), R.layout.view_refresh_header, this);
    }

    /**
     * 更新PullRefreshlayout的TopMargin
     * @param topMargin
     */
    public void updateRefreshLayoutTopMargin(int topMargin){

    }

    /**
     * 执行刷新动画
     */
    public void onRefresh(){

    }

    /**
     * 刷新动画完毕
     */
    public void refreshCompleted() {

    }
}
