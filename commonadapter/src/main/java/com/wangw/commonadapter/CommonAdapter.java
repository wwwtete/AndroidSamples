package com.wangw.commonadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 普通ListView的Adapter
 * Created by wangw on 2016/4/14.
 */
public abstract class CommonAdapter<T> extends BaseAdapter implements AdapterNotification<T> {

    private List<T> mDatas;
    private int mItemLayoutId;
    private  Context mContext;
    private LayoutInflater mInflater;

    public CommonAdapter(Context context,int itemLayoutId,List<T> datas){
        mDatas = datas == null ? new ArrayList<T>() : datas;
        mContext = context;
        mItemLayoutId = itemLayoutId;
    }

    public CommonAdapter(Context context,int itemlayoutId){
        this(context, itemlayoutId, null);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(mInflater == null)
            mInflater = LayoutInflater.from(mContext);

        ViewHolder holder = get(mContext,convertView,parent,mItemLayoutId);
        holder.updatePosition(position);

        onBindData(holder,getItem(position),position);

        return holder.getConvertView();
    }

    protected abstract void onBindData(ViewHolder holder, T item, int position);

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

    public ViewHolder get(Context context,View convertView,ViewGroup viewGroup,int layoutId){
        if(convertView == null){
            return new ViewHolder(context,viewGroup,layoutId);
        }
        return (ViewHolder) convertView.getTag();
    }

    class ViewHolder{
        private int mPosition;
        private Context mContext;
        private View mConvertView;
        private ViewHelpImpl mFv;

        public ViewHolder(Context context,ViewGroup viewGroup,int layoutId){
            this.mContext = context;
            this.mConvertView = LayoutInflater.from(context).inflate(layoutId,viewGroup,false);
            mConvertView.setTag(this);
            mFv = new ViewHelpImpl(mConvertView);
        }

        public void updatePosition(int position){
            this.mPosition = position;
        }

        public int getPosition() {
            return mPosition;
        }

        public View getConvertView(){
            return mConvertView;
        }
    }

}
