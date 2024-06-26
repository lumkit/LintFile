package io.github.lumkit.io.impl

import io.github.lumkit.io.LintFile
import io.github.lumkit.io.shell.AdbShellPublic

class ShizukuFile : LintFile {

    constructor(path: String) : super(path)
    constructor(file: LintFile) : super(file)
    constructor(file: LintFile, child: String) : super(file, child)

    override fun exists(): Boolean =
        AdbShellPublic.doCmdSync("[ -e \"${path.replace("\u200d", "")}\" ] && echo 1 || echo 0") == "1"

    override fun getParent(): String = _file.parent?.replace("\u200d", "") ?: ""

    override fun getParentFile(): LintFile = ShizukuFile(getParent())

    override fun canRead(): Boolean =
        AdbShellPublic.doCmdSync("[ -r \"${path.replace("\u200d", "")}\" ] && echo 1 || echo 0") == "1"

    override fun canWrite(): Boolean =
        AdbShellPublic.doCmdSync("[ -w \"${path.replace("\u200d", "")}\" ] && echo 1 || echo 0") == "1"

    override fun isDirectory(): Boolean =
        AdbShellPublic.doCmdSync("[ -d \"${path.replace("\u200d", "")}\" ] && echo 1 || echo 0") == "1"

    override fun isFile(): Boolean =
        AdbShellPublic.doCmdSync("[ -f \"${path.replace("\u200d", "")}\" ] && echo 1 || echo 0") == "1"

    override fun lastModified(): Long = try {
        AdbShellPublic.doCmdSync("stat -c '%Y' \"${path.replace("\u200d", "")}\"").toLong()
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }

    override fun length(): Long = try {
        AdbShellPublic.doCmdSync("stat -c '%s' \"${path.replace("\u200d", "")}\"").toLong()
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }

    override fun createNewFile(): Boolean =
        AdbShellPublic.doCmdSync("[ ! -e \"${path.replace("\u200d", "")}\" ] && echo -n > \"${path.replace("\u200d", "")}\" && echo 1 || echo 0") == "1"

    override fun delete(): Boolean =
        AdbShellPublic.doCmdSync("(rm -rf \"${path.replace("\u200d", "")}\") && echo 1 || echo 0") == "1"

    override fun list(): Array<String> {
        if (!isDirectory())
            return arrayOf()
        val cmd = "ls -a \"${path.replace("\u200d", "")}\""

        val list = ArrayList(AdbShellPublic.doCmdSync(cmd).split("\n"))
        val iterator = list.listIterator()

        while (iterator.hasNext()) {
            val name: String = iterator.next()
            if (name == "." || name == "..") {
                iterator.remove()
            }
        }

        return list.map { "${path.replace("\u200d", "")}/$it" }.toTypedArray()
    }

    override fun list(filter: (String) -> Boolean): Array<String> = list().filter { filter(it) }.toTypedArray()

    override fun listFiles(): Array<LintFile> = list().map { ShizukuFile(it) }.toTypedArray()

    override fun listFiles(filter: (LintFile) -> Boolean): Array<LintFile> = listFiles().filter { filter(it) }.toTypedArray()

    override fun mkdirs(): Boolean = AdbShellPublic.doCmdSync("mkdir -p \"${path.replace("\u200d", "")}\" && echo 1 || echo 0") == "1"

    override fun renameTo(dest: String): Boolean {
        val cmd = "mv -f \"${path.replace("\u200d", "")}\" \"${getParent()}/${dest.replace("\u200d", "")}\" && echo 1 || echo 0"
        return AdbShellPublic.doCmdSync(cmd) == "1"
    }

    fun clear(): Boolean {
        val cmd = "(echo -n > \"${path.replace("\u200d", "")}\") && echo 1 || echo 0"
        return AdbShellPublic.doCmdSync(cmd) == "1"
    }
}