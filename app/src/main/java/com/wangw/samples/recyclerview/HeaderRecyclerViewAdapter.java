package com.wangw.samples.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class HeaderRecyclerViewAdapter extends RecyclerView.Adapter {
    private static final int MAX_HEADER_VIEW_SIZE = 100;
    public static final int VIEW_TYPE_HEADER_OFFSET = Integer.MIN_VALUE;
    public static final int VIEW_TYPE_FOOTER_OFFSET = Integer.MIN_VALUE + MAX_HEADER_VIEW_SIZE;
    private RecyclerView.Adapter adapter;
    private List<View> headerViews = new ArrayList<>();
    private List<View> footerViews = new ArrayList<>();

    public static boolean isHeaderView(int viewType) {
        return viewType < VIEW_TYPE_HEADER_OFFSET + MAX_HEADER_VIEW_SIZE;
    }

    public static boolean isFooterView(int viewType) {
        return viewType >= VIEW_TYPE_FOOTER_OFFSET
                && viewType < VIEW_TYPE_FOOTER_OFFSET + MAX_HEADER_VIEW_SIZE;
    }

    public HeaderRecyclerViewAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    public void setWrappedAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
        notifyDataSetChanged();
    }

    public void addHeader(View headerView) {
        if (headerView != null && !headerViews.contains(headerView)) {
            headerViews.add(headerView);
            notifyDataSetChanged();
        }
    }

    public boolean hasHeader() {
        return !headerViews.isEmpty();
    }

    public void addFooter(View footerView) {
        if (footerView != null && !footerViews.contains(footerView)) {
            footerViews.add(footerView);
            notifyDataSetChanged();
        }
    }

    public boolean hasFooter() {
        return !footerViews.isEmpty();
    }

    public void removeHeader(View headerView) {
        if (headerView != null && this.headerViews.contains(headerView)) {
            this.headerViews.remove(headerView);
            notifyDataSetChanged();
        }
    }

    public void removeFooter(View footerView) {
        if (footerView != null && this.footerViews.contains(footerView)) {
            this.footerViews.remove(footerView);
            notifyDataSetChanged();
        }
    }

    public boolean isEmpty() {
        return adapter == null || adapter.getItemCount() == 0;
    }

    public int getHeaderSize() {
        return headerViews.size();
    }

    public int getFooterSize() {
        return footerViews.size();
    }

    public List<View> getHeaderViews() {
        return headerViews;
    }

    public List<View> getFooterViews() {
        return footerViews;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isHeaderView(viewType)) {
            return new HeaderViewHolder(headerViews.get(viewType - VIEW_TYPE_HEADER_OFFSET));
        }
        if (isFooterView(viewType)) {
            return new FooterViewHolder(footerViews.get(viewType - VIEW_TYPE_FOOTER_OFFSET));
        }
        return adapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (isHeaderView(viewType) || isFooterView(viewType)) {
            return;
        }
        position -= headerViews.size();
        adapter.onBindViewHolder(holder, position);
    }

    @Override   // TODO 其实现在还是没有利用起来payloads
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        if (payloads == null || payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            int viewType = getItemViewType(position);
            if (isHeaderView(viewType) || isFooterView(viewType)) {
                return;
            }
            position -= headerViews.size();
            adapter.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (adapter != null) {
            count += adapter.getItemCount();
        }
        count += headerViews.size();
        count += footerViews.size();
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (!headerViews.isEmpty() && position < headerViews.size()) {
            return VIEW_TYPE_HEADER_OFFSET + position;
        }
        if (!headerViews.isEmpty()) {
            position -= headerViews.size();
        }
        if (adapter != null && position < adapter.getItemCount()) {
            return adapter.getItemViewType(position);
        }
        if (adapter != null) {
            position -= adapter.getItemCount();
        }
        return VIEW_TYPE_FOOTER_OFFSET + position;
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        if (adapter != null) {
            adapter.unregisterAdapterDataObserver(observer);
        }
    }

    @Override
    public long getItemId(int position) {
        if (!headerViews.isEmpty() && position < headerViews.size()) {
            return VIEW_TYPE_HEADER_OFFSET + position;
        }
        if (!headerViews.isEmpty()) {
            position -= headerViews.size();
        }
        if (adapter != null && position < adapter.getItemCount()) {
            return adapter.getItemId(position);
        }
        if (adapter != null) {
            position -= adapter.getItemCount();
        }
        return VIEW_TYPE_FOOTER_OFFSET + position;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }


}
