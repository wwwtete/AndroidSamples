package com.wangw.samples.refreshlayout.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.exlogcat.L;
import com.wangw.samples.R;
import com.wangw.samples.refreshlayout.RefreshSamplesActivity;

/**
 * Created by wangw on 2016/5/10.
 */
@Deprecated
public class RefreshLayout_old2 extends LinearLayout {

    private SwipeRefreshLayout mLayout;

    public RefreshLayout_old2(Context context) {
        super(context);
        onInitView();
    }

    public RefreshLayout_old2(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInitView();
    }

    public RefreshLayout_old2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInitView();
    }

    private void onInitView() {
        setOrientation(VERTICAL);
//        mLayout =new SwipeRefreshLayout(getContext());
//        addView(mLayout);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ImageView img = new ImageView(getContext());
        img.setImageResource(R.drawable.ic_launcher);
        img.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(img);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        ListView mListView = (ListView) getChildAt(0);
        L.d("[onMeasure] 之前h = %s",mListView.getMeasuredHeight());
        measureChild(mListView,widthMode,heightMode);
        measureView(mListView);
        L.d("[onMeasure] 之后h = %s",mListView.getMeasuredHeight());
    }

    private void measureView(View view){
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if(params == null)
            params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void setOnLoadListener(RefreshSamplesActivity refreshActivity) {

    }

    public void setOnRefreshListener(RefreshSamplesActivity refreshActivity) {

    }

    public void onFinish() {

    }

    public interface OnLoadListener {
        void onLoad();
    }

}
