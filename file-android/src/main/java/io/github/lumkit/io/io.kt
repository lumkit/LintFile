package io.github.lumkit.io

import android.os.Build
import android.os.Environment
import android.system.ErrnoException
import com.topjohnwu.superuser.Shell
import io.github.lumkit.io.data.IoModel
import io.github.lumkit.io.impl.DefaultFile
import io.github.lumkit.io.impl.ShizukuFile
import io.github.lumkit.io.impl.StorageAccessFrameworkFile
import io.github.lumkit.io.impl.SuFile
import io.github.lumkit.io.shell.AdbShellPublic
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

/**
 * 创建一个通用的File
 */
fun file(path: String): LintFile =
    when (LintFileConfiguration.instance.ioMode) {
        IoModel.SU, IoModel.KSU -> SuFile(path)
        IoModel.SUU -> SuFile(path)
        IoModel.SHIZUKU -> ShizukuFile(path)
        else -> {
            createUserFile(path)
        }
    }

/**
 * 创建一个通用的File
 */
fun file(file: LintFile): LintFile =
    when (LintFileConfiguration.instance.ioMode) {
        IoModel.SU, IoModel.KSU, IoModel.SUU -> SuFile(file)
        IoModel.SHIZUKU -> ShizukuFile(file)
        else -> {
            createUserFile(file.path)
        }
    }

/**
 * 创建一个通用的File
 */
fun file(dir: LintFile, child: String): LintFile =
    when (LintFileConfiguration.instance.ioMode) {
        IoModel.SU, IoModel.KSU, IoModel.SUU -> SuFile(dir, child)
        IoModel.SHIZUKU -> ShizukuFile(dir, child)
        else -> {
            createUserFile(File(dir.path, child).absolutePath)
        }
    }

/**
 * 创建用户权限级别的File
 */
private fun createUserFile(path: String): LintFile =
    if (isSafDir(path)) {
        StorageAccessFrameworkFile(path)
    } else {
        DefaultFile(path)
    }

fun isSafDir(path: String): Boolean {
    val canRead = File(path).canRead()
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && path.startsWith(
        File(
            Environment.getExternalStorageDirectory(),
            "Android"
        ).absolutePath + "/"
    ) && !canRead
}

fun String.pathHandle(hide: Boolean = true): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && hide) {
        this.replace("Android", "Android\u200d")
    } else {
        this
    }

@Throws(IOException::class)
fun LintFile.openInputStream(): InputStream =
    when (this) {
        is SuFile -> suFile.newInputStream()

        is StorageAccessFrameworkFile -> LintFileConfiguration.instance.context.contentResolver.openInputStream(
            documentFile?.uri ?: throw IOException("No such file or directory")
        ) ?: throw throw IOException("No such file or directory")

        is ShizukuFile -> newInputStream()

        else -> FileInputStream(path)
    }

@Throws(IOException::class)
fun LintFile.openOutputStream(): OutputStream =
    when (this) {
        is SuFile -> suFile.newOutputStream()

        is StorageAccessFrameworkFile -> LintFileConfiguration.instance.context.contentResolver.openOutputStream(
            documentFile?.uri ?: throw IOException("No such file or directory"),
            "rwt"
        ) ?: throw throw IOException("No such file or directory")

        is ShizukuFile -> newOutputStream()
        else -> FileOutputStream(path)
    }

@Throws(
    ErrnoException::class,
    java.io.IOException::class
)
fun createTempFIFO(): File {
//    val fifo = File.createTempFile("lintfile-fifo-", null)
//    fifo.delete()
//    Os.mkfifo(fifo.path, 644)
//    return fifo
    val fifoDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        ".lint-file-tmp"
    )
    if (!fifoDir.exists()) fifoDir.mkdirs()
    val fifo = File(fifoDir, "lintfile-fifo-${UUID.randomUUID()}.tmp")
    fifo.createNewFile()
//    fifo.delete()
    return fifo
}

private fun ShizukuFile.newInputStream(): InputStream {
    if (isDirectory() || !canRead()) throw FileNotFoundException("No such file or directory: $path")

    var f: File? = null
    try {
        val fifo = createTempFIFO()
        f = fifo
        val cmd = "cat \"$path\" > \"${fifo.absolutePath}\" &"
        AdbShellPublic.doCmdSync(cmd)
        // Open the fifo only after the shell request
        val stream = FutureTask<InputStream> {
            FileInputStream(
                fifo
            )
        }
        Shell.EXECUTOR.execute(stream)
        return stream[FIFO_TIMEOUT.toLong(), TimeUnit.MILLISECONDS]
    } catch (e: Exception) {
        if (e is FileNotFoundException) throw e
        val cause = e.cause
        if (cause is FileNotFoundException) throw cause
        val err = FileNotFoundException("Cannot open fifo").initCause(e)
        throw (err as FileNotFoundException)
    } finally {
        f?.delete()
    }
}

private const val FIFO_TIMEOUT = 250
private fun ShizukuFile.newOutputStream(): OutputStream {
    if (isDirectory()) throw FileNotFoundException("$path is not a file but a directory")

    if (!canWrite() && !createNewFile()) {
        throw FileNotFoundException("Cannot write to file $path")
    } else if (!clear()) {
        throw FileNotFoundException("Failed to clear file $path")
    }

    var f: File? = null
    try {
        val fifo = createTempFIFO()
        f = fifo
        val cmd = "cat \"${fifo.absolutePath}\" > \"$path\" &"
        AdbShellPublic.doCmdSync(cmd)

        // Open the fifo only after the shell request
        val stream = FutureTask<OutputStream> { FileOutputStream(fifo) }
        Shell.EXECUTOR.execute(stream)
        return stream[FIFO_TIMEOUT.toLong(), TimeUnit.MILLISECONDS]
    } catch (e: java.lang.Exception) {
        if (e is FileNotFoundException) throw e
        val cause = e.cause
        if (cause is FileNotFoundException) throw cause
        val err = FileNotFoundException("Cannot open fifo").initCause(e)
        throw (err as FileNotFoundException)
    } finally {
        f?.let {
            AdbShellPublic.doCmdSync("mv -f \"${it.absolutePath}\" \"${this.path}\"")
            it.delete()
        }
    }
}
