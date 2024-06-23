#include <jni.h>
#include <android/log.h>
#include "jnis.h"
#include "files.h"

//
// Created by 22059 on 2024/6/22.
//
#define LOG_TAG "System.out"

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_impl_SuFile_exists(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    bool ex = exists(cPath);
    return static_cast<jboolean>(ex);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_impl_SuFile_canRead(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    bool cRead = canRead(cPath);
    return static_cast<jboolean>(cRead);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_impl_SuFile_canWrite(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    bool cWrite = canWrite(cPath);
    return static_cast<jboolean>(cWrite);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_impl_SuFile_isDirectory(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    bool isD = isDirectory(cPath);
    return static_cast<jboolean>(isD);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_impl_SuFile_isFile(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    bool isF = isFile(cPath);
    return static_cast<jboolean>(isF);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_lumkit_io_impl_SuFile_lastModified(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    return static_cast<jlong>(lastModified(cPath));
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_lumkit_io_impl_SuFile_length(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    return static_cast<jlong>(length(cPath));
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_impl_SuFile_createNewFile(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    return static_cast<jboolean>(createNewFile(cPath));
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_impl_SuFile_deletePath(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    return static_cast<jboolean>(_delete(cPath));
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_impl_SuFile_mkdirs(JNIEnv *env, jobject thiz, jstring path) {
    char *cPath = jstringToChar(env, path);
    return static_cast<jboolean>(mkdirs(cPath));
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_lumkit_io_impl_SuFile_renameTo(JNIEnv *env, jobject thiz, jstring path,
                                              jstring dest) {
    char *cPath = jstringToChar(env, path);
    char *cDest = jstringToChar(env, dest);
    return static_cast<jboolean>(renameTo(cPath, cDest));
}
extern "C"
JNIEXPORT jobjectArray JNICALL
Java_io_github_lumkit_io_impl_SuFile_list(JNIEnv *env, jobject thiz, jstring path) {
    jclass stringClass = env->FindClass("java/lang/String");
    char *cPath = jstringToChar(env, path);
    try {
        std::vector<ShellFile> shellFiles = listFiles(cPath);
        jobjectArray list = env->NewObjectArray(shellFiles.size(), stringClass, nullptr);
        for (int i = 0; i < shellFiles.size(); i++) {
            jstring jPath = charToJstring(env, shellFiles[i].getPath().c_str());
            env->SetObjectArrayElement(list, i, jPath);
            env->DeleteLocalRef(jPath);
        }
        env->DeleteLocalRef(stringClass);
        return list;
    }catch (const std::exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "%s", e.what());
        return nullptr;
    }
}