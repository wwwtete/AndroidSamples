package com.wangw.samples.recyclerview;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.recyclerview.stickyheader.StickyHeaderAdapter;
import com.wangw.samples.recyclerview.stickyheader.StickyHeaderClickListener;
import com.wangw.samples.recyclerview.stickyheader.StickyHeaderDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wangw on 2017/2/18.
 */

public class StickyHeaderActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private StickHeaderAdaper mAdaper;
    private HeaderRecyclerViewAdapter mHeaderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout rootLayout = new FrameLayout(this);
        mRecyclerView = new RecyclerView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(10,10,0,0);
        mRecyclerView.setLayoutParams(params);
        rootLayout.addView(mRecyclerView);
        setContentView(rootLayout);

//        setContentView(R.layout.activity_sticky_header_recycler);
//        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdaper = new StickHeaderAdaper();
        mHeaderAdapter= new HeaderRecyclerViewAdapter(mAdaper);
        final StickyHeaderDecoration stickyHeader = new StickyHeaderDecoration(mAdaper, layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(stickyHeader);
        mRecyclerView.setAdapter(mHeaderAdapter);
        initHeaderAndFooterView(mHeaderAdapter);
        mAdaper.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                stickyHeader.invalidateHeaders();
            }
        });
        mRecyclerView.addOnItemTouchListener(new StickyHeaderClickListener(mRecyclerView,stickyHeader) {
            @Override
            public void onClickHeader( int position) {
                Log.d("StickyHeader","[onClickHeader] position="+position);
                showToast("ClickHeader = "+position);
                mAdaper.onClickHeader(position);
//                mRecyclerView.scrollToPosition(position);
            }
        });


        initData(mAdaper);
    }

    private void initHeaderAndFooterView(HeaderRecyclerViewAdapter headerAdapter) {
        ImageView header = new ImageView(this);
        header.setImageResource(R.drawable.overlay);
        headerAdapter.addHeader(header);

        ImageView footer = new ImageView(this);
        footer.setImageResource(R.drawable.overlay);
        headerAdapter.addFooter(footer);
    }

    private void initData(StickHeaderAdaper adaper) {
        List<Pair<String,List<String>>> list = new ArrayList<>();
        String header;
        for (int i = 0; i < 5; i++) {
            header = "Header"+i;
            List<String> datas = new ArrayList<>();
            for (int i1 = 0; i1 < 25; i1++) {
                datas.add("index="+i+"|Item"+i1);
            }
            list.add(new Pair<>(header,datas));
        }
        adaper.setDatas(list);
    }

    @Override
    public String getSampleName() {
        return "使用ItemDecoration实现粘性Header效果";
    }


    class StickHeaderAdaper extends RecyclerView.Adapter<StickHeaderViewHolder> implements StickyHeaderAdapter {

        private List<Pair<String, List<String>>> mDatas;
        SparseArray<Boolean> mHidenList;


        public StickHeaderAdaper() {
            mHidenList = new SparseArray<>();
        }

        public List<Pair<String, List<String>>> getDatas() {
            return mDatas;
        }

        public void setDatas(List<Pair<String, List<String>>> datas) {
            mDatas = datas;
            notifyDataSetChanged();
        }

        @Override
        public StickHeaderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_recycleview, null);
            //TODO 必须创建一个LayoutParams，并且将宽设置为MATCH_PARENT，否则收缩列表时Header宽度会发生变化
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(params);
            return new StickHeaderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(StickHeaderViewHolder holder, int position) {
            Boolean res = mHidenList.get(position);
            res = res != null ? res : false;
            holder.bindData(position,res,mDatas.get(position));
        }

        @Override
        public int getItemCount() {
            if (mDatas == null)
                return 0;
            else
                return mDatas.size();
        }

        @Override
        public int getAllItemCount() {
            return mHeaderAdapter.getItemCount();
        }

        @Override
        public boolean headerPositionValid(int position) {
            //TODO 如果没有Header则直接返回true即可，此方法是用于判断当前View对应的Position是否为Header
            return  !(HeaderRecyclerViewAdapter.isHeaderView(mHeaderAdapter.getItemViewType(position)) ||
                    HeaderRecyclerViewAdapter.isFooterView(mHeaderAdapter.getItemViewType(position)));
        }

        @Override
        public View getHeaderView(RecyclerView parent, int posit, View itemView) {
            TextView header = findViewHolder(posit).mTvContent;
            header.setText("");
            return header;
        }

        public void removeItem(int index, int position) {
            Pair<String, List<String>> pair = mDatas.get(index);
            if (pair != null){
                List list = pair.second;
                if (list != null){
                    list.remove(position);
                    mHeaderAdapter.notifyDataSetChanged();
//                    mHeaderAdapter.notifyItemChanged(position+mHeaderAdapter.getHeaderSize());
                }
            }
        }

        private StickHeaderViewHolder findViewHolder(int position){
            return (StickHeaderViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
        }

        public void onClickHeader( final int position) {
            StickHeaderViewHolder holder = findViewHolder(position);
            boolean tag = holder.getTag();
            int p = position - mHeaderAdapter.getHeaderSize();
            if (!tag) {
                holder.hidenRecyclerView();
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int next = position+1 >= mHeaderAdapter.getItemCount() ? 0 : position+1;
                        mRecyclerView.scrollToPosition(next);
                    }
                },50);
                mHidenList.put(p,true);
            }else {
                holder.showRecyclerView();
                mRecyclerView.scrollTo(holder.itemView.getLeft(),holder.itemView.getTop());
                mHidenList.put(p,false);
            }
            holder.setTag(!tag);
        }
    }

    class StickHeaderViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_content)
        TextView mTvContent;
        @Bind(R.id.rv_items)
        RecyclerView mRvItems;

        Pair<String, List<String>> mData;
        DemoAdapter adapter;
        GridLayoutManager gdManager;
        boolean mTag;

        public StickHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            gdManager= new GridLayoutManager(StickyHeaderActivity.this,3);
            mRvItems.setLayoutManager(gdManager);
            adapter = new DemoAdapter();
            mRvItems.setAdapter(adapter);
        }

        public void bindData(int position,boolean isHiden,Pair<String, List<String>> stringListPair) {
            if (isHiden){
                mRvItems.setVisibility(View.GONE);
            }else {
                mRvItems.setVisibility(View.VISIBLE);
            }
            mData = stringListPair;
            mTvContent.setText(mData.first);
            adapter.setDatas(position,mData.second);
        }

        public void hidenRecyclerView() {
            mRvItems.setVisibility(View.GONE);
        }

        public void showRecyclerView() {
            mRvItems.setVisibility(View.VISIBLE);
        }

        public boolean getTag() {
            return mTag;
        }

        public void setTag(boolean tag) {
            mTag = tag;
        }
    }

    class DemoAdapter extends RecyclerView.Adapter<DemoViewHolder>{

        private List<String> mDatas;
        private int mPosition;
        public List<String> getDatas() {
            return mDatas;
        }

        public void setDatas(int position,List<String> datas) {
            mDatas = datas;
            mPosition= position;
            notifyDataSetChanged();
        }

        @Override
        public DemoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DemoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid,null));
        }

        @Override
        public void onBindViewHolder(DemoViewHolder holder, int position) {
            holder.bindData(mPosition,position,mDatas.get(position));
        }

        @Override
        public int getItemCount() {
            if (mDatas == null)
                return 0;
            else
                return mDatas.size();
        }
    }

    class DemoViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvContent;

        public DemoViewHolder(View itemView) {
            super(itemView);
            mTvContent = (TextView) itemView.findViewById(R.id.tv_content);
        }

        public void bindData(final int index, final int position, String s) {
            mTvContent.setText(s+"|position="+position);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdaper.removeItem(index,position);
                }
            });
        }

    }

}
