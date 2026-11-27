package com.sdroidstraps.launcher.shell

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import java.io.IOException
import java.util.concurrent.TimeUnit

data class RootManager(val label: String, val packageName: String)

object RootHelper {
    private val MANAGERS = listOf(
        RootManager("KernelSU", "me.weishu.kernelsu"),
        RootManager("KernelSU Next", "me.weishu.kernelsu.next"),
        RootManager("APatch", "me.bmax.apatch"),
        RootManager("Magisk", "com.topjohnwu.magisk"),
        RootManager("SuperSU", "eu.chainfire.supersu")
    )

    fun manager(context: Context): RootManager? = MANAGERS.firstOrNull {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(it.packageName, 0)
            true
        }.getOrDefault(false)
    }

    fun checkRoot(): Boolean {
        val result = exec("id")
        return result.ok && result.output.contains("uid=0")
    }

    fun writeFlags(json: String): ShellResult {
        val result = exec(FlagsScript.write(json, root = true))
        if (!result.ok || !result.output.contains("OK")) return result
        return ShellResult(
            true,
            "Flags written to ${FlagsScript.TMP_FILE} and Roblox app data via root"
        )
    }

    fun clearFlags(): ShellResult {
        val result = exec(FlagsScript.clear(root = true))
        return if (result.ok && result.output.contains("OK")) {
            ShellResult(true, "Flag file removed, Roblox stopped")
        } else {
            result
        }
    }

    fun readFlags(): ShellResult = exec("cat ${FlagsScript.TMP_FILE}")

    fun exec(command: String): ShellResult {
        val process = try {
            ProcessBuilder("su", "-c", command)
                .redirectErrorStream(true)
                .start()
        } catch (t: Throwable) {
            return ShellResult(false, "Can't run su: ${t.message ?: t.javaClass.simpleName}")
        }

        return try {
            val output = process.inputStream.bufferedReader().use { it.readText() }
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                return ShellResult(false, "su timed out")
            }
            val text = output.trim()
            if (process.exitValue() == 0) {
                ShellResult(true, text.ifEmpty { "OK" })
            } else {
                ShellResult(false, text.ifEmpty { "su exited with ${process.exitValue()}" })
            }
        } catch (t: IOException) {
            ShellResult(false, t.message ?: t.javaClass.simpleName)
        } catch (t: Throwable) {
            ShellResult(false, t.message ?: t.javaClass.simpleName)
        }
    }
}
