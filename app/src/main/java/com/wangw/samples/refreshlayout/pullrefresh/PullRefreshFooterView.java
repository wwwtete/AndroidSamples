package com.wangw.samples.refreshlayout.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.wangw.samples.R;


/**
 * 上拉加载时显示的View
 * Created by wangw on 2016/3/25.
 */
public class PullRefreshFooterView extends LinearLayout {

    private int mHeaderViewHeight;

    public PullRefreshFooterView(Context context) {
        super(context);
        initView();
    }

    public PullRefreshFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PullRefreshFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.refreshlayout_footer, this);
    }

    /**
     * 更新PullRefreshlayout的TopMargin
     * @param topMargin
     */
    public void updateRefreshLayoutTopMargin(int topMargin){

    }

    /**
     * 设置HeaderView的高度，用于判断FooterView是否完全显示出来了
     * @param headerViewHeight
     */
    public void setHeaderViewHeight(int headerViewHeight){
        this.mHeaderViewHeight = headerViewHeight;
    }

    /**
     * 开始执行加载数据的动画
     */
    public void onLoad() {

    }

    /**
     * 加载动画完成
     */
    public void loadCompleted() {

    }
}
