package com.sdroidstraps.launcher.shell

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

data class ShellResult(val ok: Boolean, val output: String)

object ShizukuHelper {
    const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    const val ROBLOX_PACKAGE = FlagsScript.ROBLOX_PACKAGE

    fun isInstalled(context: Context): Boolean = runCatching {
        context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
        true
    }.getOrDefault(false)

    fun isRunning(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun hasPermission(): Boolean = isRunning() && runCatching {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    fun isRoot(): Boolean = isRunning() && runCatching { Shizuku.getUid() == 0 }.getOrDefault(false)

    fun requestPermission(requestCode: Int) {
        if (!isRunning() || hasPermission()) return
        runCatching { Shizuku.requestPermission(requestCode) }
    }

    fun writeFlags(json: String): ShellResult {
        if (!isRunning()) return ShellResult(false, "Shizuku is not running — open Shizuku and try again")
        if (!hasPermission()) return ShellResult(false, "No Shizuku access — tap \"Request access\"")

        val result = exec(FlagsScript.write(json, isRoot()))
        if (!result.output.contains("OK") || !result.ok) return result
        val suffix = if (isRoot()) " and app data" else ""
        return ShellResult(true, "Flags written to ${FlagsScript.TMP_FILE}$suffix")
    }

    fun clearFlags(): ShellResult {
        if (!isRunning()) return ShellResult(false, "Shizuku is not running")
        if (!hasPermission()) return ShellResult(false, "No Shizuku access")

        val result = exec(FlagsScript.clear(isRoot()))
        return if (result.ok && result.output.contains("OK")) {
            ShellResult(true, "Flag file removed, Roblox stopped")
        } else {
            result
        }
    }

    fun readFlags(): ShellResult = exec("cat ${FlagsScript.TMP_FILE}")

    fun adbCommand(json: String): String = FlagsScript.adbCommand(json)

    @Suppress("SwallowedException")
    private fun newProcess(command: Array<String>): Process? = try {
        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        method.isAccessible = true
        method.invoke(null, command, null, null) as? Process
    } catch (_: Throwable) {
        null
    }

    fun exec(command: String): ShellResult {
        if (!isRunning()) return ShellResult(false, "Shizuku is not running")
        if (!hasPermission()) return ShellResult(false, "No Shizuku access")

        val process = newProcess(arrayOf("sh", "-c", command))
            ?: return ShellResult(false, "Shizuku can't run shell commands")

        return try {
            val out = process.inputStream.bufferedReader().use { it.readText() }
            val err = process.errorStream.bufferedReader().use { it.readText() }
            val code = process.waitFor()
            val text = (out + err).trim()
            ShellResult(code == 0, text.ifEmpty { "OK" })
        } catch (t: Throwable) {
            ShellResult(false, t.message ?: t.javaClass.simpleName)
        }
    }
}
