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

internal fun String.primaryChildPath(): String {
    checkPath()
    return this.substring(indexOf(androidPath) + androidPath.length)
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
                or Intent.FLAG_ACTIVITY_NEW_TASK
    )
    val uri = path.documentUri()
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

        this.toString()
            .startsWith(it.uri.toString()) && (it.isReadPermission || it.isWritePermission)
    } != null

fun Uri.absolutePath(): String {
    val decode = Uri.decode(this.toString())
    val child = decode.substring(decode.lastIndexOf("primary:") + "primary:".length)
    return File(Environment.getExternalStorageDirectory(), child).absolutePath
}

