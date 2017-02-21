package com.wangw.samples.media.filter.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.wangw.samples.R;


public class FilterManager {

    private static final int mCurveIndex = 5;
    private static int[] mCurveArrays = new int[]{
            R.raw.cross_1, R.raw.cross_2, R.raw.cross_3, R.raw.cross_4, R.raw.cross_5,
            R.raw.cross_6, R.raw.cross_7, R.raw.cross_8, R.raw.cross_9, R.raw.cross_10,
            R.raw.cross_11,
    };

    private FilterManager() {
    }

    private static Bitmap mBlendBitmap;

    public static void setBlendBitmap(Bitmap bitmap) {
        mBlendBitmap = bitmap;
    }

    public static Bitmap getSoftLightBitmap(Context context, int drawableId) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;    // No pre-scaling
        return BitmapFactory.decodeResource(context.getResources(), drawableId, options);
    }

    public static IFilter getCameraFilter(FilterType filterType, Context context) {
        Log.i("zl", "getCameraFilter filterType: " + filterType);
        switch (filterType) {
            case ToneCurve://色调曲线,mCurveIndex == 5,取滤镜名 阳光
                return new CameraFilterToneCurve(context,
                        context.getResources().openRawResource(mCurveArrays[mCurveIndex]));
            case SoftLight://柔和叠加
                return new CameraFilterBlendSoftLight(context, getSoftLightBitmap(context, R.drawable.mask));
            case Blend://叠加
                if (mBlendBitmap != null) {
                    return new CameraFilterBlend(context, mBlendBitmap);
                } else {
                    return new CameraFilter(context);
                }
            case Beauty:
                return new CameraFilterBeauty(context, 5);
            case SkinWhite:
                return new CameraFilterWhite(context);
            case Antique:
                return new CameraFilterAntique(context);
            case Nostalgia:
                return new CameraFilterNostalgia(context);
            case COOL:
                return new CameraFilterCool(context);
            case CALM:
                return new CameraFilterCalm(context);
            case WARM:
                return new CameraFilterWarm(context);
            case TENDER:
                return new CameraFilterTender(context);
            case Healthy:
                return new CameraFilterHealthy(context);
            case Normal:
            default:
                return new CameraFilter(context);
        }
    }

    public static IFilter getImageFilter(FilterType filterType, Context context) {
        switch (filterType) {
            case ToneCurve://色调曲线,mCurveIndex == 5,取滤镜名 阳光
                return new CameraFilterToneCurve(context,
                        context.getResources().openRawResource(mCurveArrays[mCurveIndex]), false);
            case SoftLight://柔和叠加
                return new CameraFilterBlendSoftLight(context, getSoftLightBitmap(context, R.drawable.mask), false);
            case Blend://叠加
                if (mBlendBitmap != null) {
                    return new CameraFilterBlend(context, mBlendBitmap, false);
                } else {
                    return new CameraFilter(context, false);
                }
            case Beauty:
                return new CameraFilterBeauty(context, 5, false);
            case SkinWhite:
                return new CameraFilterWhite(context, false);
            case Antique:
                return new CameraFilterAntique(context, false);
            case Nostalgia:
                return new CameraFilterNostalgia(context, false);
            case COOL:
                return new CameraFilterCool(context, false);
            case CALM:
                return new CameraFilterCalm(context, false);
            case WARM:
                return new CameraFilterWarm(context, false);
            case TENDER:
                return new CameraFilterTender(context, false);
            case Healthy:
                return new CameraFilterHealthy(context, false);
            case Normal:
            default:
                return new CameraFilter(context, false);

        }
    }

    public enum FilterType {
        Normal, ToneCurve, SoftLight, Blend, Beauty, SkinWhite, Antique, Nostalgia, COOL, CALM, WARM, TENDER, Healthy
    }
}
