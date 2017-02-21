package com.wangw.samples.media;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wangw.samples.R;
import com.wangw.samples.media.filter.filter.FilterManager;

/**
 * Created by wangw on 2017/2/7.
 */

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private FilterManager.FilterType[] mFilterTypes;
    private MediaCodecSampleActivity mActivity;

    public FilterAdapter(FilterManager.FilterType[] filterTypes,MediaCodecSampleActivity activity) {
        mFilterTypes = filterTypes;
        this.mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.samples_item,null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(mFilterTypes[position]);
    }

    @Override
    public int getItemCount() {
        return mFilterTypes != null ? mFilterTypes.length : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FilterManager.FilterType mFilterType;
        private TextView mTvName;
        public ViewHolder(View itemView) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            itemView.setOnClickListener(this);

        }

        public void bindData(FilterManager.FilterType filterType) {
            mFilterType = filterType;
            mTvName.setText(filterType.name());
        }

        @Override
        public void onClick(View v) {
            mActivity.startFilter(mFilterType);
        }
    }
}
