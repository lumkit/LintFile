package io.github.lumkit.io.jni

object LintJni {

    /**
     * A native method that is implemented by the 'lumkit' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    external fun cmd(cmd: String): String?
    external fun exists(path: String): Boolean
    external fun length(path: String): Long
    external fun lastModified(path: String): Long
    external fun createNewFile(path: String): Boolean
    external fun checkRoot(): Boolean

    init {
        System.loadLibrary("shell-file")
    }
}