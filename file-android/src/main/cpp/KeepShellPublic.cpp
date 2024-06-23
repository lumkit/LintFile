//
// Created by 22059 on 2024/6/21.
//

#include "KeepShell.h"
#include "KeepShellPublic.h"


KeepShell defaultKeepShell = KeepShell();
KeepShell secondaryKeepShell = KeepShell();

KeepShell getDefaultInstance() {
    if (defaultKeepShell.isIdle() || !secondaryKeepShell.isIdle()) {
        return defaultKeepShell;
    } else {
        return secondaryKeepShell;
    }
}

std::string doCmdSync(const std::string &cmd) {
    return getDefaultInstance().doCmdSync(cmd);
}

bool chockRoot() {
    return getDefaultInstance().chockRoot();
}

