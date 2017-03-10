# 要使用CMake构建so库，必须包含cmake_minimum_required 和 add_library 命令行脚本
# 参考文档: http://www.cnblogs.com/coderfenghc/archive/2012/06/23/2559603.html
#设置CMake最低版本号
cmake_minimum_required(VERSION 3.4.1)

#指定so库的name，指定so库类型:STATIC(静态库) 或 SHARED(共享库) 或 MODULE(动态链接程序库，不在编译期进行连结，而是等到执行期才透过 dlopen() 或 LoadLibrary() 方式调用。一般情况下 MODULE 输出的档案类型也是 so 或 dll)
#设置C/C++源码的相对路径
#可以通过add_libarary(name,类型,源码相对路径)命令来添加多个so库
#当Gradle构建项目时会自动打包到APK中，可以通过Build > Analyze APK工具来分析so库是否打包进去


add_library( # 设置so库Name
            native-lib

            # 设置类型为SHARED
            SHARED

            # 设置源码的相对路径
            # 如果cpp文件和头文件在同一目录则会自动关联头文件
            # 可以设置多个源文件
            src/main/cpp/NativeLib.cpp
            src/main/cpp/NativeLib2.cpp)

# =============以下的配置为可选配置，按需添加================

#为确保CMake可以在编译时定位头文件，可以使用include_directories()命令指定头文件的路径
#include_directories("头文件目录")

#使用find_library("定义一个变量名" "需要引用的so库名称")命令来引用Android NDK提供的一些API
#以下示例是定位Andorid特定的日志库并将其存储在log-lib变量中:
find_library( # 定义一个变量名存储要查找的库路径
                log-lib

                # 指定CMake需要定位的NDK库名称
                log)
#为确保原生库可以调用log库中的函数，需要使用target_link_libraries("目标so的名称" "${定义的变量名}")命令来关联库
target_link_libraries( # 指定目标库
                        native-lib

                        # 将刚才定义的变量关联到目标库
                        ${log-lib})



