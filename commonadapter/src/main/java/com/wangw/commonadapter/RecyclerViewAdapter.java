package com.wangw.commonadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView的Adapter
 * Created by wangw on 2016/4/14.
 */
public abstract class RecyclerViewAdapter<T> extends RecyclerView.Adapter implements AdapterNotification<T> {


    protected Context mContext;
    protected LayoutInflater mInflater;
    protected int mLayoutResId;
    protected List<T> mDatas;
    protected OnItemClickListener<T> mListener;


    public RecyclerViewAdapter(Context context,int resId){
        this(context,resId,null);
    }

    public RecyclerViewAdapter(Context context,int resId,List<T> datas){
        mContext = context;
        mLayoutResId = resId;
        mDatas = datas == null ? new ArrayList<T>() : datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mInflater == null)
            mInflater = LayoutInflater.from(mContext);
        return new RecyclerViewHolder(mInflater.inflate(mLayoutResId,parent,false));
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener =listener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onItemClick(v,mDatas.get(position),position);
                }
            }
        });
        onBindData((RecyclerViewHolder) holder, mDatas.get(position), position);
    }

    /**
     * 绑定数据
     * @param holder
     * @param t
     * @param position
     */
    protected abstract void onBindData(RecyclerViewHolder holder, T t, int position);

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void add(T data) {
        mDatas.add(data);
        notifyDataSetChanged();
    }

    @Override
    public void add(List<T> datas) {
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public void set(int position,T data) {
        mDatas.add(position, data);
        notifyDataSetChanged();
    }

    @Override
    public void refreshData(List<T> datas) {
        mDatas.clear();
        if(datas != null && !datas.isEmpty()){
            mDatas.addAll(datas);
        }
        notifyDataSetChanged();
    }

    @Override
    public void clearData() {
        mDatas.clear();
        notifyDataSetChanged();
    }

    @Override
    public void removeData(int position) {
        mDatas.remove(position);
        notifyDataSetChanged();
    }

   protected class RecyclerViewHolder extends RecyclerView.ViewHolder implements ViewHelp{

        private ViewHelp mHelp;


        public RecyclerViewHolder(View itemView) {
            super(itemView);
            mHelp = new ViewHelpImpl(itemView);
        }


        @Override
        public <E extends View> E getView(int id) {
            return mHelp.getView(id);
        }

        @Override
        public ViewHelp setText(int viewId, CharSequence text) {
            return mHelp.setText(viewId,text);
        }

        @Override
        public ViewHelp setText(int viewId, CharSequence text, int colorRresId) {
            return mHelp.setText(viewId,text,colorRresId);
        }

        @Override
        public ViewHelp setImageResource(int viewId, int drawableId) {
            return mHelp.setImageResource(viewId,drawableId);
        }

        @Override
        public ViewHelp setImageBitmap(int viewId, Bitmap bm) {
            return mHelp.setImageBitmap(viewId,bm);
        }

        @Override
        public ViewHelp setVisible(int viewId, int visibility) {
            return mHelp.setVisible(viewId,visibility);
        }
    }

    public interface OnItemClickListener<T>{
        void onItemClick(View view,T data,int position);
    }

}
