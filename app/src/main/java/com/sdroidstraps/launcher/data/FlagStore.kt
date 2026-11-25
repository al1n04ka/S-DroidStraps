package com.sdroidstraps.launcher.data

import android.content.Context
import org.json.JSONObject

object FlagStore {
    private const val PREFS = "sdroidstraps"
    private const val KEY_FLAGS = "flags_json"
    private const val KEY_AUTO_APPLY = "auto_apply"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): Map<String, String> {
        val raw = prefs(context).getString(KEY_FLAGS, null) ?: return emptyMap()
        return fromJson(raw).getOrDefault(emptyMap())
    }

    fun save(context: Context, flags: Map<String, String>) {
        prefs(context).edit().putString(KEY_FLAGS, toJson(flags)).apply()
    }

    fun toJson(flags: Map<String, String>): String {
        val json = JSONObject()
        flags.toSortedMap().forEach { (key, value) -> json.put(key, value) }
        return json.toString(2)
    }

    fun fromJson(raw: String): Result<Map<String, String>> = runCatching {
        val json = JSONObject(raw)
        val map = LinkedHashMap<String, String>()
        json.keys().forEach { key ->
            when (val value = json.get(key)) {
                is Boolean -> map[key] = if (value) "True" else "False"
                is Number -> map[key] = value.toString()
                is String -> map[key] = value
                else -> throw IllegalArgumentException("Unsupported value for $key")
            }
        }
        map
    }

    fun isAutoApply(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_APPLY, true)

    fun setAutoApply(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_APPLY, enabled).apply()
    }
}
