package com.sdroidstraps.launcher.data

import android.content.Context

enum class ShellMode(val id: String, val label: String) {
    Auto("auto", "Auto"),
    Shizuku("shizuku", "Shizuku"),
    Root("root", "Root");

    companion object {
        fun from(id: String?): ShellMode = entries.firstOrNull { it.id == id } ?: Auto
    }
}

object ShellModeStore {
    private const val PREFS = "shell"
    private const val KEY_MODE = "mode"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): ShellMode = ShellMode.from(prefs(context).getString(KEY_MODE, null))

    fun save(context: Context, mode: ShellMode) {
        prefs(context).edit().putString(KEY_MODE, mode.id).apply()
    }
}
