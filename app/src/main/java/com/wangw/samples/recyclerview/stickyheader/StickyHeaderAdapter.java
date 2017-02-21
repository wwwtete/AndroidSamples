package com.wangw.samples.recyclerview.stickyheader;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by wangw on 2017/2/18.
 */

public interface StickyHeaderAdapter<VH extends RecyclerView.ViewHolder> {

    /**
     * 获取所有的Item总数(包含Header和Footer)
     * @return
     */
    int getAllItemCount();

    /**
     * 当前Position是否有效的Header下标
     * @param position
     * @return
     */
    boolean headerPositionValid(int position);

    View getHeaderView(RecyclerView parent, int position, View itemView);
}
