//
// Created by 22059 on 2024/6/21.
//

#ifndef LINT_FILE_KEEPSHELLPUBLIC_H
#define LINT_FILE_KEEPSHELLPUBLIC_H

#include "KeepShell.h"
#include <string>

KeepShell getDefaultInstance();
std::string doCmdSync(const std::string &cmd);
bool chockRoot();
#endif //LINT_FILE_KEEPSHELLPUBLIC_H
