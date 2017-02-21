package com.wangw.samples.refreshlayout.view;

/**
 * Created by Administrator on 2015/6/25.
 */

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.exlogcat.L;
import com.wangw.samples.R;

/**
 * 继承自SwipeRefreshLayout,从而实现滑动到底部时上拉加载更多的功能.
 *
 * Created by wangw on 2016/5/10.
 */
@Deprecated
public class RefreshLayout_old extends SwipeRefreshLayout {

    private int mTouchSlop;
    /**
     * listview实例
     */
    private ListView mListView;

    /**
     * 上拉监听器, 到了最底部的上拉加载操作
     */
    private OnLoadListener mOnLoadListener;

    /**
     * ListView的加载中footer
     */
    private View mViewFooter;

    private int mLastRawY;
    /**
     * 是否在加载中 ( 上拉加载更多 )
     */
    private boolean mIsLoading = false;



    private int mFooterHeight;

    /***
     * 上拉加载是否可用
     */
    private boolean mLoadEnabled;

    private int mTargetViewHeight;

    private int mOffset = 0;
    /**
     * @param context
     */
    public RefreshLayout_old(Context context) {
        this(context, null);
        onInitView();
    }

    public RefreshLayout_old(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInitView();
    }

    private void onInitView() {
        //TODO 初始化工作
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        setColorSchemeColors(Color.GREEN,Color.BLUE);
//        addFooterView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addFooterView();
        initContentView();
    }

    private void initContentView() {
        int count = getChildCount();
        if(count < 3){
            throw new IllegalArgumentException("Refreshlayout必须包含一个子View");
        }
        for (int i=0;i<count;i++){
            View child = getChildAt(i);
            if(child instanceof AbsListView){
                mListView = (ListView) child;
                break;
            }
        }

        if(mListView == null)
            throw new IllegalArgumentException("RefreshLayout必须包含一个ListView");
    }

