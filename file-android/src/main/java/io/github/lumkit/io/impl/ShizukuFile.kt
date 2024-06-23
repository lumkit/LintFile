package io.github.lumkit.io.impl

import com.topjohnwu.superuser.ShellUtils
import io.github.lumkit.io.LintFile
import io.github.lumkit.io.shell.AdbShellPublic

class ShizukuFile : LintFile {

    constructor(path: String) : super(path)
    constructor(file: LintFile) : super(file)
    constructor(file: LintFile, child: String) : super(file, child)

    override fun exists(): Boolean =
        AdbShellPublic.doCmdSync("[ -e \"$path\" ] && echo 1 || echo 0") == "1"

    override fun getParent(): String = _file.parent ?: ""

    override fun getParentFile(): LintFile = ShizukuFile(getParent())

    override fun canRead(): Boolean =
        AdbShellPublic.doCmdSync("[ -r \"$path\" ] && echo 1 || echo 0") == "1"

    override fun canWrite(): Boolean =
        AdbShellPublic.doCmdSync("[ -w \"$path\" ] && echo 1 || echo 0") == "1"

    override fun isDirectory(): Boolean =
        AdbShellPublic.doCmdSync("[ -d \"$path\" ] && echo 1 || echo 0") == "1"

    override fun isFile(): Boolean =
        AdbShellPublic.doCmdSync("[ -f \"$path\" ] && echo 1 || echo 0") == "1"

    override fun lastModified(): Long = try {
        AdbShellPublic.doCmdSync("stat -c '%Y' \"$path\"").toLong()
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }

    override fun length(): Long = try {
        AdbShellPublic.doCmdSync("stat -c '%s' \"$path\"").toLong()
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }

    override fun createNewFile(): Boolean =
        AdbShellPublic.doCmdSync("[ ! -e \"$path\" ] && echo -n > \"$path\" && echo 1 || echo 0") == "1"

    override fun delete(): Boolean =
        AdbShellPublic.doCmdSync("(rm -f \"$path\" || rmdir -f \"$path\") && echo 1 || echo 0") == "1"

    override fun list(): Array<String> {
        if (!isDirectory())
            return arrayOf()
        val cmd = "ls -a \"$path\""

        val list = ArrayList(AdbShellPublic.doCmdSync(cmd).split("\n"))
        val iterator = list.listIterator()

        while (iterator.hasNext()) {
            val name: String = iterator.next()
            if (name == "." || name == "..") {
                iterator.remove()
            }
        }

        return list.map { "$path/$it" }.toTypedArray()
    }

    override fun list(filter: (String) -> Boolean): Array<String> = list().filter { filter(it) }.toTypedArray()

    override fun listFiles(): Array<LintFile> = list().map { ShizukuFile(it) }.toTypedArray()

    override fun listFiles(filter: (LintFile) -> Boolean): Array<LintFile> = listFiles().filter { filter(it) }.toTypedArray()

    override fun mkdirs(): Boolean = AdbShellPublic.doCmdSync("mkdir -p \"$path\" && echo 1 || echo 0") == "1"

    override fun renameTo(dest: String): Boolean {
        val cmd = "mv -f \"$path\" \"${getParent()}/$dest\" && echo 1 || echo 0"
        return AdbShellPublic.doCmdSync(cmd) == "1"
    }
}