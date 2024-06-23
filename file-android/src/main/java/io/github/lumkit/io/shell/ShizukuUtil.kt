package io.github.lumkit.io.shell

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream


object ShizukuUtil {

    const val REQUEST_CODE = 0x000001

    val onRequestPermissionResultListener by lazy {
        OnRequestPermissionResultListener { _, _ ->
            checkPermission()
        }
    }

    fun addListener(onRequestPermissionResultListener: OnRequestPermissionResultListener) {
        Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener)
    }

    fun removeListener(onRequestPermissionResultListener: OnRequestPermissionResultListener) {
        Shizuku.removeRequestPermissionResultListener(onRequestPermissionResultListener)
    }

    fun checkPermission(): Boolean = try {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }catch (e: Exception) {
        e.printStackTrace()
        false
    }

    fun runCmd(cmd: String): String? = try {
        val p: Process = Shizuku.newProcess(arrayOf("sh"), null, null)
        val os = p.outputStream
        os.write("$cmd\nexit".toByteArray())
        os.flush()
        os.close()
        val `is` = p.inputStream
        val bis = BufferedInputStream(`is`)
        val baos = ByteArrayOutputStream()
        val bArr = ByteArray(8192)
        var i: Int
        while (bis.read(bArr).also { i = it } != -1) {
            baos.write(bArr, 0, i)
        }
        val bytes = baos.toByteArray()
        baos.close()
        bis.close()
        `is`.close()
        String(bytes)
    } catch (err: Exception) {
        err.printStackTrace()
        null
    }

    fun requestPermission() {
        Shizuku.requestPermission(REQUEST_CODE)
    }
}