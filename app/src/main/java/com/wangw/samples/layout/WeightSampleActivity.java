package com.wangw.samples.layout;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

/**
 * Created by wangw on 2016/10/21.
 */
public class WeightSampleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_sample);
    }

    @Override
    public String getSampleName() {
        return "LinearLayout权重属性layout_weight样例";
    }
}
