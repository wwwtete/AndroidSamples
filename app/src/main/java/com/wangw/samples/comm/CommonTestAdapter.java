package com.wangw.samples.comm;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wangw.samples.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wangw on 2017/1/22.
 */

public class CommonTestAdapter extends RecyclerView.Adapter<CommonTestAdapter.ViewHolder> {

    private String[] mdatas;

    public CommonTestAdapter(int size) {
        mdatas = new String[size];
        for (int i = 0; i < mdatas.length; i++) {
            mdatas[i] = "item" + i;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(mdatas[position]);
    }

    @Override
    public int getItemCount() {
        return mdatas != null ? mdatas.length : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {


        @Bind(R.id.textItem)
        TextView mTextItem;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void bindData(String mdata) {
            mTextItem.setText(mdata);
        }
    }
}
