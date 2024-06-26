package io.github.lumkit.io

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.UriPermission
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File

internal val androidPath = File(Environment.getExternalStorageDirectory(), "/Android").absolutePath
internal val rootPath = "${Environment.getExternalStorageDirectory().absolutePath}/"

internal fun String.checkPath() {
    if (!this.startsWith(androidPath)) {
        throw RuntimeException("Invalid path! Please ensure the path is under ${androidPath}.")
    }
}

internal fun String.primaryPath(): String {
    checkPath()
    return this.substring(indexOf(rootPath) + rootPath.length)
}

//internal fun String.primaryChildPath(): String {
//    checkPath()
//    return this.substring(indexOf(androidPath) + androidPath.length)
//}

internal fun String.primaryChildPath(): String {
    checkPath()
    val list = (if (this.startsWith("/")) {
        this.substring(1)
    } else this).split("/")
    val builder = StringBuilder()
    for (i in (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 6 else 5) until list.size) {
        builder.append("/")
            .append(list[i])
    }
    return builder.toString()
}

internal fun String.folderId(hide: Boolean = true) =
    "primary:${this.primaryPath().pathHandle(hide)}"

fun String.uri(hide: Boolean = true): Uri =
    Uri.Builder()
        .scheme("content")
        .authority("com.android.externalstorage.documents")
        .appendPath("tree")
        .appendPath(this.folderId(hide))
        .build()

fun String.documentUri(hide: Boolean = true): Uri =
    Uri.Builder()
        .scheme("content")
        .authority("com.android.externalstorage.documents")
        .appendPath("tree")
        .appendPath(this.folderId(hide))
        .appendPath("document")
        .appendPath(this.folderId(hide))
        .build()

fun String.getPrivateRootPath(): String {
    val list = (if (this.startsWith("/")) {
        this.substring(1)
    } else this).split("/")
    val builder = StringBuilder()
    for (i in 0 until if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 6 else 5) {
        builder.append("/")
            .append(list[i].replace("\u200d", ""))
    }
    return builder.toString()
}

fun String.documentUriForPermissions(hide: Boolean = true): Uri {
    val path = getPrivateRootPath()
    val builder = Uri.Builder()
        .scheme("content")
        .authority("com.android.externalstorage.documents")
        .appendPath("tree")
        .appendPath(path.folderId(hide))
        .appendPath("document")
        .appendPath(path.folderId(hide))
    return builder
        .build()
}

fun String.documentReallyUri(hide: Boolean = false): Uri {
    val list = (if (this.startsWith("/")) {
        this.substring(1)
    } else this).split("/")
    val builder = StringBuilder()
    for (i in 3 until if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 6 else 5) {
        builder.append("/")
            .append(list[i])
    }
    val fId = builder.substring(1).toString()
    val uBuilder = Uri.Builder()
        .scheme("content")
        .authority("com.android.externalstorage.documents")
        .appendPath("tree")
        .appendPath("primary:${fId.pathHandle(hide)}")
        .appendPath("document")
        .appendPath(this.folderId(hide))
    return uBuilder
        .build()
}

fun String.documentFileUri(tree: Boolean = true, hide: Boolean = true): Uri =
    if (tree) {
        DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            this.folderId(hide)
        )
    } else {
        DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            this.folderId(hide)
        )
    }

fun Activity.requestAccessPermission(requestCode: Int, path: String) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    intent.setFlags(
        Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
    )
    val uri = path.documentUriForPermissions()
    intent.putExtra("android.provider.extra.INITIAL_URI", uri)
    intent.putExtra(
        "pn",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val primaryChildPath = path.primaryChildPath()
                val childPath = primaryChildPath.substring(primaryChildPath.indexOf("/") + 1)
                val names = childPath.split("/")
                names[1]
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    )
    startActivityForResult(intent, requestCode)
}

@SuppressLint("WrongConstant")
fun Activity.takePersistableUriPermission(
    yourCode: Int,
    requestCode: Int,
    resultCode: Int,
    data: Intent?
) {
    if (resultCode == Activity.RESULT_OK && yourCode == requestCode && data != null) {
        data.data?.let {
            contentResolver.takePersistableUriPermission(
                it, data.flags and (
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
            )
        }
    }
}

fun Uri.isInPersistedUriPermissions(): Boolean =
    LintFileConfiguration.instance.context.contentResolver.persistedUriPermissions.find {
        this.toString().replace("%E2%80%8D", "").substring(this.toString().lastIndexOf("%3A") + 3).startsWith(
            it.uri.toString().replace("%E2%80%8D", "").substring(it.uri.toString().lastIndexOf("%3A") + 3)
        ) && (it.isReadPermission || it.isWritePermission)
    } != null

fun Uri.absolutePath(): String {
    val decode = Uri.decode(this.toString())
    val child = decode.substring(decode.lastIndexOf("primary:") + "primary:".length)
    return File(Environment.getExternalStorageDirectory(), child).absolutePath
}

