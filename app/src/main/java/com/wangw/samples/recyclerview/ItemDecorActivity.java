package com.wangw.samples.recyclerview;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

/**
 * Created by wangw on 2016/12/14.
 */

public class ItemDecorActivity extends BaseActivity {

    RecyclerView mRecyclerView;
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemdecor);
        initView();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        LinearLayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        adapter = new Adapter();
        mRecyclerView.setLayoutManager(manager);
        MyItemDecoration itemDecoration = new MyItemDecoration(this);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setAdapter(adapter);


        StickHeaderTouchListener headerTouchListener = new StickHeaderTouchListener(mRecyclerView,itemDecoration);
        headerTouchListener.setClickListener(new StickHeaderTouchListener.OnHeaderClickListener() {
            @Override
            public void onHeaderClick() {
                Toast.makeText(ItemDecorActivity.this,"onHeaderClick",Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.addOnItemTouchListener(headerTouchListener);

    }

    @Override
    public String getSampleName() {
        return "ItemDecor之getItemOffsets方法";
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder>{

        private String[] values = {"A","B","C","D","E","A","B","C","D","E","A","B","C","D","E","A","B","C","D","E"};

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_item_decor,parent,false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Object tag = holder.mTv.getTag();
            boolean flag = false;
            if(tag != null){
                flag = (boolean) tag;
            }
            String txt = flag ? "onClick = "+position : values[position];
            holder.mTv.setText(txt);
            holder.bindData(position);
        }

        @Override
        public int getItemCount() {
            return values.length;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView mTv;
        private int mposition;

        public ViewHolder(View itemView) {
            super(itemView);
            mTv = (TextView) itemView.findViewById(R.id.tv_text);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ItemDecorActivity.this,"OnClikc = "+mposition,Toast.LENGTH_SHORT).show();
                    Object tag = mTv.getTag();
                    if(tag != null){
                        mTv.setTag(!((Boolean)tag));
                    }else {
                        mTv.setTag(true);
                    }
                    adapter.notifyItemChanged(mposition);
                }
            });
        }

        public void bindData(int position) {
            mposition = position;
        }
    }

}
