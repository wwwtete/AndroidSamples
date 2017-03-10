//
// Created by wangw on 2017/3/6.
//

//引用Android log库
#include <android/log.h>
//定义一个宏来保存 TAG
#define TAG "JNILOG"

//定义一个宏定义 来调用Android的输出log方法
#define LOG(...)    __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

