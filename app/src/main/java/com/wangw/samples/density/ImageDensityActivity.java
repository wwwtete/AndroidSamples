package com.wangw.samples.density;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageDensityActivity extends BaseActivity {

    @Bind(R.id.iv_img)
    ImageView mIvImg;
    @Bind(R.id.tv_msg)
    TextView mTvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_density);
        ButterKnife.bind(this);

        mIvImg.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mIvImg.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                StringBuilder builder = new StringBuilder();
                builder.append("densityDpi = ")
                        .append(getResources().getDisplayMetrics().densityDpi)
                        .append("\n density =")
                        .append(getResources().getDisplayMetrics().density)
                        .append("\n");
                builder.append("图片原始 size = 100*100\n");
                builder.append("组件 size = ")
                        .append(mIvImg.getWidth())
                        .append("*")
                        .append(mIvImg.getHeight())
                        .append("\n");
                Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.test);
                builder.append("图片加载后 size = ")
                        .append(bmp.getWidth())
                        .append("*")
                        .append(bmp.getHeight())
                        .append("\n");
                builder.append("所占内存大小")
                        .append(bmp.getRowBytes()*bmp.getHeight()/1024.0f/1024.0f)
                        .append("m");
                mTvMsg.setText(builder.toString());
            }
        });

    }

    @Override
    public String getSampleName() {
        return "不同的drawable显示不同的宽高";
    }
}
