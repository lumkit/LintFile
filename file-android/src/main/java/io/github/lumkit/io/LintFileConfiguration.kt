package io.github.lumkit.io

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.topjohnwu.superuser.Shell
import io.github.lumkit.io.data.IoModel
import io.github.lumkit.io.data.LintFileConfig
import io.github.lumkit.io.shell.ShizukuUtil

class LintFileConfiguration {

    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance = LintFileConfiguration()
    }

    internal lateinit var context: Activity
    var ioMode: IoModel = IoModel.NORMAL

    fun init(context: Activity, fileConfig: LintFileConfig? = null) {
        this.context = context
        fileConfig?.let {
            this.ioMode = it.ioModel
        }
        ShizukuUtil.addListener(ShizukuUtil.onRequestPermissionResultListener)
    }

    fun destroy() {
        ShizukuUtil.removeListener(ShizukuUtil.onRequestPermissionResultListener)
    }

}