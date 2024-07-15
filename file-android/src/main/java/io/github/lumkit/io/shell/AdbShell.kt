package io.github.lumkit.io.shell

import android.util.Log
import java.io.BufferedReader
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.concurrent.locks.ReentrantLock

class AdbShell {
    private var process: Process? = null
    private var out: OutputStream? = null
    private var reader: BufferedReader? = null
    private var currentIsIdle = true // 是否处于闲置状态
    val isIdle: Boolean
        get() {
            return currentIsIdle
        }

    private val mLock = ReentrantLock()
    private val LOCK_TIMEOUT = 10000L
    private var enterLockTime = 0L

    fun tryExit() {
        try {
            out?.close()
            reader?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            process?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        enterLockTime = 0L
        out = null
        reader = null
        process = null
        currentIsIdle = true
    }

    private fun getRuntimeShell() {
        if (process != null) return
        val getSu = Thread {
            try {
                mLock.lockInterruptibly()
                enterLockTime = System.currentTimeMillis()
                process = ShellExecutor.getShizukuProcess().apply {}
                out = process?.outputStream
                reader = process?.inputStream?.bufferedReader()
                Thread {
                    try {
                        val errorReader = process?.errorStream?.bufferedReader()
                        while (true) {
                            println("adb shell error: ${errorReader?.readLine()}")
                        }
                    } catch (ex: Exception) {
                        Log.e("c", "" + ex.message)
                    }
                }.start()
            } catch (ex: Exception) {
                Log.e("getRuntime", "" + ex.message)
            } finally {
                enterLockTime = 0L
                mLock.unlock()
            }
        }
        getSu.start()
        getSu.join(10000)
        if (process == null && getSu.state != Thread.State.TERMINATED) {
            enterLockTime = 0L
            getSu.interrupt()
        }
    }

    private val shellOutputCache = StringBuilder()
    private val endTag = "|<<SH|"
    private val endTagBytes = "echo '$endTag'\n".toByteArray(Charset.defaultCharset())


    fun doCmdSync(cmd: String): String {
        if (mLock.isLocked && enterLockTime > 0 && System.currentTimeMillis() - enterLockTime > LOCK_TIMEOUT) {
            tryExit()
        }
        getRuntimeShell()
        try {
            mLock.lockInterruptibly()
            currentIsIdle = false
            out?.run {
                write("$cmd\n".toByteArray(Charset.defaultCharset()))
                write(endTagBytes)
                flush()
            }
            reader?.also {
                shellOutputCache.clear()
                while (true) {
                    val line = it.readLine()
                    if (line.contains(endTag)) {
                        break
                    }
                    shellOutputCache.append(line).append("\n")
                }
            }
            return shellOutputCache.let {
                if (it.isEmpty()) {
                    it
                } else {
                    it.substring(0, it.length - 1)
                }
            }.toString()
        } catch (e: Exception) {
            tryExit()
            e.printStackTrace()
            return "error"
        } finally {
            enterLockTime = 0L
            mLock.unlock()
            currentIsIdle = true
        }
    }
}