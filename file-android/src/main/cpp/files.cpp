//
// Created by 22059 on 2024/6/21.
//

#include <android/log.h>
#include "files.h"
#include "KeepShellPublic.h"
#include "jnis.h"

#define LOG_TAG "System.out"

ShellFile::ShellFile() = default;

ShellFile::~ShellFile() = default;

ShellFile shellFileInfo(const std::string& parent, const std::string& row) {
    std::istringstream iss(row);
    ShellFile file;
    std::string sizeStr, date, timeOrYear;
    file.parent = parent;
    iss >> file.permissions >> file.links >> file.owner >> file.group >> sizeStr;
    try {
        file.size = std::stol(sizeStr);
    } catch (const std::exception& e) {
        file.size = -1;  // 设置为-1表示转换失败
    }

    iss >> date >> timeOrYear;
    file.time = date + " " + timeOrYear;

    // 读取文件名和符号链接目标
    std::getline(iss, file.name);
    file.name = trimLine(file.name);

    // 处理符号链接目标
    size_t linkPos = file.name.find(" -> ");
    if (linkPos != std::string::npos) {
        file.link_target = file.name.substr(linkPos + 4);
        file.name = file.name.substr(0, linkPos);
    } else {
        file.link_target = "";
    }
    return file;
}

std::vector<ShellFile> shellFileInfoRow(const std::string& parent, const std::string &input) {
    std::istringstream iss(input);
    std::vector<ShellFile> attributes;
    std::string line;

    // 按行读取输入
    while (std::getline(iss, line)) {
        if (!line.empty() && line.find_first_of("total") != 0) {
            attributes.push_back(shellFileInfo(parent, line));
        }
    }

    return attributes;
}

// 获取父目录路径的辅助函数
std::string parentPath(const std::string& path) {
    std::string dirPath = path;
    size_t lastSlash = dirPath.find_last_of('/');
    if (lastSlash != std::string::npos) {
        return dirPath.substr(0, lastSlash);
    } else {
        return "";
    }
}

std::vector<ShellFile> listFiles(const std::string &path) {
    std::string cmd = "ls -l " + path;
    std::string result = doCmdSync(cmd);
    return shellFileInfoRow(path, result);
}

bool exists(const std::string& path) {
    const std::string cmd = "[ -e \"" + path + "\" ] && echo 1 || echo 0";
    std::string result = doCmdSync(cmd);
    return result.find('1') != std::string::npos;
}

bool canRead(const std::string& path) {
    const std::string cmd = "[ -r \"" + path + "\" ] && echo 1 || echo 0";
    std::string result = doCmdSync(cmd);
    return result.find('1') != std::string::npos;
}

bool canWrite(const std::string& path) {
    const std::string cmd = "[ -w \"" + path + "\" ] && echo 1 || echo 0";
    std::string result = doCmdSync(cmd);
    return result.find('1') != std::string::npos;
}

bool isDirectory(const std::string& path) {
    const std::string cmd = "[ -d \"" + path + "\" ] && echo 1 || echo 0";
    std::string result = doCmdSync(cmd);
    return result.find('1') != std::string::npos;
}

bool isFile(const std::string& path) {
    const std::string cmd = "[ -f \"" + path + "\" ] && echo 1 || echo 0";
    std::string result = doCmdSync(cmd);
    return result.find('1') != std::string::npos;
}

long lastModified(const std::string& path) {
    const std::string cmd = "[[ -e \"" + path + "\" ]] && stat -c %Y \"" + path + "\" || echo 0";
    std::string result = doCmdSync(cmd);
    try {
        return std::stol(trimLine(result)) * 1000L;
    } catch (const std::exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Error converting last modified time: %s", e.what());
        return -1;
    }
}

long length(const std::string& path) {
    const std::string cmd = "[[ -e \"" + path + "\" ]] && du -sb \"" + path + "\" | cut -f1 || echo 0";
    std::string result = doCmdSync(cmd);
    try {
        return std::stol(trimLine(result));
    } catch (const std::exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Error converting path size: %s", e.what());
        return -1;
    }
}

bool createNewFile(const std::string& path) {
    const std::string cmd = "touch \"" + path + "\"";
    std::string result = doCmdSync(cmd);
    // 检查文件是否已成功创建
    const std::string checkCmd = "[ -e \"" + path + "\" ] && echo 1 || echo 0";
    std::string checkResult = doCmdSync(checkCmd);
    return checkResult.find('1') != std::string::npos;
}

bool _delete(const std::string& path) {
    const std::string cmd = "rm -rf \"" + path + "\"";
    std::string result = doCmdSync(cmd);
    // 检查文件或目录是否已成功删除
    const std::string checkCmd = "[ ! -e \"" + path + "\" ] && echo 1 || echo 0";
    std::string checkResult = doCmdSync(checkCmd);
    return checkResult.find('1') != std::string::npos;
}

bool mkdirs(const std::string& path) {
    const std::string cmd = "mkdir -p \"" + path + "\"";
    std::string result = doCmdSync(cmd);
    // 检查目录是否已成功创建
    const std::string checkCmd = "[ -d \"" + path + "\" ] && echo 1 || echo 0";
    std::string checkResult = doCmdSync(checkCmd);
    return checkResult.find('1') != std::string::npos;
}

bool renameTo(const std::string& path, const std::string &dest) {
    try {
        std::string parent = parentPath(parent);
        std::string newPath = parent + "/" + dest;
        const std::string cmd = "mv \"" + path + "\" \"" + newPath + "\"";
        std::string result = doCmdSync(cmd);
        // 检查重命名是否成功
        const std::string checkCmd = "[ -e \"" + newPath + "\" ] && echo 1 || echo 0";
        std::string checkResult = doCmdSync(checkCmd);
        return checkResult.find('1') != std::string::npos;
    }catch (const std::exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "%s", e.what());
        return false;
    }
}

std::string ShellFile::getPath() const {
    return this->parent + "/" + this->name;
}
