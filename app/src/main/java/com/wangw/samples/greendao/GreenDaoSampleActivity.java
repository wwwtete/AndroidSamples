package com.wangw.samples.greendao;

import android.os.Bundle;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.greendao.dao.DaoSession;

/**
 * Created by wangw on 2017/2/22.
 */

public class GreenDaoSampleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaoSession daoSession;
    }

    @Override
    public String getSampleName() {
        return "GreenDao使用Sample";
    }
}
