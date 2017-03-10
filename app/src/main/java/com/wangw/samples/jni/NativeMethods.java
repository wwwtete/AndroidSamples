package com.wangw.samples.jni;

/**
 * Created by wangw on 2017/3/4.
 */

public class NativeMethods {

    static {
        //加载so库
        System.loadLibrary("native-lib");
    }

    /**
     * 从JNI中获取字符串
     * @return
     */
    public static native String hellowWord();

    /**
     * 将String传递到JNI
     * @param string
     */
    public static native void stringToJni(String string);

    public static native float sum(float num1,float num2);

    public static native int intArrayToJni(int[] arr);

    /**
     * 传一个字符串数组给JNI，
     * JNI返回一个字符串数组给JAVA
     * @param arr
     * @return
     */
    public static native String[] stringArrayFromJni(String[] arr);

    /**
     * 传一个java对象给JNI
     * JNI创建一个新的Java对象赋值并返回
     * @param bean
     * @return
     */
    public static native PersionBean persionFromJni(PersionBean bean);

    /**
     * 从JNI返回一个结构体
     * @return
     */
    public static native StructInfo getStructInfo();


}
