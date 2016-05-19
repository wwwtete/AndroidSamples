package com.wangw.samples.refreshlayout.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * 下拉刷新，上拉加载的Layout
 * 支持子布局为AdapterView 或 ScrollView
 * Created by wangw on 2016/3/25.
 */
public class PullRefreshLayout extends LinearLayout {

    private static final int MOVE_UP_STATE = 0;     //向上移动
    private static final int MOVE_DOWN_STATE = 1;   //向下移动
    private static final int REFRESHING = 3;    //刷新中
    private static final int IDLE_STATE = -1;   //空闲中
    private static final int MOVEING = 4;       //滑动中


    private AdapterView<?> mAdapterView;
    private ScrollView mScrollView;

    private PullRefreshHeaderView mHeaderView;
    private PullRefreshFooterView mFooterView;

    private int mLastRawY;
    private int mMoveState;
    private int mPullSate;

    private int mHeaderViewHeight;
    private int mFooterViewHeight;

    //下拉刷新监听事件
    private OnRefreshListener mRefreshListener;
    //上拉加载监听事件
    private OnLoadListener mLoadListener;


    //上拉加载数据是否可用(默认不可用)
    private boolean mLoadDataEnable = false;


    public PullRefreshLayout(Context context) {
        super(context);
        initView();
    }