    private void addFooterView() {
//        mViewFooter = new ImageView(getContext());
//        ((ImageView)mViewFooter).setImageResource(R.drawable.if_ic_launcher);
//        ((ImageView)mViewFooter).setAdjustViewBounds(true);
//        ((ImageView)mViewFooter).setScaleType(ImageView.ScaleType.FIT_XY);

        mViewFooter = LayoutInflater.from(getContext()).inflate(R.layout.refreshlayout_footer, null,false);
        LayoutParams params = generateDefaultLayoutParams();
//        params.height = 200;
        addView(mViewFooter,params);
        measureView(mViewFooter);
        mFooterHeight = mViewFooter.getMeasuredHeight();
        mViewFooter.setVisibility(GONE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mListView == null || mViewFooter == null)
            return;
        final int height = getMeasuredHeight();
        final int width = getMeasuredWidth();
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
//        mListView.layout(childLeft, childTop, childLeft + childWidth, childTop + mListView.getMeasuredHeight());
        mListView.layout(mListView.getLeft(),mListView.getTop()+mOffset,mListView.getRight(),childHeight);
        L.d("[onLayout] offset = %s | 真实h = %s | 匹配h = %s",mOffset,mTargetViewHeight,mListView.getMeasuredHeight());
        mViewFooter.layout(childLeft,mTargetViewHeight+mOffset,childLeft+childWidth,mTargetViewHeight+mFooterHeight+mOffset);

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //重新测量ListView的高度，默认是Matchparent，必须改成WRAP_CONTENT
        mListView.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), getChildMeasureSpec(MeasureSpec.makeMeasureSpec(getMeasuredHeight(),MeasureSpec.EXACTLY),0,LayoutParams.WRAP_CONTENT));
        mTargetViewHeight = mListView.getMeasuredHeight();
        L.d("[onMeasure] 真实h = %s",mListView.getMeasuredHeight());
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        L.d("[onMeasure] 匹配h = %s",mListView.getMeasuredHeight());
        mViewFooter.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mFooterHeight, MeasureSpec.EXACTLY));
    }

    private void measureView(View view){
        LayoutParams params = view.getLayoutParams();
        if(params == null)
            params = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        int widthSpac = getChildMeasureSpec(0,0,params.width);
        int height = params.height;
        int heightSpac;
        if(height > 0){
            heightSpac = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }else {
            heightSpac = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(widthSpac,heightSpac);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean flag =super.onInterceptTouchEvent(ev);
        int y = (int) ev.getRawY();
//        L.d("[onInterceptTouchEvent] action = %s | y = %s",ev.getAction(),y);
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastRawY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int tempY = y - mLastRawY;
                if(!flag && !mIsLoading && tempY < 0){ //上
                    flag = isChildScrollToBottom();
                }
                break;
        }
        L.d("[onInterceptTouchEvent]>>>> flag = %s | action=%s",flag,ev.getAction());
        return flag;
    }

    /**
     * 判断targetView是否滑动到顶部，是否还可以继续欢动
     * @return true:已经滚动到顶部，false：没有滚动到顶部
     */
    private boolean isChildScrollToTop(){
        return !canChildScrollUp();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isRefreshing() || mIsLoading)
            return false;
        boolean flag;
        int y = (int) ev.getRawY();
        int offsetY = y - mLastRawY;
        if (offsetY > 0)
            flag = super.onTouchEvent(ev);
        else
            flag = canChildScrollDown();
        L.d("[onTouchEvent]>>>> flag = %s",flag);
        if(!flag) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                        moveFooterView(offsetY);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (Math.abs(mListView.getTop()) >= mFooterHeight) {
                        onLoadData();
                    } else {
                        setmIsLoading(false);
                    }
                    break;
            }
        }
        return flag;
    }

    /**
     * 判断targetView是否滑动到最底部，是否还可以继续滑动
     * @return
     */
    private boolean isChildScrollToBottom(){
        return !canChildScrollDown();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private void moveFooterView(int offsetY) {
        if(mViewFooter.getVisibility() == GONE){
            mViewFooter.setVisibility(VISIBLE);
        }
        int offsetD = (int) (offsetY*0.09f);
        setFooterViewBottom(offsetD);
//        int top = mListView.getTop();
//        int tempd = offsetD+top;
//        if(Math.abs(tempd) > mFooterHeight){
//            setFooterViewBottom(-(mFooterHeight - Math.abs(top)));
//        }else {
//            setFooterViewBottom(offsetD);
//        }
    }

    private void resetView(){
        int top = mListView.getTop();
        mViewFooter.setVisibility(GONE);
        mViewFooter.setAlpha(1.0f);
        if(top < 0){
            setFooterViewBottom(Math.abs(top));
        }
        mOffset = 0;
    }

    private void setFooterViewBottom(int offset){
        L.d("[setFooterViewBottom] offset = %s",offset);
        int top = mListView.getTop();
        int tempd = offset+top;
        if(offset > 0){
            offset = tempd > 0 ? Math.abs(top) : offset;
        }else {
                offset =Math.abs(tempd) > mFooterHeight ?  -(mFooterHeight - Math.abs(top)) : offset;
        }
        mOffset += offset;
        requestLayout();
//        mListView.offsetTopAndBottom(offset);
//        mViewFooter.layout(mViewFooter.getLeft(),mViewFooter.getTop()+offset, mViewFooter.getRight(),mViewFooter.getBottom()+offset+mFooterHeight);
    }

    private boolean canChildScrollDown() {
        if(Build.VERSION.SDK_INT < 14){
            ListAdapter adapter = mListView.getAdapter();
            if(adapter != null) {
                int count = adapter.getCount() - 1;
                return mListView.getLastVisiblePosition() == count;
            }else {
                return true;
            }
        }else {
            return ViewCompat.canScrollVertically(mListView,0);
        }
    }

    /**
     * 如果到了最底部,而且是上拉操作.那么执行onLoad方法
     */
    private void onLoadData() {
        if (mOnLoadListener != null) {
            // 设置状态
            setmIsLoading(true);
            //
            mOnLoadListener.onLoad();
        }
    }

    /**
     * @param isLoading
     */
    public void setmIsLoading(boolean isLoading) {
        this.mIsLoading = isLoading;
        if (this.mIsLoading) {
            setFooterViewBottom(-mFooterHeight);
        } else {
//            mListView.layout(mListView.getLeft(),0,mListView.getRight(),mListView.getBottom());
            int top = mListView.getTop();
            if(top < 0){
                setFooterViewBottom(Math.abs(top));
            }
            hidenFooterByAnim();
//            resetView();
        }
    }

    private Handler mHandler = new Handler();
    private void hidenFooterByAnim(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//               if(mListView.getTop() < 0){
//                   hidenFooterByAnim();
//                   setFooterViewBottom(30);
//               }else {
//                   resetView();
//               }

                if(mViewFooter.getAlpha() > 0){
                    hidenFooterByAnim();
                    mViewFooter.setAlpha(mViewFooter.getAlpha()-0.09f);
                }else {
                    resetView();
                }

            }
        },100);
    }


    /**
     * 设置动画完成
     */
    public void onFinish(){
        setmIsLoading(false);
        setRefreshing(false);
    }

    /**
     * @param loadListener
     */
    public void setOnLoadListener(OnLoadListener loadListener) {
        mLoadEnabled = loadListener != null;
        mOnLoadListener = loadListener;
    }

    /**
     * 加载更多的监听器
     *
     * @author mrsimple
     */
    public interface OnLoadListener {
        void onLoad();
    }


//    @Override
//    public boolean canChildScrollUp() {
//        if (mListView != null && mListView instanceof AbsListView) {
//            final AbsListView absListView = (AbsListView) mListView;
//            return absListView.getChildCount() > 0
//                    && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
//                    .getTop() < absListView.getPaddingTop());
//        }
//        return super.canChildScrollUp();
//    }
}
