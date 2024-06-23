//
// Created by 22059 on 2024/6/21.
//

#include "jnis.h"

#include <jni.h>
#include <iostream>

// 将char*转换为jstring
jstring charToJstring(JNIEnv* env, const char* pat) {
    jclass strClass = env->FindClass("java/lang/String");
    jmethodID ctorID = env->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = env->NewByteArray(strlen(pat));
    env->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*)pat);
    jstring encoding = env->NewStringUTF("UTF-8");
    jstring result = (jstring) env->NewObject(strClass, ctorID, bytes, encoding);
    env->DeleteLocalRef(bytes);
    env->DeleteLocalRef(encoding);
    return result;
}

// 将jstring转换为char*
char* jstringToChar(JNIEnv* env, jstring jstr) {
    const char* cstr = env->GetStringUTFChars(jstr, NULL);
    if (cstr == NULL) {
        return NULL; // JVM 抛出异常
    }
    char* result = strdup(cstr);
    env->ReleaseStringUTFChars(jstr, cstr);
    return result;
}

long currentTimeMillis() {
    // 获取当前时间点
    auto now = std::chrono::system_clock::now();

    // 获取自纪元以来的毫秒数
    auto duration = now.time_since_epoch();
    auto milliseconds = std::chrono::duration_cast<std::chrono::milliseconds>(duration).count();

    // 将毫秒数转换为long类型
    long timestamp = static_cast<long>(milliseconds);
    return timestamp;
}

// 辅助函数，用于判断是否是空格或回车
bool isSpaceOrNewline(char c) {
    return c == ' ' || c == '\n' || c == '\r' || c == '\t';
}

// 去除字符串中的空格和回车
std::string trimLine(const std::string& str) {
    size_t start = str.find_first_not_of(" \t\n\r");
    size_t end = str.find_last_not_of(" \t\n\r");
    return (start == std::string::npos) ? "" : str.substr(start, end - start + 1);
}