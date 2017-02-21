package com.wangw.samples.media.filter.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.wangw.samples.media.filter.filter.FilterManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xingliao_zgl on 16/8/31.
 */
public class ImageFilterHelper {

    private static final String TAG = "zl";
    private static final boolean VERBOSE = false;
    private ImageFilterHelper() {
    }

    private static ImageFilterHelper mInstance;

    public static ImageFilterHelper getInstance() {
        if (mInstance == null) {
            synchronized (ImageFilterHelper.class) {
                if (mInstance == null) {
                    mInstance = new ImageFilterHelper();
                }
            }
        }
        return mInstance;
    }

    public void release(){
        if(mImageFilerStatus != null){
            mImageFilerStatus.clear();
            mImageFilerStatus = null;
        }

        if(mFilterBitmaps != null){
            mFilterBitmaps.clear();
            mFilterBitmaps = null;
        }
    }

    private Map<FilterManager.FilterType, Bitmap> mFilterBitmaps;
    public void displayFilterImage(Context context, ImageView imageView, int drawableId, FilterManager.FilterType filterType) {

        if(mFilterBitmaps == null){
            mFilterBitmaps = new HashMap<>();
        }

        if(mFilterBitmaps.containsKey(filterType)){
            imageView.setImageBitmap(mFilterBitmaps.get(filterType));
            return;
        }

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            if (VERBOSE) Log.i(TAG,"displayFilterImage() cacheDir == null");
            startFileFilterTask(context, imageView, drawableId, filterType);
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(cacheDir.getPath() + "/" + getFilterImageName(filterType));
        if (bitmap != null) {
            if (VERBOSE) Log.i(TAG,"displayFilterImage() get cache bitmap success, filterType: " + filterType);
            mFilterBitmaps.put(filterType,bitmap);
            imageView.setImageBitmap(bitmap);
            return;
        }

        startFileFilterTask(context, imageView, drawableId, filterType);
    }

    private List<FilterManager.FilterType> mImageFilerStatus;

    private void startFileFilterTask(Context context, ImageView imageView, int drawableId, FilterManager.FilterType filterType) {
        if (mImageFilerStatus == null) {
            mImageFilerStatus = new ArrayList<>();
        }
        if (!mImageFilerStatus.contains(filterType)) {
            if (VERBOSE) Log.i(TAG,"startFileFilterTask(),filterType: " + filterType);
            FilterTask filterTask = new FilterTask(context, imageView, drawableId, filterType);
            filterTask.execute();
        }
    }


    private class FilterTask extends AsyncTask<Void, Void, Bitmap> {

        private Context mContext;
        private ImageView mImageView;
        private int mDrawableId;
        private FilterManager.FilterType mFilterType;
        private ImageRenderer mRenderer;

        public FilterTask(Context context, ImageView imageView, int drawableId, FilterManager.FilterType filterType) {
            mFilterType = filterType;
            mContext = context;
            mDrawableId = drawableId;
            mImageView = imageView;
            mRenderer = new ImageRenderer(context.getApplicationContext(), FilterManager.FilterType.Normal);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), mDrawableId, options);
            ImageEglSurface imageEglSurface = new ImageEglSurface(bitmap.getWidth(), bitmap.getHeight()); //设置输出宽高,
            imageEglSurface.setRenderer(mRenderer);
            mRenderer.changeFilter(mFilterType);
            mRenderer.setImageBitmap(bitmap);
            imageEglSurface.drawFrame();
            Bitmap filterBitmap = imageEglSurface.getBitmap();
            imageEglSurface.release();
            mRenderer.destroy();

            mFilterBitmaps.put(mFilterType,filterBitmap);
            saveToFile(filterBitmap, mContext.getExternalCacheDir(), getFilterImageName(mFilterType));
            return filterBitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mImageView.setImageResource(mDrawableId);
            mImageView.setTag(mFilterType);
            mImageFilerStatus.add(mFilterType);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            mImageFilerStatus.remove(mFilterType);
            if (bitmap == null) {
                return;
            }
            if (mImageView.getTag() == mFilterType) {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

    private String getFilterImageName(FilterManager.FilterType filterType) {
        return "bg_filter_" + filterType + ".png";
    }

    private File saveToFile(Bitmap bitmap, File folder, String fileName) {
        if (bitmap != null && folder != null) {
            if (!folder.exists()) {
                folder.mkdir();
            }
            File file = new File(folder, fileName);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
                BufferedOutputStream e = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, e);
                e.flush();
                e.close();
                return file;
            } catch (IOException var5) {
                var5.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
