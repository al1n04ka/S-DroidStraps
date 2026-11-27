package com.sdroidstraps.launcher.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sdroidstraps.launcher.data.ShellMode
import com.sdroidstraps.launcher.shell.ShizukuHelper

@Composable
fun HelperScreen(
    shizukuState: Int,
    rootMode: Boolean,
    shellMode: ShellMode,
    rootManager: String?,
    rootAvailable: Boolean,
    adbCommand: String,
    onRequestPermission: () -> Unit,
    onCheckRoot: () -> Unit,
    onOpenManager: () -> Unit,
    onShellMode: (ShellMode) -> Unit
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val shizukuInstalled = remember { ShizukuHelper.isInstalled(context) }

    fun openShizuku() {
        val market = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${ShizukuHelper.SHIZUKU_PACKAGE}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val web = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://shizuku.rikka.app/download/")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val launched = runCatching { context.startActivity(market) }
        if (launched.isFailure) {
            runCatching { context.startActivity(web) }
        }
    }

    fun copyCommand() {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("adb", adbCommand))
        copied = true
    }

    ScreenColumn {
        ScreenTitle("Shell access", "Shizuku · KernelSU · Magisk · APatch")

        SectionLabel("Shizuku")
        StatusRow(
            icon = if (shizukuInstalled) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            label = "Installed",
            value = if (shizukuInstalled) "yes" else "no",
            tone = if (shizukuInstalled) Tone.Ok else Tone.Warn
        )
        StatusRow(
            icon = if (shizukuState >= 1) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            label = "Service",
            value = if (shizukuState >= 1) "running" else "stopped",
            tone = if (shizukuState >= 1) Tone.Ok else Tone.Warn
        )
        StatusRow(
            icon = if (shizukuState == 2) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            label = "Access",
            value = if (shizukuState == 2) "granted" else "not granted",
            tone = if (shizukuState == 2) Tone.Ok else Tone.Warn
        )
        StatusRow(
            icon = Icons.Filled.Info,
            label = "Mode",
            value = when {
                rootMode -> "root (${rootManager ?: "su"})"
                shizukuState >= 1 -> "ADB"
                else -> "—"
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Request")
            }
            TextButton(onClick = { openShizuku() }, modifier = Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Open")
            }
        }

        Hairline()
        SectionLabel("Root")

        SectionCard {
            StatusRow(
                icon = if (rootManager != null) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                label = "Manager",
                value = rootManager ?: "not found",
                tone = if (rootManager != null) Tone.Ok else Tone.Warn
            )
            StatusRow(
                icon = if (rootAvailable) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                label = "Root shell",
                value = if (rootAvailable) "granted" else "not granted",
                tone = if (rootAvailable) Tone.Ok else Tone.Warn
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = onCheckRoot,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Grant root")
                }
                TextButton(onClick = onOpenManager, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (rootManager != null) "Open" else "Not found")
                }
            }
            HintText("Works with KernelSU, APatch, Magisk and SuperSU — approves the su prompt once, then applies flags without Shizuku.")
        }

        Hairline()
        SectionLabel("Shell method")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShellMode.entries.forEach { mode ->
                FilterChip(
                    selected = shellMode == mode,
                    onClick = { onShellMode(mode) },
                    label = { Text(mode.label) },
                    shape = MaterialTheme.shapes.small
                )
            }
        }
        HintText("Auto uses Shizuku when granted, otherwise root if available.")

        Hairline()
        SectionLabel("Setup")

        SectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberedRow(1, "Install Shizuku from the Play Store or shizuku.rikka.app.")
                NumberedRow(2, "On Android 11+ start it through Wireless debugging, right on the phone.")
                NumberedRow(3, "Come back here and tap \"Request\".")
                NumberedRow(4, "Apply flags and launch Roblox.")
            }
            HintText("Root is not required. Asset mods (skybox, sounds, fonts) are not possible on stock Android — only flags apply.")
        }

        Hairline()
        SectionLabel("No Shizuku · PC + ADB")

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            SelectionContainer {
                Text(
                    adbCommand,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        TextButton(onClick = { copyCommand() }) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (copied) "Copied" else "Copy command")
        }
        HintText("Run it again after a reboot or a Roblox update.")
    }
}
