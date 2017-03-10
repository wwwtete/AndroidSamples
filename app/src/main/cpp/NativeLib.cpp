#include <jni.h>
#include <stdio.h>
//引用Android log库
//#include <android/log.h>
#include "Log.h"
//
// Created by wangw on 2017/3/4.
//

// __cplusplus是cpp中的自定义宏定义，表示这一段cpp的代码，
// 也就是说如果这是一段cpp的代码，那么就加入到extern "C"{和}处理其中的代码
//C和C++对函数的处理方式是不同的.extern "C"是使C++能够调用C写作的库文件的一个手段，如果要对编译器提示使用C的方式来处理函数的话，那么就要使用extern "C"来说明

#ifdef __cplusplus
extern "C" {
#endif

//定义一个结构体
struct StructInfo{
    char info[256];
    long size;
};

//定义结构体对象
static StructInfo gInfo={"JNIStruct",2000};


JNIEXPORT jobject JNICALL
Java_com_wangw_samples_jni_NativeMethods_getStructInfo(JNIEnv *env, jclass type) {

    //获取java中与结构体相对于的类
    jclass objClass = env->FindClass("com/wangw/samples/jni/StructInfo");
    //获取java类中的字段Id
    jfieldID infoId = env->GetFieldID(objClass,"info","Ljava/lang/String;");
    jfieldID sizeId = env->GetFieldID(objClass,"size","J");

    jstring infoStr = env->NewStringUTF(gInfo.info);
    jlong sizeV = gInfo.size;

    //获取Java对象的构造函数
    jmethodID initMId = env->GetMethodID(objClass,"<init>","()V");
    //通过构造函数Id创建Java对象
    jobject javaBean = env->NewObject(objClass,initMId);
    //给新建的java对象赋值
    env->SetObjectField(javaBean,infoId,infoStr);
    env->SetLongField(javaBean,sizeId,sizeV);
    return javaBean;
}

JNIEXPORT jobject JNICALL
Java_com_wangw_samples_jni_NativeMethods_persionFromJni(JNIEnv *env, jclass type, jobject bean) {

    //通过FindClass函数查找要操作的Class对象
//    jclass objClass = env->FindClass("com/wangw/samples/jni/PersionBean");
    //也可以通过GetObjectClass方法获取java Class对象
    jclass objClass = env->GetObjectClass(bean);
    //通过GetFieldID函数查找字段的ID，需要注意：只能查找public字段
    //第三个参数代表字段的类型，可以从网上搜索"类型签名"来查找对应的签名
    jfieldID nameId = env->GetFieldID(objClass,"name","Ljava/lang/String;"); //Ljava/lang/String表示java中的字符串
    jfieldID idId = env->GetFieldID(objClass,"id","I"); //I表示java中的int类型

    jobject obj = env->GetObjectField(bean,nameId);
    const char *nameV = env->GetStringUTFChars((jstring)obj,JNI_FALSE);
    int idV = (int)env->GetIntField(bean,idId);
    LOG("FromJavaObject: id=%d | name=%s",idV,nameV);

    //通过Set{type}Field方法给Object赋值
    env->SetObjectField(bean,nameId,env->NewStringUTF("JNI Update"));
    env->SetIntField(bean,idId,2);

    //创建一个新的Java对象
    //可以通过GetFieldID(class,"<init>","(参数列表)V")函数来获取java对象的构造函数Id
    //构造类型的方法防止永远都是Void
    //如果没有无参的构造函数，则第三个参数中必须传入对应的参数类型
    jmethodID initMId = env->GetMethodID(objClass,"<init>","(Ljava/lang/String;I)V");
    //通过构造函数Id来创建一个Java对象
    //如果要构建的java对象有无参的构造函数可以不用传参，但是如果没有无参构造函数则必须按照java定义的有参构造函数进行赋值
    jobject newBean = env->NewObject(objClass,initMId,env->NewStringUTF("JNI NewPerson"),1);
    return newBean;
}

JNIEXPORT jobjectArray JNICALL
Java_com_wangw_samples_jni_NativeMethods_stringArrayFromJni(JNIEnv *env, jclass type,
                                                            jobjectArray arr) {
    //获取数组长度
    int len = env->GetArrayLength(arr);
    for (int i = 0; i < len; ++i) {
        //根据index获取String数组元素
        jobject  obj = env->GetObjectArrayElement(arr,i);
        //转换成字符串
        jstring str = (jstring)obj;
        //转换成一个UTF-8编码的字符串
        const char *szStr = env->GetStringUTFChars(str,JNI_FALSE);
        LOG("fromJAVA元素%d = %s",i,szStr);
        //释放引用
        env->ReleaseStringUTFChars(str,szStr);
    }

    jstring str;
    jsize size = 3;
    const char *sa[] = {"Hello","JNI","StringArr"};
    jclass elementClass = env->FindClass("java/lang/String");
    //构建一个新的指定的Class类型的数组，元素初始值为0
    jobjectArray args = env->NewObjectArray(size,elementClass,0);
    for (int i = 0; i < size; ++i) {
        //将char数组转换成Java类型的jstring
        str = env->NewStringUTF(sa[i]);
        //给数组赋值
        env->SetObjectArrayElement(args,i,str);
    }
    //返回给JAVA
    return args;
}

JNIEXPORT jint JNICALL
Java_com_wangw_samples_jni_NativeMethods_intArrayToJni(JNIEnv *env, jclass type, jintArray arr_) {
    //获取数组长度
    jsize len = env->GetArrayLength(arr_);
    //获取一个指向数组元素的的指针
    jint *content = env->GetIntArrayElements(arr_,JNI_FALSE);
    //必须为定义的变量赋初始值，否则直接使用会出现错误
    int sum=0;
    for (int i = 0; i < len; ++i) {
        int num = (int)content[i];
        sum += num;
        LOG("数组元素 %d = %d",i, num);
    }
    LOG("int数组总和 =%d",sum);
    //释放数组引用
    env->ReleaseIntArrayElements(arr_,content,0);
    return (jint)sum;
}

JNIEXPORT jfloat JNICALL
Java_com_wangw_samples_jni_NativeMethods_sum(JNIEnv *env, jclass type, jfloat num1, jfloat num2) {
    LOG(__func__);
    float sum1F = num1;
    float sum2F = num2;
    LOG("%f + %f",sum1F,sum2F);
    float sumM= sum1F + sum2F;
    LOG("sum = %f",sumM);
    return (jfloat)sumM;

}

JNIEXPORT void JNICALL
Java_com_wangw_samples_jni_NativeMethods_stringToJni(JNIEnv *env, jclass type, jstring string_) {
    //返回指向字符串的UTF-8字符串数组指针，该数组在被ReleaseStringUTFChars()释放前一直有效
    const char *string = env->GetStringUTFChars(string_,JNI_FALSE);

    LOG("从Java传递过来的字符串: %s", string);
    //释放字符串
    env->ReleaseStringUTFChars(string_, string);
}


/**
 *
 */
JNIEXPORT jstring JNICALL
Java_com_wangw_samples_jni_NativeMethods_hellowWord(JNIEnv *env, jclass type) {

    //创建一个字符串返给Java
    return env->NewStringUTF("Hello From C++");
}



#ifdef __cplusplus
}
#endif