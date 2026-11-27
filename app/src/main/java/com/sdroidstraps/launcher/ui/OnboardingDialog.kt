package com.sdroidstraps.launcher.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sdroidstraps.launcher.shell.ShizukuHelper

@Composable
fun ShizukuOnboardingDialog(
    shizukuState: Int,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val installed = ShizukuHelper.isInstalled(context)

    fun openShizuku() {
        val market = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${ShizukuHelper.SHIZUKU_PACKAGE}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val web = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://shizuku.rikka.app/download/")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (runCatching { context.startActivity(market) }.isFailure) {
            runCatching { context.startActivity(web) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Set up Shizuku", style = MaterialTheme.typography.headlineSmall)
                HintText("S-DroidStraps writes flags through Shizuku, no root needed. One-time setup.")
                HintText("Already rooted with KernelSU, APatch or Magisk? Choose Root in Helper instead.")

                NumberedRow(1, "Install Shizuku from the Play Store or shizuku.rikka.app.")
                NumberedRow(2, "Start it with Wireless debugging, right on the phone.")
                NumberedRow(3, "Grant access, then apply flags and launch Roblox.")

                Hairline()
                StatusRow(
                    icon = if (installed) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    label = "Shizuku",
                    value = if (installed) "installed" else "not installed",
                    tone = if (installed) Tone.Ok else Tone.Warn
                )
                StatusRow(
                    icon = if (shizukuState == 2) Icons.Filled.CheckCircle else Icons.Filled.Info,
                    label = "Access",
                    value = when (shizukuState) {
                        2 -> "granted"
                        1 -> "needs permission"
                        else -> "service not running"
                    },
                    tone = if (shizukuState == 2) Tone.Ok else Tone.Warn
                )

                Spacer(Modifier.size(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Later")
                    }
                    if (!installed) {
                        Button(
                            onClick = { openShizuku() },
                            modifier = Modifier.weight(1.4f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Install Shizuku")
                        }
                    } else if (shizukuState != 2) {
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier.weight(1.4f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Request access")
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1.4f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Continue")
                        }
                    }
                }
            }
        }
    }
}
