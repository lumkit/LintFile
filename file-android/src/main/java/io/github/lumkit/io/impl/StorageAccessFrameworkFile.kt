package io.github.lumkit.io.impl

import androidx.documentfile.provider.DocumentFile
import io.github.lumkit.io.LintFile
import io.github.lumkit.io.LintFileConfiguration
import io.github.lumkit.io.absolutePath
import io.github.lumkit.io.androidPath
import io.github.lumkit.io.documentFileUri
import io.github.lumkit.io.primaryChildPath
import io.github.lumkit.io.uri
import java.io.File
import java.io.IOException

class StorageAccessFrameworkFile : LintFile {

    constructor(path: String) : super(path)
    constructor(file: LintFile) : super(file)
    constructor(file: LintFile, child: String) : super(file, child)

    private val context = LintFileConfiguration.instance.context;
    internal val documentFile: DocumentFile? = DocumentFile.fromTreeUri(context, path.documentFileUri(false))

    override fun exists(): Boolean =
        this.documentFile?.exists() ?: false

    override fun getParent(): String = this._file.parent ?: ""

    override fun getParentFile(): LintFile = StorageAccessFrameworkFile(getParent())

    override fun canRead(): Boolean =
        this.documentFile?.canRead() ?: false

    override fun canWrite(): Boolean =
        this.documentFile?.canWrite() ?: false

    override fun isDirectory(): Boolean =
        this.documentFile?.isDirectory ?: false

    override fun isFile(): Boolean =
        this.documentFile?.isFile ?: false

    override fun lastModified(): Long =
        this.documentFile?.lastModified() ?: -1

    override fun length(): Long =
        this.documentFile?.length() ?: -1

    override fun createNewFile(): Boolean {
        val primaryChildPath = getParent().primaryChildPath()
        val childPath = primaryChildPath.substring(primaryChildPath.indexOf("/") + 1)
        val names = childPath.split("/")
        val primaryPath = File(androidPath, names[0]).absolutePath
        var treeFile = DocumentFile.fromTreeUri(context, primaryPath.uri()) ?: throw IOException("File does not exist.")
        names.subList(1, names.size).forEach { name ->
            val findFile = treeFile.findFile(name)
            treeFile = if (findFile == null) {
                treeFile.createDirectory(name) ?: throw IOException("File does not exist.")
            } else {
                if (findFile.exists()) {
                    findFile
                } else {
                    findFile.createDirectory(name) ?: throw IOException("File does not exist.")
                }
            }
        }
        var findFile = treeFile.findFile(name)
        if (findFile == null) {
            findFile = treeFile.createFile("*/*", name)
        } else {
            if (!findFile.exists()) {
                findFile = treeFile.createFile("*/*", name)
            }
        }
        return findFile?.exists() ?: false
    }

    override fun delete(): Boolean =
        this.documentFile?.delete() ?: false

    override fun list(): Array<String> {
        val list = ArrayList<String>()
        if (this.documentFile != null) {
            val listFiles = this.documentFile.listFiles()
            listFiles.forEach {
                list.add(it.uri.absolutePath())
            }
        }
        return list.toTypedArray()
    }

    override fun list(filter: (String) -> Boolean): Array<String> {
        val list = ArrayList<String>()
        if (this.documentFile != null) {
            val listFiles = this.documentFile.listFiles()
            listFiles.forEach {
                if (filter(it.name ?: ""))
                    list.add(it.uri.absolutePath())
            }
        }
        return list.toTypedArray()
    }

    override fun listFiles(): Array<LintFile> {
        val list = ArrayList<StorageAccessFrameworkFile>()
        this.list().forEach {
            list.add(StorageAccessFrameworkFile(it))
        }
        return list.toTypedArray()
    }

    override fun listFiles(filter: (LintFile) -> Boolean): Array<LintFile> {
        val list = ArrayList<StorageAccessFrameworkFile>()
        this.list().forEach {
            val file = StorageAccessFrameworkFile(it)
            if (filter(file))
                list.add(file)
        }
        return list.toTypedArray()
    }

    override fun mkdirs(): Boolean {
        val primaryChildPath = path.primaryChildPath()
        val childPath = primaryChildPath.substring(primaryChildPath.indexOf("/") + 1)
        val names = childPath.split("/")
        val primaryPath = File(androidPath, names[0]).absolutePath
        var treeFile = DocumentFile.fromTreeUri(context, primaryPath.uri()) ?: return false
        names.subList(1, names.size).forEach { name ->
            val findFile = treeFile.findFile(name)
            treeFile = if (findFile == null) {
                treeFile.createDirectory(name) ?: return false
            } else {
                if (findFile.exists()) {
                    findFile
                } else {
                    findFile.createDirectory(name) ?: return false
                }
            }
        }
        return treeFile.exists()
    }

    override fun renameTo(dest: String): Boolean =
        this.documentFile?.renameTo(dest) ?: false
}