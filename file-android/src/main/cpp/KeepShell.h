//
// Created by 22059 on 2024/6/21.
//

#ifndef LINT_FILE_KEEPSHELL_H
#define LINT_FILE_KEEPSHELL_H


#include <cstdio>
#include <jni.h>
#include <string>


class KeepShell {
private:
    long LOCK_TIMEOUT = 10000L;

    bool isRoot = true;

public:

    KeepShell(bool isRoot = true);

    ~KeepShell();

private:
    FILE *pipe;
private:
    bool currentIsIdle = true; // 是否处于闲置状态
private:
    bool mLock = false;
private:
    long enterLockTime = 0L;
public:
    bool isIdle();

private:
    const std::string checkRoot = "if [[ $(id -u 2>&1) == '0' ]] || [[ $($UID) == '0' ]] || [[ $(whoami 2>&1) == 'root' ]] || [[ $(set | grep 'USER_ID=0') == 'USER_ID=0' ]]; then\n  echo 'success'\nelse\nif [[ -d /cache ]]; then\necho 1 > /cache/yohub_root\nif [[ -f /cache/yohub_root ]] && [[ $(cat /cache/yohub_root) == '1' ]]; then\necho 'success'\nrm -rf /cache/yohub_root\nreturn\nfi\nfi\nexit 1\nexit 1\nfi\n";

public:
    void tryExit();

    void createProcess();

    std::string doCmdSync(const std::string &cmd);

    bool chockRoot();
};

#endif //LINT_FILE_KEEPSHELL_H
