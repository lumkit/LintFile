//
// Created by 22059 on 2024/6/21.
//

#ifndef LINT_FILE_FILES_H
#define LINT_FILE_FILES_H

#include <iostream>
#include <string>
#include <sstream>
#include <vector>

class ShellFile {

public:
    ShellFile();

    ~ShellFile();

    std::string parent;
    std::string permissions;
    int links{};
    std::string owner;
    std::string group;
    int size{};
    std::string date;
    std::string time;
    std::string name;
    std::string link_target;
    std::string getPath() const;
};

ShellFile shellFileInfo(const std::string& parent, const std::string& row);
std::vector<ShellFile> shellFileInfoRow(const std::string& parent, const std::string& input);
std::vector<ShellFile> listFiles(const std::string& path);
bool exists(const std::string& path);
bool canRead(const std::string& path);
bool canWrite(const std::string& path);
bool isDirectory(const std::string& path);
bool isFile(const std::string& path);
long lastModified(const std::string& path);
long length(const std::string& path);
bool createNewFile(const std::string& path);
bool _delete(const std::string& path);
bool mkdirs(const std::string& path);
bool renameTo(const std::string& path, const std::string& dest);

#endif //LINT_FILE_FILES_H
