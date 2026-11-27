package com.sdroidstraps.launcher.shell

import android.util.Base64

object FlagsScript {
    const val TMP_FILE = "/data/local/tmp/ClientAppSettings.json"
    const val ROBLOX_PACKAGE = "com.roblox.client"
    private const val APP_DIR = "/data/data/com.roblox.client/files/exe/ClientSettings"
    private const val APP_FILE = "$APP_DIR/ClientAppSettings.json"

    fun write(json: String, root: Boolean): String {
        val encoded = Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val script = StringBuilder()
        script.append("mkdir -p /data/local/tmp; ")
        script.append("printf '%s' '").append(encoded)
            .append("' | base64 -d > ").append(TMP_FILE).append("; ")
        script.append("chmod 644 ").append(TMP_FILE).append("; ")
        if (root) {
            script.append("mkdir -p ").append(APP_DIR).append("; ")
            script.append("cat ").append(TMP_FILE).append(" > ").append(APP_FILE).append("; ")
            script.append("chmod 644 ").append(APP_FILE).append("; ")
        }
        script.append("am force-stop ").append(ROBLOX_PACKAGE).append("; ")
        script.append("echo OK")
        return script.toString()
    }

    fun clear(root: Boolean): String {
        val script = StringBuilder()
        script.append("rm -f ").append(TMP_FILE).append("; ")
        if (root) {
            script.append("rm -f ").append(APP_FILE).append("; ")
        }
        script.append("am force-stop ").append(ROBLOX_PACKAGE).append("; ")
        script.append("echo OK")
        return script.toString()
    }

    fun adbCommand(json: String): String {
        val encoded = Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "adb shell \"echo '$encoded' | base64 -d > $TMP_FILE && chmod 644 $TMP_FILE && am force-stop $ROBLOX_PACKAGE\""
    }
}