    public PullRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PullRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOrientation(LinearLayout.VERTICAL);
        addHeadView();
    }

    private void addHeadView() {
        mHeaderView = new PullRefreshHeaderView(getContext());
        measureView(mHeaderView);
        mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,mHeaderViewHeight);
        params.topMargin = -mHeaderViewHeight;
        addView(mHeaderView, params);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //当所有子组件都布局加载完成后添加Footer
        addFooterView();
        initConnetntView();
    }

    /**
     * 初始化ContentView
     */
    private void initConnetntView() {
        int count = getChildCount();
        if(count < 3){
            throw new IllegalArgumentException("PullRefreshLayout必须包含一个子View");
        }
        View view = null;
        for (int i=0;i<count-1;i++){
            view = getChildAt(i);
            if(view instanceof AdapterView<?>)
                mAdapterView = (AdapterView<?>) view;
            if(view instanceof ScrollView)
                mScrollView = (ScrollView) view;
        }
        if(mAdapterView == null && mScrollView == null){
            throw  new IllegalArgumentException("PullRefreshLayout中必须包含一个AdapterView 或 ScrollView");
        }
    }

    private void addFooterView() {
        mFooterView = new PullRefreshFooterView(getContext());
        mFooterView.setHeaderViewHeight(mHeaderViewHeight);
        measureView(mFooterView);
        mFooterViewHeight = mFooterView.getMeasuredHeight();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,mFooterViewHeight);
        addView(mFooterView, params);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int y = (int) ev.getRawY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                //先记录下down时的坐标
                mLastRawY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                //判断是项上滑动还是向下滑动
                int deltay = y - mLastRawY;
                if(checkIsRefreshScroll(deltay))
                    return true;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return false;//super.onInterceptTouchEvent(ev);
    }

    /**
     * 检查是否需要拦截Touch事件
     * @param deltay deltay > 0 向下 | deltay < 0 向上
     * @return
     */
    private boolean checkIsRefreshScroll(int deltay) {
        boolean flag = checkRefreshViewScroll(deltay);
        if(mPullSate == REFRESHING || mPullSate == REFRESHING){
            //TODO 如果正在执行刷新中，用户想触发关闭刷新动画则加上下面的判断条件，如果不想让用户触发关闭动画则删除此判断条件
            if(mPullSate == REFRESHING && Math.abs(getHeaderViewTopMargin()) < mHeaderViewHeight && !flag){
                return true;
            }
            return false;
        }
        if(flag){
            if(deltay > 0)
                mMoveState = MOVE_DOWN_STATE;
            else {
                mMoveState = MOVE_UP_STATE;
            }
        }
        return flag;
    }

    /**
     * 检查判断是否该允许父控件滑动了
     * @param deltay
     * @return
     */
    private boolean checkRefreshViewScroll(int deltay) {
        //AdapterView不为空的情况
        if(mAdapterView != null){
            if(deltay > 0){
                View child = mAdapterView.getChildAt(0);
                //如果AdapterView中没有子view，则拦截事件进行刷新操作
                if(child == null){
                    return true;
                }
                if(mAdapterView.getFirstVisiblePosition() == 0 && ( child.getTop() == 0 || Math.abs(child.getTop() - mAdapterView.getPaddingTop()) <= 8)){
                    return true;
                }
            }else if(deltay < 0){
                View lastView = mAdapterView.getChildAt(mAdapterView.getChildCount() - 1);
                //如果AdapterView中没有Vie则不进行上拉加载
                if(lastView == null)
                    return false;
                if(mAdapterView.getLastVisiblePosition() == mAdapterView.getCount() - 1 && lastView.getBottom() <= getHeight())
                    return true;
            }
        }

        //ScrollView不为空情况
        if(mScrollView != null){
            //判断ScrollView的子View距离顶端距离
            View child = mScrollView.getChildAt(0);
            if(child == null)
                return false;
            if(deltay > 0 && mScrollView.getScrollY() == 0)
                return true;
            else if(deltay < 0 && child.getMeasuredHeight() <= getHeight() + mScrollView.getScrollY())
                return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getRawY();
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                int offsetY =y - mLastRawY;
                //执行下拉操作
                if(mMoveState == MOVE_DOWN_STATE){
                    if(mPullSate == REFRESHING)
                        setHeaderViewTopMarginOffset(offsetY);
                    else
                        moveHeaderView(offsetY);
                }else if(mLoadDataEnable && mMoveState == MOVE_UP_STATE) {  //执行上拉操作
                    if(mFooterView.getVisibility() == View.GONE){
                        mFooterView.setVisibility(VISIBLE);
                    }
                    moveFooterView(offsetY);
                }
                mLastRawY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(mPullSate == REFRESHING)
                    break;

                int topMargin = getHeaderViewTopMargin();
                if(mMoveState == MOVE_DOWN_STATE){
                    if(topMargin >= 0){
                        //执行刷新操作
                        onRefresh();
                    }else {
                        //没有完全显示出来则重新隐藏
                        setHeaderViewTopMargin(-mHeaderViewHeight);
                    }
                }else if(mMoveState == MOVE_UP_STATE && mLoadDataEnable) {
                    if(Math.abs(topMargin) >= mHeaderViewHeight+mFooterViewHeight)
                        onLoad();
                    else {
                        setHeaderViewTopMargin(-mHeaderViewHeight);
                        mFooterView.setVisibility(GONE);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 开始执行刷新操作
     */
    public void onRefresh() {
        mPullSate = REFRESHING;
        setHeaderViewTopMargin(0);
        mHeaderView.onRefresh();
        if(mRefreshListener != null){
            mRefreshListener.onRefresh(this);
        }
    }

    /**
     * 刷新操作完成
     */
    public void onRefreshCompleted(){
        mMoveState = mPullSate = IDLE_STATE;
        setHeaderViewTopMargin(-mHeaderViewHeight);
        mHeaderView.refreshCompleted();
    }

    /**
     * laod操作完成
     */
    public void onLoadCompleted(){
        mMoveState =  mPullSate = IDLE_STATE;
        setHeaderViewTopMargin(-mHeaderViewHeight);
        mFooterView.loadCompleted();
    }

    /**
     * 在不清楚调用那种操作完成的情况下，直接调用这个方法即可
     */
    public void onCompleted(){
        onRefreshCompleted();
        onLoadCompleted();
    }

    /**
     * 开始执行Load操作
     */
    public void onLoad(){
        mPullSate = REFRESHING;
        setHeaderViewTopMargin(-(mHeaderViewHeight + mFooterViewHeight));
        mFooterView.onLoad();
        if(mLoadListener != null){
            mLoadListener.onLoad(this);
        }
    }


    /**
     * 手指按下并且滑动中移动FooterView
     * @param offsetY
     */
    private void moveFooterView(int offsetY) {
        int newTopMargin = changeHeaderViewTopMarginOffset(offsetY);
        mPullSate = MOVEING;
        //如果HeaderView TopMargin的绝对值大于或等于(HeaderViewHeight + FooterViewHeight)的高度
        //则说明FooterView完全显示了出来，修改FooterView的提示状态
        mFooterView.updateRefreshLayoutTopMargin(newTopMargin);
    }

    /**
     * 手指按下并且滑动中移动HeaderView的TopMargin操作
     * @param offsetY
     */
    private void moveHeaderView(int offsetY) {
        int newTopmargin = changeHeaderViewTopMarginOffset(offsetY);
        mPullSate = MOVEING;
        //如果headerView的topMaring >= 0 则证明HeaderView已经完全显露了出来，则需要更新Headerview的提示状态
        mHeaderView.updateRefreshLayoutTopMargin(newTopmargin);
    }

    /**
     * 修改HeaderView的TopMargin的偏移量
     * @param offsetY topmargin的偏移量
     * @return
     */
    private int changeHeaderViewTopMarginOffset(int offsetY){
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        int newTopMargin = (int) (params.topMargin + offsetY * 0.4f);
        //需要对上拉过程和下拉过程中做一下限制，避免出现两种动画同时执行的bug
        //如果先是上拉一段距离后，然后再下拉，则不不设置TopMarging，直接返回原来的TopMargin，避免触发下拉刷新动画
        if(offsetY > 0 && mMoveState == MOVE_UP_STATE && Math.abs(params.topMargin) <= mHeaderViewHeight)
            return params.topMargin;
        //如果先是下拉一段距离后，又上拉，则不设置TopMargin，直接返回原来的TopMargin，避免触发上拉加载动画
        if(offsetY < 0 && mMoveState == MOVE_DOWN_STATE && Math.abs(params.topMargin) >= mHeaderViewHeight)
            return params.topMargin;
        setHeaderViewTopMargin(newTopMargin);
        return newTopMargin;
    }

    /**
     * 测量计算View宽高
     * @param view
     */
    private void measureView(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if(params == null)
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int widthSpec = ViewGroup.getChildMeasureSpec(0,0,params.width);
        int viewHeight = params.height;
        int viewHeightSpec;
        if(viewHeight > 0){
            viewHeightSpec = MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY);
        }else {
            viewHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(widthSpec, viewHeightSpec);
    }



    /**
     * 上拉加载数据功能是否可用
     * @param enable
     */
    public void setLoadDataEnable(boolean enable){
        this.mLoadDataEnable = enable;
    }


    /**
     * 设置下拉刷新监听事件
     * @param listener
     */
    public void setOnRefreshListener(OnRefreshListener listener){
        this.mRefreshListener = listener;
    }

    /**
     * 设置上拉加载事件监听
     * @param listener
     */
    public void setOnLoadListener(OnLoadListener listener){
        this.mLoadListener = listener;
        mLoadDataEnable = listener != null;
    }

    /**
     * 获取HeaderView的TopMargin
     * @return
     */
    private int getHeaderViewTopMargin() {
        return ((LayoutParams)mHeaderView.getLayoutParams()).topMargin;
    }

    /**
     * 在原来的TopMargin的基础上进行设置HeaderView距离顶部的距离，
     * @param offsetTopMargin 这个值应该是偏差值，并不是最终值
     */
    public int setHeaderViewTopMarginOffset(int offsetTopMargin) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        int newTopMargin = offsetTopMargin + params.topMargin;
        if(mMoveState == MOVE_DOWN_STATE) {
            if (newTopMargin > 0) {
                newTopMargin = 0;
            } else if (Math.abs(newTopMargin) > mHeaderViewHeight) {
                newTopMargin = -mHeaderViewHeight;
            }
        }else {
            if(newTopMargin > 0){
                newTopMargin = 0;
            }else if(Math.abs(newTopMargin) > mFooterViewHeight){
                newTopMargin = -mFooterViewHeight;
            }
        }
        setHeaderViewTopMargin(newTopMargin);
        return newTopMargin;
    }


    public void setHeaderViewTopMargin(int topMargin){
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        params.topMargin = topMargin;
        mHeaderView.setLayoutParams(params);
        requestLayout();

    }

    public interface OnRefreshListener{
        void onRefresh(PullRefreshLayout refreshLayout);
    }

    public interface OnLoadListener{
        void onLoad(PullRefreshLayout refreshLayout);
    }

}
