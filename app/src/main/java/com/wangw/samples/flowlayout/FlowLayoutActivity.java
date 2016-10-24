package com.wangw.samples.flowlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.flowlayout.view.FlowLayout;
import com.wangw.samples.materialrangebar.colorpicker.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FlowLayoutActivity extends BaseActivity {

    @Bind(R.id.tv_info)
    TextView mTvInfo;
    @Bind(R.id.flow_layout)
    FlowLayout mFlowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_layout);
        ButterKnife.bind(this);
        onInitView();
    }

    private void onInitView() {
        mTvInfo.setText("流式布局：自动换行");
        for (int i=0;i<80;i++){
            mFlowLayout.addView(onCreateItem("item "+i));
        }
    }


    private View onCreateItem(String text){
        TextView view = new TextView(this);
        view.setText(text);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT,ViewGroup.MarginLayoutParams.WRAP_CONTENT);
        params.topMargin = 20;
        params.bottomMargin = 10;
        params.leftMargin = params.rightMargin = 10;
        view.setPadding(20,20,20,20);
        view.setLayoutParams(params);
        view.setBackgroundColor(Color.GREEN);
        return view;
    }

    @Override
    public String getSampleName() {
        return "流式布局样例(自动换行)";
    }
}
