//
// Created by 22059 on 2024/6/21.
//

#ifndef LINT_FILE_JNIS_H
#define LINT_FILE_JNIS_H

#include <jni.h>
#include <string>

jstring charToJstring(JNIEnv* env, const char* pat);
char* jstringToChar(JNIEnv* env, jstring jstr);
long currentTimeMillis();
// 去除字符串中的空格和回车
std::string trimLine(const std::string &input);
#endif //LINT_FILE_JNIS_H
