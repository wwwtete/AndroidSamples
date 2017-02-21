package com.wangw.samples.media;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;
import com.wangw.samples.comm.FileExplorerActivity;
import com.wangw.samples.media.filter.filter.FilterManager;
import com.wangw.samples.media.filter.video.CodecConfig;
import com.wangw.samples.media.filter.video.MediaCodecLHelper;
import com.wangw.samples.media.filter.video.OnVideoProcessCallback;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wangw on 2017/2/4.
 */

public class MediaCodecSampleActivity extends BaseActivity implements OnVideoProcessCallback {
    public static final String TEST_FILE = "ping20s.mp4";
    @Bind(R.id.ev_videpath)
    EditText mEvVidepath;
    @Bind(R.id.rv_filters)
    RecyclerView mRvFilters;
    @Bind(R.id.iv_select)
    ImageView mIvSelect;
    @Bind(R.id.ev_audiopath)
    EditText mEvAudiopath;
    @Bind(R.id.iv_select_audio)
    ImageView mIvSelectAudio;
    @Bind(R.id.btn_play)
    Button mBtnPlay;

    private String mOutpuVideo;
    private long mStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_codec_sample);
        ButterKnife.bind(this);
        initView();

    }

    private void initView() {
        mRvFilters.setLayoutManager(new LinearLayoutManager(this));
        mRvFilters.setAdapter(new FilterAdapter(FilterManager.FilterType.values(), this));
    }

    @OnClick({R.id.btn_play, R.id.iv_select,R.id.iv_select_audio})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_select:
                onSelectFile(true);
                break;
            case R.id.iv_select_audio:
                onSelectFile(false);
                break;
            case R.id.btn_play:
                onPlayVideo();
                break;
        }
    }

    private void onPlayVideo() {
        if (TextUtils.isEmpty(mOutpuVideo)) {
            showToast("视频地址为空");
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndTypeAndNormalize(Uri.fromFile(new File(mOutpuVideo)), "video/*");
        startActivity(intent);

    }

    private void onSelectFile(boolean isVideo) {
        Intent intent = new Intent(this, FileExplorerActivity.class);
        intent.putExtra("SELECT_MODE", isVideo ? "video" : "audio");
        startActivityForResult(intent, isVideo ? 8800 : 9900);
    }

    private void onFilter(FilterManager.FilterType filterType) {
        String path = mEvVidepath.getText().toString();
        if (!TextUtils.isEmpty(path)) {
            if (!new File(path).exists()) {
                showToast("视频文件不存在");
                return;
            }
        } else {
            showToast("请输入视频地址");
            return;
        }
        String audioPath = mEvAudiopath.getText().toString();
//        MediaCodecHelper helper = new MediaCodecHelper(true, true);
        MediaCodecLHelper helper = new MediaCodecLHelper(true, true);
        helper.setOnVideoProcessCallback(this);
        String srcVideo = path;//"/sdcard/movice/01.mp4";//
        String srcAudio = TextUtils.isEmpty(audioPath) ? path : audioPath;
        mOutpuVideo = SampleActivity.ROOT_DIR + "/" + System.currentTimeMillis() + ".mp4";

        CodecConfig config = new CodecConfig(srcVideo, srcAudio, mOutpuVideo, filterType);
        mStart = System.currentTimeMillis();
        helper.startProcessVideo(MediaCodecSampleActivity.this, config);
        showLoading(false);
    }

    public Bitmap getBmp() {
        Bitmap bmp = FilterManager.getSoftLightBitmap(this, R.drawable.if_ic_launcher);
//        Bitmap bitmap2 = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap2);
//        canvas.drawARGB(0, 0, 255, 0);
//        canvas.drawBitmap(bmp,null,new Rect(10,10,32,32),new Paint());

        Matrix matrix = new Matrix();
        matrix.postScale(0.2f, 0.2f);
        Bitmap bitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawBitmap(bmp, null, new Rect(10, 10, 32, 32), paint);
//        canvas.drawBitmap(bitmap,null,new Rect(10,10,32,32),null);
        return bitmap;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String path = data.getStringExtra("SELECT_VIDEO");
            if (requestCode == 8800)
                mEvVidepath.setText(path);
            else
                mEvAudiopath.setText(path);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public String getSampleName() {
        return "MediaCodec+OpenGl各种滤镜效果";
    }

    @Override
    public void onVideoProcessComplete() {
        closeLoading();
    }

    @Override
    public void onVideoProcessSuccess() {
        closeLoading();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast("处理成功,用时 =" + ((System.currentTimeMillis() - mStart) / 1000.0f) + "秒");
            }
        });

        onPlayVideo();
    }

    @Override
    public void onVideoProcessFailed(final String errMsg) {
        closeLoading();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast("处理失败 :" + errMsg);
            }
        });

    }

    @Override
    public void onVideoProcessCanceled() {
        closeLoading();
    }

    public void startFilter(FilterManager.FilterType filterType) {
        if (filterType == FilterManager.FilterType.Blend) {
            FilterManager.setBlendBitmap(getBmp());//BitmapFactory.decodeFile(imagePath));//(BitmapDrawable)getResources().getDrawable(R.drawable.if_ic_launcher)).getBitmap());
        }
        onFilter(filterType);
    }
}
