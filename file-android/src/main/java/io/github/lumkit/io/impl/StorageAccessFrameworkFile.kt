package io.github.lumkit.io.impl

import android.os.Build
import androidx.documentfile.provider.DocumentFile
import io.github.lumkit.io.LintFile
import io.github.lumkit.io.LintFileConfiguration
import io.github.lumkit.io.absolutePath
import io.github.lumkit.io.androidPath
import io.github.lumkit.io.documentFileUri
import io.github.lumkit.io.documentReallyUri
import io.github.lumkit.io.primaryChildPath
import io.github.lumkit.io.uri
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class StorageAccessFrameworkFile : LintFile {

    constructor(path: String) : super(path)
    constructor(file: LintFile) : super(file)
    constructor(file: LintFile, child: String) : super(file, child)

    private val context = LintFileConfiguration.instance.context;
    internal val documentFile: DocumentFile? = DocumentFile.fromTreeUri(context, path.replace("\u200d", "").documentReallyUri(false))

    override fun exists(): Boolean =
        this.documentFile?.exists() ?: false

    override fun getParent(): String = this._file.parent?.replace("\u200d", "") ?: ""

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
        if (exists())
            return true
        val parentFile = getParentFile() as StorageAccessFrameworkFile
        if (!parentFile.exists()) {
            throw FileNotFoundException("No such file or directory: ${parentFile.path}")
        }
        return parentFile.documentFile?.createFile("*/*", name) != null
    }

    override fun delete(): Boolean =
        this.documentFile?.delete() ?: false

    override fun list(): Array<String> {
        val list = ArrayList<String>()
        if (this.documentFile != null) {
            val listFiles = this.documentFile.listFiles()
            listFiles.forEach {
                list.add(it.uri.absolutePath().replace("\u200d", ""))
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
                    list.add(it.uri.absolutePath().replace("\u200d", ""))
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
        if (exists())
            return true

        var startPath = startPath()
        val endPath = endPath()

        if (endPath.isEmpty())
            return true

        val names = endPath.substring(1).split("/")
        names.forEach {
            val p = File(startPath, it).absolutePath
            val safFile = StorageAccessFrameworkFile(p)
            if (!safFile.exists()) {
                StorageAccessFrameworkFile(startPath).documentFile?.createDirectory(it) ?: throw FileNotFoundException("Cannot write to file $path")
            }
            startPath = p
        }
        return StorageAccessFrameworkFile(startPath).exists()
    }

    private fun startPath(): String {
        val path = path.replace("\u200d", "")
        val list = (if (path.startsWith("/")) {
            path.substring(1)
        } else path).split("/")
        val builder = StringBuilder()
        for (i in 0 until if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 6 else 5) {
            builder.append("/")
                .append(list[i])
        }
        return builder.toString()
    }

    private fun endPath(): String {
        val path = path.replace("\u200d", "")
        val list = (if (path.startsWith("/")) {
            path.substring(1)
        } else path).split("/")
        val builder = StringBuilder()
        for (i in (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 6 else 5) until list.size) {
            builder.append("/")
                .append(list[i])
        }
        return builder.toString()
    }

    override fun renameTo(dest: String): Boolean =
        this.documentFile?.renameTo(dest) ?: false
}