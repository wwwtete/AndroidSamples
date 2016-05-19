package com.wangw.samples.utils;

import android.content.Context;
import android.widget.Toast;

import com.exlogcat.L;
import com.wangw.samples.SamplesApplication;

/**
 * Toast统一管理类
 * 
 * 
 */
public class T {
	// Toast
	private static Toast toast;
	
	public static void showShort(CharSequence message) {
		showToast(message);
	}

	public static void showShort(int message) {
		showToast(message);
	}

	public static void showLong(CharSequence message) {
		showToast(message, Toast.LENGTH_LONG);
	}


	public static void showLong(int message) {
		showToast(message, Toast.LENGTH_LONG);
	}


	/** Hide the toast, if any. */
	public static void hideToast() {
		if (null != toast) {
			toast.cancel();
		}
	}


	public static void showToast(CharSequence message,int duration){
		if (null == toast) {
			toast = Toast.makeText(getContentx(), message, duration);
			// toast.setGravity(Gravity.CENTER, 0, 0);
		} else {
			toast.setText(message);
			toast.setDuration(duration);
		}
		toast.show();
	}

	public static void showToast(int resId,int duration){
		if (null == toast) {
			toast = Toast.makeText(getContentx(), resId, duration);
		} else {
			toast.setText(resId);
			toast.setDuration(duration);
		}
		toast.show();
	}

	public static void showToast(int resId){
		showToast(resId, Toast.LENGTH_SHORT);
	}

	public static void showToast(CharSequence msg){
		showToast(msg, Toast.LENGTH_SHORT);
	}

	private static Context getContentx(){
		return SamplesApplication.getInstance();
	}

}
