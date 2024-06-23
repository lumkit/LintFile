package io.github.lumkit.io.impl

import io.github.lumkit.io.LintFile
import java.io.File

class SuFile: LintFile {

    companion object {
        init {
            System.loadLibrary("shell-file")
        }
    }

    private external fun exists(path: String): Boolean
    private external fun canRead(path: String): Boolean
    private external fun canWrite(path: String): Boolean
    private external fun isDirectory(path: String): Boolean
    private external fun isFile(path: String): Boolean
    private external fun lastModified(path: String): Long
    private external fun length(path: String): Long
    private external fun createNewFile(path: String): Boolean
    private external fun deletePath(path: String): Boolean
    private external fun mkdirs(path: String): Boolean
    private external fun renameTo(path: String, dest: String): Boolean
    private external fun list(path: String): Array<String>?

    constructor(path: String): super(path)
    constructor(file: LintFile): super(file)
    constructor(file: LintFile, child: String): super(file, child)

    internal val suFile by lazy {
        com.topjohnwu.superuser.io.SuFile(path)
    }

    override fun exists(): Boolean = suFile.exists()

    override fun getParent(): String = _file.parent ?: ""

    override fun getParentFile(): LintFile = SuFile(getParent())

    override fun canRead(): Boolean = suFile.canRead()

    override fun canWrite(): Boolean = suFile.canWrite()

    override fun isDirectory(): Boolean = suFile.isDirectory

    override fun isFile(): Boolean = suFile.isFile

    override fun lastModified(): Long = suFile.lastModified()

    override fun length(): Long = suFile.length()

    override fun createNewFile(): Boolean = suFile.createNewFile()

    override fun delete(): Boolean = suFile.delete()

    override fun list(): Array<String> = suFile.list()?.map { "$path${File.separator}$it" }?.toTypedArray() ?: arrayOf()

    override fun list(filter: (String) -> Boolean): Array<String> =
        list().filter { filter(it) }.toTypedArray()

    override fun listFiles(): Array<LintFile> =
        list().map { SuFile(it) }.toTypedArray()

    override fun listFiles(filter: (LintFile) -> Boolean): Array<LintFile> =
        listFiles().filter { filter(it) }.toTypedArray()

    override fun mkdirs(): Boolean = suFile.mkdirs()

    override fun renameTo(dest: String): Boolean = suFile.renameTo(File(getParent(), dest))

}