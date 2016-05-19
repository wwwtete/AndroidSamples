package com.wangw.samples.comm;

import android.content.Context;

import com.wangw.commonadapter.RecyclerViewAdapter;
import com.wangw.samples.R;

/**
 * Created by wangw on 2016/4/15.
 */
public class SamplesAdapter extends RecyclerViewAdapter<SamplesModel> {


    public SamplesAdapter(Context context) {
        super(context, R.layout.samples_item);
    }

    @Override
    protected void onBindData(RecyclerViewAdapter<SamplesModel>.RecyclerViewHolder holder, SamplesModel samplesModel, int position) {
        holder.setText(R.id.tv_name,samplesModel.getSampleName());
    }
}
