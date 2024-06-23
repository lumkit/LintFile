package io.github.lumkit.io.shell

object AdbShellPublic {

    private val pool = HashMap<String, AdbShell>()

    fun getInstance(key: String): AdbShell {
        synchronized(pool) {
            if (!pool.containsKey(key)) {
                pool[key] = AdbShell()
            }
            return pool[key]!!
        }
    }

    fun destroyInstance(key: String) {
        synchronized(pool) {
            if (!pool.containsKey(key)) {
                return
            } else {
                val keepShell = pool[key]!!
                pool.remove(key)
                keepShell.tryExit()
            }
        }
    }

    fun destroyAll() {
        synchronized(pool) {
            while (pool.isNotEmpty()) {
                val key = pool.keys.first()
                val keepShell = pool.get(key)!!
                pool.remove(key)
                keepShell.tryExit()
            }
        }
    }

    val defaultKeepShell = AdbShell()
    val secondaryKeepShell = AdbShell()

    val shell = getInstance("shell-default")
//    val shizuku = getInstance("shizuku-default", IOMode.SHIZUKU)

    fun getDefaultInstance(): AdbShell {
        return if (defaultKeepShell.isIdle || !secondaryKeepShell.isIdle) {
            defaultKeepShell
        } else {
            secondaryKeepShell
        }
    }

    fun doCmdSync(commands: List<String>): Boolean {
        val stringBuilder = StringBuilder()
        for (cmd in commands) {
            stringBuilder.append(cmd)
            stringBuilder.append("\n\n")
        }
        return doCmdSync(stringBuilder.toString()) != "error"
    }

    //执行脚本
    fun doCmdSync(cmd: String): String {
        return getDefaultInstance().doCmdSync(cmd)
    }

    fun tryExit() {
        defaultKeepShell.tryExit()
        secondaryKeepShell.tryExit()
    }

}