package com.wangw.samples.recyclerview.stickyheader;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

/**
 */
public class HeaderViewCache {

  private final StickyHeaderAdapter mAdapter;
  private final SparseArray<View> mHeaderViews = new SparseArray<>();
  private int mOrientation;

  public HeaderViewCache(StickyHeaderAdapter adapter,
                         int orientation) {
    mAdapter = adapter;
    mOrientation = orientation;
  }

  public View getHeader(RecyclerView parent, int position,View itemView) {

    //TODO 如果实时性要求较高则直接调用Adapter获取，如果实时性不高则使用缓存
    View header = mAdapter.getHeaderView(parent,position,itemView);//mHeaderViews.get(position);
    if (header == null) {
      //通过Adapter获取StickyHeaderView
      header = mAdapter.getHeaderView(parent,position,itemView);
      mHeaderViews.put(position, header);
//      if (header.getLayoutParams() == null) {
//        header.setLayoutParams(new ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//      }

      //TODO 如果需要测量StickyHeader布局，则放开此处代码
//      int widthSpec;
//      int heightSpec;
//
//      if (mOrientation == LinearLayoutManager.VERTICAL) {
//        widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
//        heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.EXACTLY);
//      } else {
//        widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.UNSPECIFIED);
//        heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.EXACTLY);
//      }
//
//      int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
//          parent.getPaddingLeft() + parent.getPaddingRight(), header.getLayoutParams().width);
//      int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
//          parent.getPaddingTop() + parent.getPaddingBottom(), header.getLayoutParams().height);
//      header.measure(childWidth, childHeight);
//      header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
    }
    return header;
  }

  public void invalidate() {
    mHeaderViews.clear();
  }
}
