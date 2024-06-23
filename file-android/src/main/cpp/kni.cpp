#include <jni.h>
#include <string>
#include <android/log.h>
#include "jnis.h"
#include "KeepShellPublic.h"
#include "files.h"

#define LOG_TAG "System.out"

extern "C" JNIEXPORT jstring JNICALL
Java_io_github_lumkit_io_jni_LintJni_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_github_lumkit_io_jni_LintJni_cmd(JNIEnv *env, jobject thiz, jstring cmd) {
    char *cmdChar = jstringToChar(env, cmd);
    long ctm = currentTimeMillis();
    std::string result = doCmdSync(cmdChar);
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "耗时：%ld", currentTimeMillis() - ctm);
    return charToJstring(env, result.c_str());
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_jni_LintJni_exists(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    bool exist = exists(cPath);
    return static_cast<jboolean>(exist);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_lumkit_io_jni_LintJni_length(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    long size = length(cPath);
    return static_cast<jlong>(size);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_lumkit_io_jni_LintJni_lastModified(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    long last = lastModified(cPath);
    return static_cast<jlong>(last);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_jni_LintJni_createNewFile(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    bool isCreate = createNewFile(cPath);
    return static_cast<jboolean>(isCreate);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_jni_LintJni_checkRoot(JNIEnv *env, jobject thiz) {
    return static_cast<jboolean>(chockRoot());
}