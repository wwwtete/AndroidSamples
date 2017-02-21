package com.wangw.samples.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wangw.samples.R;

/**
 * Created by wangw on 2016/12/14.
 */
class MyItemDecoration extends RecyclerView.ItemDecoration {

    //我们通过获取系统属性中的listDivider来添加，在系统中的AppTheme中设置
    public static final int[] ATRRS  = new int[]{
            android.R.attr.listDivider
    };
    private LinearGradient mLinearGradient =
            new LinearGradient(0, 10, 0, 100, new int[] { Color.RED, 0 }, null,
                    Shader.TileMode.CLAMP);
    private LinearGradient mLinearGradient2 =
            new LinearGradient(0, 40, 0, 100, new int[] { Color.BLACK, 0 }, null,
                    Shader.TileMode.CLAMP);
    private final Context mContext;
    private final Drawable mDivider;
    private Paint mPaint;


    public MyItemDecoration(Context context) {
        this.mContext = context;
        final TypedArray ta = context.obtainStyledAttributes(ATRRS);
        this.mDivider = ta.getDrawable(0);
        mPaint = new Paint();
        ta.recycle();
    }

    /**
     * 在 onDraw 为 divider 设置绘制范围，并绘制到 canvas 上，
     * 而这个绘制范围可以超出在 getItemOffsets 中设置的范围，
     * 但由于 decoration 是绘制在 child view 的底下，所以并不可见，
     * 但是会存在重叠（overdraw）
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int recyclerTop = parent.getPaddingTop();
        final int recyclerBottom = parent.getHeight() - parent.getPaddingBottom();
        int size = parent.getChildCount();
        //测试1
        for (int i=0;i<size;i++){
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = Math.max(recyclerTop,child.getBottom() + params.bottomMargin);
            final int bottom = Math.min(recyclerBottom,top+25);
            mDivider.setBounds(left,top,right,bottom);
            mDivider.draw(c);
        }

        //测试2
//        for (int i=0;i<size;i++){
//            final View child = parent.getChildAt(i);
//            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
//            final int top = Math.max(recyclerTop,child.getBottom() + params.bottomMargin - 25);
//            final int bottom = Math.min(recyclerBottom,top+50);
//            mDivider.setBounds(left,top,right,bottom);
//            mDivider.draw(c);
//        }

        //测试3
//        for (int i=0;i<size;i++){
//            final View child = parent.getChildAt(i);
//            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
//            final int top = Math.max(recyclerTop,child.getBottom() + params.bottomMargin - 50);
//            final int bottom = Math.min(recyclerBottom,top+100);
//            mDivider.setBounds(left-100,top,right-100,bottom);
//            mDivider.draw(c);
//        }
        //测试4
//        for (int i=0;i<size-1;i++){
//            final View child = parent.getChildAt(i);
//            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
//            final int top = Math.max(recyclerTop,child.getBottom() + params.bottomMargin - 50);
//            final int bottom = Math.min(recyclerBottom,top+100);
//            mDivider.setBounds(left-100,top,right-100,bottom);
//            mDivider.draw(c);
//        }
    }

    /**
     * getItemOffsets 中为 outRect 设置的4个方向的值，
     * 将被计算进所有 decoration 的尺寸中，而这个尺寸，
     * 被计入了 RecyclerView 每个 item view 的 padding 中。
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//                super.getItemOffsets(outRect, view, parent, state);
        outRect.top = outRect.left = outRect.right = outRect.bottom = 50;
    }


    String TAG ="TEST";
    Rect mRect = new Rect();
    /**
     * 需要注意：decoration 的 onDraw，
     * child view 的 onDraw，decoration 的 onDrawOver，这三者是依次发生的。
     * 而由于 onDrawOver 是绘制在最上层的，
     * 所以它的绘制位置并不受限制（当然，decoration 的 onDraw 绘制范围也不受限制，只不过不可见）
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
//        mPaint.setShader(mLinearGradient);
        int count = parent.getChildCount();
        if(count <= 0)
            return;
        View child1 = parent.getChildAt(0);
//        View header = getHeader(parent,0);;
        int y =0;
        if(count >= 2){
            View child2 = parent.getChildAt(1);
//            View header2 = getHeader(parent,1);
            y =child2.getTop() - child1.getHeight() ;
            Log.d(TAG, "onDrawOver: y="+y);
            if(y > getListTop(parent))
                y = Math.min(Math.max(getListTop(parent),child1.getTop()),y);
            drawHeader(c, child1, child1.getLeft(),y);
            drawHeader(c,child2,child2.getLeft(),child2.getTop());
        }else {
            y = child1.getTop();
            drawHeader(c, child1, child1.getLeft(),y);
        }
        mRect.set(child1.getLeft(),y,child1.getRight(),child1.getBottom());
        //添加一个蒙层
    }

    public boolean isClickHeader(int x,int y){
        return mRect.contains(x,y);
    }

    private int getListTop(RecyclerView view) {
        if (view.getLayoutManager().getClipToPadding()) {
            return view.getPaddingTop();
        } else {
            return 0;
        }
    }

    private void drawHeader(Canvas c, View header,int x,int y) {
        c.save();
        c.translate(x,y);
        header.draw(c);
        c.restore();
    }

    public View getHeader(View parent,int position) {

        View header = LayoutInflater.from(mContext).inflate(R.layout.view_header,null);
        ((TextView)header.findViewById(R.id.txt)).setText("postion = "+position);
        header.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
       int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);
        int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.getPaddingLeft() + parent.getPaddingRight(), header.getLayoutParams().width);
        int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.getPaddingTop() + parent.getPaddingBottom(), header.getLayoutParams().height);
        header.measure(childWidth, childHeight);
        header.layout(0, 0, parent.getWidth(), header.getMeasuredHeight());
        return header;
    }


}
