package com.sdroidstraps.launcher.shell

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import rikka.shizuku.Shizuku

data class ShellResult(val ok: Boolean, val output: String)

object ShizukuHelper {
    const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    const val ROBLOX_PACKAGE = "com.roblox.client"

    private const val TMP_FILE = "/data/local/tmp/ClientAppSettings.json"
    private const val APP_DIR = "/data/data/com.roblox.client/files/exe/ClientSettings"
    private const val APP_FILE = "$APP_DIR/ClientAppSettings.json"

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

        val encoded = Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val script = StringBuilder()
        script.append("mkdir -p /data/local/tmp; ")
        script.append("printf '%s' '").append(encoded)
            .append("' | base64 -d > ").append(TMP_FILE).append("; ")
        script.append("chmod 644 ").append(TMP_FILE).append("; ")
        if (isRoot()) {
            script.append("mkdir -p ").append(APP_DIR).append("; ")
            script.append("cat ").append(TMP_FILE).append(" > ").append(APP_FILE).append("; ")
            script.append("chmod 644 ").append(APP_FILE).append("; ")
        }
        script.append("am force-stop ").append(ROBLOX_PACKAGE).append("; ")
        script.append("echo OK")

        val result = exec(script.toString())
        if (!result.output.contains("OK")) return result
        val suffix = if (isRoot()) " and app data" else ""
        return ShellResult(true, "Flags written to $TMP_FILE$suffix")
    }

    fun clearFlags(): ShellResult {
        if (!isRunning()) return ShellResult(false, "Shizuku is not running")
        if (!hasPermission()) return ShellResult(false, "No Shizuku access")

        val script = StringBuilder()
        script.append("rm -f ").append(TMP_FILE).append("; ")
        if (isRoot()) {
            script.append("rm -f ").append(APP_FILE).append("; ")
        }
        script.append("am force-stop ").append(ROBLOX_PACKAGE).append("; ")
        script.append("echo OK")

        val result = exec(script.toString())
        return if (result.output.contains("OK")) {
            ShellResult(true, "Flag file removed, Roblox stopped")
        } else {
            result
        }
    }

    fun readFlags(): ShellResult = exec("cat $TMP_FILE")

    fun adbCommand(json: String): String {
        val encoded = Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "adb shell \"echo '$encoded' | base64 -d > $TMP_FILE && chmod 644 $TMP_FILE && am force-stop $ROBLOX_PACKAGE\""
    }

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
