package io.github.lumkit.io

import java.io.File
import java.io.IOException

abstract class LintFile : Comparator<LintFile> {

    internal val _file: File

    constructor(path: String) {
        this._file = File(path.pathHandle())
    }
    constructor(file: LintFile) {
        this._file = file._file
    }
    constructor(file: LintFile, child: String) {
        this._file = File(file._file, child.pathHandle())
    }

    val path: String
        get() = _file.path
    val name: String
        get() = _file.name

    abstract fun exists(): Boolean
    abstract fun getParent(): String
    abstract fun getParentFile(): LintFile
    abstract fun canRead(): Boolean
    abstract fun canWrite(): Boolean
    abstract fun isDirectory(): Boolean
    abstract fun isFile(): Boolean
    abstract fun lastModified(): Long
    abstract fun length(): Long

    @Throws(IOException::class)
    abstract fun createNewFile(): Boolean

    abstract fun delete(): Boolean
    abstract fun list(): Array<String>
    abstract fun list(filter: (String) -> Boolean): Array<String>
    abstract fun listFiles(): Array<LintFile>
    abstract fun listFiles(filter: (LintFile) -> Boolean): Array<LintFile>
    abstract fun mkdirs(): Boolean
    abstract fun renameTo(dest: String): Boolean
    override fun compare(o1: LintFile, o2: LintFile): Int = o1._file.compareTo(o2._file)
}