package com.sdroidstraps.launcher.roblox

import android.content.Context
import android.content.Intent

object RobloxLauncher {
    const val PACKAGE = "com.roblox.client"

    fun isInstalled(context: Context): Boolean =
        context.packageManager.getLaunchIntentForPackage(PACKAGE) != null

    fun launch(context: Context): Result<Unit> {
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE)
            ?: return Result.failure(IllegalStateException("Roblox is not installed"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent) }
    }
}
