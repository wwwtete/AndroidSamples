package com.wangw.samples.flowlayout.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 流式布局：自动换行布局
 * Created by wangw on 2016/8/15.
 */
public class FlowLayout extends ViewGroup {

    private List<List<View>> mAllRows = new ArrayList<>();
    private List<Integer> mRowHeight = new ArrayList<>();

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //算出父控件传入的宽高和测量模式
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        //如果当前ViewGroup的宽高是wrap_content的情况
        int width = 0;
        int height = 0;
        //记录每一行的宽高
        int lineWidth =0;
        int lineHeight = 0;

        //遍历子View
        int childCount = getChildCount();
        for (int i =0;i<childCount;i++){
            View child = getChildAt(i);
            //测量子View的宽高
            measureChild(child,widthMeasureSpec,heightMeasureSpec);
            //得到子View的LayoutParams;
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            //子View的宽高
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            //换行情况
            if(childWidth + lineWidth > sizeWidth){
                //得到最大的宽度
                width = Math.max(width,lineWidth);
                //重置LineWidth
                lineWidth = childWidth;
                //记录行总高
                height += lineHeight;
                lineHeight = childHeight;
            }else { //不需要换行
                //叠加宽
                lineWidth += childWidth;
                //记录行最高值
                lineHeight = Math.max(lineHeight,childHeight);
            }
            //处理最后一个子View
            if(i == childCount-1){
                width = Math.max(width,lineWidth);
                height += lineHeight;
            }
        }

        //真正设置宽高
        setMeasuredDimension( modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width,
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        mAllRows.clear();
        mRowHeight.clear();
        //获取View宽
        int width = getWidth();

        //每行的宽高
        int lineWidth = 0;
        int rowHeight = 0;

        int childCount = getChildCount();
        List<View> rowViews = new ArrayList<>();
        for (int i=0;i<childCount;i++){
            View view = getChildAt(i);
            int childWidth = view.getMeasuredWidth();
            int childHeight = view.getMeasuredHeight();
            MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
            if(lineWidth + childWidth + lp.leftMargin+lp.rightMargin > width){
                //记录行高
                mRowHeight.add(rowHeight);
                //记录当前行
                mAllRows.add(rowViews);
                //重置行高
                rowHeight = childHeight + lp.topMargin + lp.bottomMargin;
                lineWidth = 0;
                //重置LinViews集合
                rowViews = new ArrayList<>();
            }
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            rowHeight = Math.max(rowHeight,childHeight+lp.topMargin+lp.bottomMargin);
            rowViews.add(view);
        }
        //处理最后一行
        mRowHeight.add(rowHeight);
        mAllRows.add(rowViews);

        //设置子View位置
        int left =0;
        int top = 0;
        int rowCount = mAllRows.size();
        for (int i = 0;i<rowCount;i++){
            rowViews = mAllRows.get(i);
            rowHeight = mRowHeight.get(i);
            for (View view:rowViews){
                if(view.getVisibility() == GONE){
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
                int cLeft = left + lp.leftMargin;
                int cRight = cLeft + view.getMeasuredWidth();
                int cTop = top + lp.topMargin;
                int cBottom = cTop + view.getMeasuredHeight();
                view.layout(cLeft,cTop,cRight,cBottom);
                left += view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            }
            left = 0;
            top += rowHeight;
        }
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    }
}
