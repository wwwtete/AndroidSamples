package com.wangw.samples.fresco;

import android.net.Uri;
import android.os.Bundle;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

/**
 * Created by wangw on 2016/12/24.
 */

public class FristSampleActiity extends BaseActivity {

    SimpleDraweeView mDraweeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fresco_frist);
        mDraweeView = (SimpleDraweeView) findViewById(R.id.my_image_view);

        String url = "";//"http://pics.sc.chinaz.com/files/pic/pic9/201508/apic1405201.jpg";
        mDraweeView.setImageURI(Uri.parse(url));

    }

    @Override
    public String getSampleName() {
        return "第一个Sample";
    }
}
