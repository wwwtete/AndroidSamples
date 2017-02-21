package com.wangw.samples.recyclerview.stickyheader;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by wangw on 2017/2/18.
 */

public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {

    private StickyHeaderAdapter mAdapter;
    private Rect mTempRect = new Rect();
    private Rect mTempRect1 = new Rect();
    private Rect mTempRect2 = new Rect();
    private final SparseArray<Rect> mHeaderRects = new SparseArray<>();
    private HeaderViewCache mHeaderProvider;
    private int mOrientation;

    public StickyHeaderDecoration(StickyHeaderAdapter adapter,int orientation) {
        mAdapter = adapter;
        mOrientation = orientation;
        mHeaderProvider = new HeaderViewCache(mAdapter,mOrientation);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        int childCount = parent.getChildCount();
        if (childCount <= 0 || mAdapter.getAllItemCount() <= 0)
            return;
        boolean isFristHeader;
        for (int i = 0; i < childCount; i++) {
            View itemView = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(itemView);
            if (position == RecyclerView.NO_POSITION)
                continue;
            isFristHeader = isFirstHeader(itemView,position);
            if (isFristHeader ||
                    hasNewHeader(position)){
                View header = mHeaderProvider.getHeader(parent,position,itemView);
                Rect headerOffset = mHeaderRects.get(position);
                if (headerOffset == null){
                    headerOffset = new Rect();
                    mHeaderRects.put(position,headerOffset);
                }
                initHeaderOffset(headerOffset,parent,header,itemView,isFristHeader);
                drawHeader(parent,c,header,headerOffset);
            }
        }

    }

    private void drawHeader(RecyclerView parent, Canvas canvas, View header, Rect headerOffset) {
        canvas.save();

        if (parent.getLayoutManager().getClipToPadding()) {
            // Clip drawing of headers to the padding of the RecyclerView. Avoids drawing in the padding
            initClipRectForHeader(mTempRect, parent, header);
            canvas.clipRect(mTempRect);
        }

        canvas.translate(headerOffset.left, headerOffset.top);

        header.draw(canvas);
        canvas.restore();
    }

    private void initClipRectForHeader(Rect clipRect, RecyclerView recyclerView, View header) {
        DimensionUtils.initMargins(clipRect, header);
        if (mOrientation == LinearLayout.VERTICAL) {
            clipRect.set(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getWidth() - recyclerView.getPaddingRight() - clipRect.right,
                    recyclerView.getHeight() - recyclerView.getPaddingBottom());
        } else {
            clipRect.set(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getWidth() - recyclerView.getPaddingRight(),
                    recyclerView.getHeight() - recyclerView.getPaddingBottom() - clipRect.bottom);
        }
    }

    private void initHeaderOffset(Rect headerOffset, RecyclerView parent, View header, View itemView, boolean isFristHeader) {
        initDefaultHeaderOffset(headerOffset,parent,header,itemView);
        if (isFristHeader && isStickyHeaderBeingPushedOffscreen(parent, header,itemView)) {
            View viewAfterNextHeader = getFirstViewUnobscuredByHeader(parent, header);
            int firstViewUnderHeaderPosition = parent.getChildAdapterPosition(viewAfterNextHeader);
            View secondHeader = mHeaderProvider.getHeader(parent, firstViewUnderHeaderPosition,viewAfterNextHeader);
            translateHeaderWithNextHeader(parent, headerOffset,
                    header, viewAfterNextHeader, secondHeader);
        }
    }

    private boolean isStickyHeaderBeingPushedOffscreen(RecyclerView recyclerView, View stickyHeader,View itemView) {
        View viewAfterHeader = getFirstViewUnobscuredByHeader(recyclerView, stickyHeader);
        int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterHeader);
        if (firstViewUnderHeaderPosition == RecyclerView.NO_POSITION) {
            return false;
        }

        if (firstViewUnderHeaderPosition > 0 && hasNewHeader(firstViewUnderHeaderPosition)) {
            View nextHeader = mHeaderProvider.getHeader(recyclerView, firstViewUnderHeaderPosition,viewAfterHeader);
            DimensionUtils.initMargins(mTempRect1, nextHeader);
            DimensionUtils.initMargins(mTempRect2, stickyHeader);

            if (mOrientation == LinearLayoutManager.VERTICAL) {
                int topOfNextHeader = viewAfterHeader.getTop() - mTempRect1.bottom - nextHeader.getHeight() - mTempRect1.top;
                int bottomOfThisHeader = recyclerView.getPaddingTop() + stickyHeader.getBottom() + mTempRect2.top + mTempRect2.bottom;
                if (topOfNextHeader < bottomOfThisHeader) {
                    return true;
                }
            } else {
                int leftOfNextHeader = viewAfterHeader.getLeft() - mTempRect1.right - nextHeader.getWidth() - mTempRect1.left;
                int rightOfThisHeader = recyclerView.getPaddingLeft() + stickyHeader.getRight() + mTempRect2.left + mTempRect2.right;
                if (leftOfNextHeader < rightOfThisHeader) {
                    return true;
                }
            }
        }

        return false;
    }

    private void translateHeaderWithNextHeader(RecyclerView recyclerView, Rect translation,
                                               View currentHeader, View viewAfterNextHeader, View nextHeader) {
        DimensionUtils.initMargins(mTempRect1, nextHeader);
        DimensionUtils.initMargins(mTempRect2, currentHeader);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            int topOfStickyHeader = getListTop(recyclerView) + mTempRect2.top + mTempRect2.bottom;
            int shiftFromNextHeader = viewAfterNextHeader.getTop()  - mTempRect1.bottom - mTempRect1.top - currentHeader.getHeight() - topOfStickyHeader;
            if (shiftFromNextHeader < topOfStickyHeader) {
                translation.top += shiftFromNextHeader;
            }
        } else {
            int leftOfStickyHeader = getListLeft(recyclerView) + mTempRect2.left + mTempRect2.right;
            int shiftFromNextHeader = viewAfterNextHeader.getLeft() - nextHeader.getWidth() - mTempRect1.right - mTempRect1.left - currentHeader.getWidth() - leftOfStickyHeader;
            if (shiftFromNextHeader < leftOfStickyHeader) {
                translation.left += shiftFromNextHeader;
            }
        }
    }

    /**
     * Returns the first item currently in the RecyclerView that is not obscured by a header.
     *
     * @param parent Recyclerview containing all the list items
     * @return first item that is fully beneath a header
     */
    private View getFirstViewUnobscuredByHeader(RecyclerView parent, View firstHeader) {
        boolean isReverseLayout = false;
        int step = isReverseLayout? -1 : 1;
        int from = isReverseLayout? parent.getChildCount()-1 : 0;
        for (int i = from; i >= 0 && i <= parent.getChildCount() - 1; i += step) {
            View child = parent.getChildAt(i);
            if (!itemIsObscuredByHeader(parent, child, firstHeader)) {
                return child;
            }
        }
        return null;
    }

    private boolean itemIsObscuredByHeader(RecyclerView parent, View item, View header) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) item.getLayoutParams();
        DimensionUtils.initMargins(mTempRect1, header);

        int adapterPosition = parent.getChildAdapterPosition(item);
        if (!mAdapter.headerPositionValid(adapterPosition))
            return true;
        if (adapterPosition == RecyclerView.NO_POSITION || mHeaderProvider.getHeader(parent, adapterPosition,item) != header) {
            // Resolves https://github.com/timehop/sticky-headers-recyclerview/issues/36
            // Handles an edge case where a trailing header is smaller than the current sticky header.
            return false;
        }

        if (mOrientation == LinearLayoutManager.VERTICAL) {
            int itemTop = item.getTop() - layoutParams.topMargin;
            int headerBottom = getListTop(parent) + header.getBottom() + mTempRect1.bottom + mTempRect1.top;
            if (itemTop >= headerBottom) {
                return false;
            }
        } else {
            int itemLeft = item.getLeft() - layoutParams.leftMargin;
            int headerRight = getListLeft(parent) + header.getRight() + mTempRect1.right + mTempRect1.left;
            if (itemLeft >= headerRight) {
                return false;
            }
        }

        return true;
    }

    private void initDefaultHeaderOffset(Rect headerOffset, RecyclerView parent, View header, View itemView) {
        int translationX, translationY;
        DimensionUtils.initMargins(mTempRect1,header);
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        int leftMargin = 0;
        int topMargin =0;
        if (layoutParams instanceof ViewGroup.MarginLayoutParams){
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            leftMargin = marginLayoutParams.leftMargin;
            topMargin = marginLayoutParams.topMargin;
        }
        if (mOrientation == LinearLayoutManager.VERTICAL){
            translationX = itemView.getLeft() - leftMargin + mTempRect1.left;
            translationY = Math.max(itemView.getTop() - topMargin - mTempRect1.bottom,
                    getListTop(parent)+ mTempRect1.top);
        }else {
            translationY = itemView.getTop() - topMargin + mTempRect1.top;
            translationX = Math.max(
                    itemView.getLeft() - leftMargin - mTempRect1.right,
                    getListLeft(parent) + mTempRect1.left);
        }
        headerOffset.set(translationX,translationY,translationX+header.getWidth(),translationY+header.getHeight());
    }

    private int getListTop(RecyclerView view) {
        if (view.getLayoutManager().getClipToPadding()) {
            return view.getPaddingTop();
        } else {
            return 0;
        }
    }

    private int getListLeft(RecyclerView view) {
        if (view.getLayoutManager().getClipToPadding()) {
            return view.getPaddingLeft();
        } else {
            return 0;
        }
    }

    private boolean hasNewHeader(int position) {
        if (indexOutOfBounds(position))
            return false;
        return mAdapter.headerPositionValid(position);
    }


    private boolean isFirstHeader(View child,  int position) {
        DimensionUtils.initMargins(mTempRect1,child);
        int offset,margin;
       if (mOrientation == LinearLayoutManager.VERTICAL){
           offset = child.getTop();
           margin = mTempRect1.top;
       }else {
           offset = child.getLeft();
           margin = mTempRect1.left;
       }
        return offset <= margin && mAdapter.headerPositionValid(position);
    }

    private boolean indexOutOfBounds(int position) {
        return position < 0 || position >= mAdapter.getAllItemCount();
    }

    private void throwIfNotLinearLayoutManager(RecyclerView.LayoutManager layoutManager){
        if (!(layoutManager instanceof LinearLayoutManager)) {
            throw new IllegalStateException("StickyListHeadersDecoration can only be used with a " +
                    "LinearLayoutManager.");
        }
    }

    public void invalidateHeaders(){
        if (mHeaderProvider != null){
            mHeaderProvider.invalidate();
        }
        mHeaderRects.clear();
    }

    private void log(String log){
        Log.d("StickyHeader",log);
    }


    public int findHeaderPostionUnder(RecyclerView recyclerView,int x, int y) {
        Rect rect;
        int position;
        View view;
        for (int i=0;i<mHeaderRects.size();i++){
            rect = mHeaderRects.get(mHeaderRects.keyAt(i));
            position = mHeaderRects.keyAt(i);
            view = recyclerView.getLayoutManager().findViewByPosition(position);
            if (view != null && view.getBottom() >= y && contains(rect,x,y)){

                if (mAdapter != null && mAdapter.headerPositionValid(position)) {
                    log("[findHeaderPostionUnder] position="+position+" rect="+rect.toString());
                    return position;
                }

            }
        }
        return -1;
    }

    private boolean contains(Rect rect,int x,int y){
        if(rect.top < 0){
            return rect.contains(x,y) && y <= rect.top+rect.bottom;
        }else
            return rect.contains(x,y);
    }

    public View getHeaderView(View itemView, RecyclerView recyclerView, int position) {
       return mHeaderProvider.getHeader(recyclerView,position,itemView);
    }
}
