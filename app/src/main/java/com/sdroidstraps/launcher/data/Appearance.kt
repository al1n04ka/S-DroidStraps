package com.sdroidstraps.launcher.data

import android.content.Context

enum class UiStyle(val id: String, val label: String) {
    Classic("classic", "Classic"),
    Bloxstrap("bloxstrap", "Bloxstrap"),
    Terminal("terminal", "Terminal"),
    Minimal("minimal", "Minimal");

    companion object {
        fun from(id: String?): UiStyle = entries.firstOrNull { it.id == id } ?: Classic
    }
}

data class ColorPreset(val id: String, val label: String, val accent: Long)

val COLOR_PRESETS: List<ColorPreset> = listOf(
    ColorPreset("violet", "Violet", 0xFF7C5CFF),
    ColorPreset("ocean", "Ocean", 0xFF3AA0FF),
    ColorPreset("emerald", "Emerald", 0xFF34D399),
    ColorPreset("sunset", "Sunset", 0xFFFF8A3D),
    ColorPreset("rose", "Rose", 0xFFFF5C8A),
    ColorPreset("mono", "Mono", 0xFFD7DEE8)
)

const val DEFAULT_ACCENT: Long = 0xFF7C5CFF

data class Appearance(
    val style: UiStyle,
    val accent: Long
)

object AppearanceStore {
    private const val PREFS = "appearance"
    private const val KEY_STYLE = "style"
    private const val KEY_ACCENT = "accent"
    private const val KEY_ONBOARDING = "onboarding_done"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): Appearance {
        val prefs = prefs(context)
        return Appearance(
            style = UiStyle.from(prefs.getString(KEY_STYLE, null)),
            accent = prefs.getLong(KEY_ACCENT, DEFAULT_ACCENT)
        )
    }

    fun saveStyle(context: Context, style: UiStyle) {
        prefs(context).edit().putString(KEY_STYLE, style.id).apply()
    }

    fun saveAccent(context: Context, accent: Long) {
        prefs(context).edit().putLong(KEY_ACCENT, accent).apply()
    }

    fun isOnboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING, false)

    fun setOnboardingDone(context: Context, done: Boolean) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING, done).apply()
    }
}
fun parseHexColor(input: String): Long? {
    val raw = input.trim().removePrefix("#")
    if (raw.length != 6 && raw.length != 8) return null
    val value = raw.toLongOrNull(16) ?: return null
    return if (raw.length == 6) 0xFF000000L or value else value
}