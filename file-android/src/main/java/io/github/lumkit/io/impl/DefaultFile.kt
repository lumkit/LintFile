package io.github.lumkit.io.impl

import io.github.lumkit.io.LintFile
import java.io.File

/**
 * java io file system
 */
class DefaultFile : LintFile {

    constructor(path: String): super(path)
    constructor(file: LintFile): super(file)
    constructor(file: LintFile, child: String): super(file, child)

    override fun exists(): Boolean = _file.exists()

    override fun getParent(): String = _file.parent ?: ""

    override fun getParentFile(): LintFile = DefaultFile(getParent())

    override fun canRead(): Boolean = _file.canRead()

    override fun canWrite(): Boolean = _file.canWrite()

    override fun isDirectory(): Boolean = _file.isDirectory

    override fun isFile(): Boolean = _file.isFile

    override fun lastModified(): Long = _file.lastModified()

    override fun length(): Long = _file.length()

    override fun createNewFile(): Boolean = _file.createNewFile()

    override fun delete(): Boolean = _file.delete()

    override fun list(): Array<String> = _file.list()?.map {
        "$path${File.separator}$it"
    }?.toTypedArray() ?: arrayOf()

    override fun list(filter: (String) -> Boolean): Array<String> =
        _file.list { dir, name -> filter("${dir}${File.separator}$name") }?.map {
            "${path}${File.separator}$it"
        }?.toTypedArray() ?: arrayOf()

    override fun listFiles(): Array<LintFile> =
        _file.listFiles()?.map {
            DefaultFile(it.absolutePath)
        }?.toTypedArray() ?: arrayOf()

    override fun listFiles(filter: (LintFile) -> Boolean): Array<LintFile>  =
        _file.listFiles { file ->
            filter(DefaultFile(file.absolutePath))
        }?.map {
            DefaultFile(it.absolutePath)
        }?.toTypedArray() ?: arrayOf()

    override fun mkdirs(): Boolean = _file.mkdirs()

    override fun renameTo(dest: String): Boolean = _file.renameTo(File(dest))

}