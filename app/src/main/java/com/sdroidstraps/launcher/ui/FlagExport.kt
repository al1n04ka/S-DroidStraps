package com.sdroidstraps.launcher.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.sdroidstraps.launcher.data.FlagStore

fun exportFlagsJson(context: Context, flags: Map<String, String>): Boolean {
    val json = FlagStore.toJson(flags)
    val share = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "S-DroidStraps flags")
        putExtra(Intent.EXTRA_TEXT, json)
    }
    val launched = runCatching {
        context.startActivity(
            Intent.createChooser(share, "Export flags")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
    if (launched.isFailure) {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("S-DroidStraps flags", json))
        return true
    }
    return false
}
