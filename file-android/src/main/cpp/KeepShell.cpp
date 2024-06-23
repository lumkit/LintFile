//
// Created by 22059 on 2024/6/21.
//

#include <cstring>
#include "KeepShell.h"
#include "jnis.h"

KeepShell::KeepShell(bool isRoot) : pipe(nullptr) {
    this->isRoot = isRoot;
}

KeepShell::~KeepShell() {
    tryExit();
}

bool KeepShell::isIdle() {
    return this->currentIsIdle;
}

void KeepShell::tryExit() {
    if (pipe) {
        pclose(pipe);
    }
    enterLockTime = 0;
    currentIsIdle = true;
}

void KeepShell::createProcess() {
    if (!pipe) {
        pipe = popen(isRoot ? "su" : "sh", "w+");
        enterLockTime = currentTimeMillis();
    }
}

std::string KeepShell::doCmdSync(const std::string &cmd) {
    if (mLock && enterLockTime > 0 && currentTimeMillis() - enterLockTime > LOCK_TIMEOUT) {
        tryExit();
    }
    createProcess();
    try {
        mLock = true;
        currentIsIdle = false;

        std::array<char, 128> buffer{};
        std::string result;
        const std::string end_marker = "END_OF_COMMAND_OUTPUT";

        fprintf(pipe, "%s; echo %s\n", cmd.c_str(), end_marker.c_str());
        fflush(pipe);

        while (fgets(buffer.data(), buffer.size(), pipe) != nullptr) {
            std::string line(buffer.data());
            if (line.find(end_marker) != std::string::npos) {
                break;
            }
            result += line;
        }

        enterLockTime = 0L;
        mLock = false;
        currentIsIdle = true;

        return result;
    }catch (const std::exception& e) {
        tryExit();
        enterLockTime = 0L;
        mLock = false;
        currentIsIdle = true;
        return "error";
    }
}

bool KeepShell::chockRoot() {
    std::string result = trimLine(doCmdSync(checkRoot));
    return result == "success";
}
