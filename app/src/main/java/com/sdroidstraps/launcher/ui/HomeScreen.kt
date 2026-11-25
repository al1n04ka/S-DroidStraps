package com.sdroidstraps.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    robloxInstalled: Boolean,
    flagCount: Int,
    shizukuState: Int,
    onLaunch: () -> Unit,
    onApply: () -> Unit,
    onOpenFlags: () -> Unit,
    onOpenHelper: () -> Unit
) {
    ScreenColumn {
        ScreenTitle("S-DroidStraps", "FastFlag launcher · Shizuku, no client mods")

        Button(
            onClick = onLaunch,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = robloxInstalled
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Launch Roblox")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onApply, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Apply")
            }
            TextButton(onClick = onOpenFlags, modifier = Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Flags")
            }
        }

        Hairline()
        SectionLabel("Status")

        StatusRow(
            icon = if (robloxInstalled) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            label = "Roblox",
            value = if (robloxInstalled) "installed" else "not found",
            tone = if (robloxInstalled) Tone.Ok else Tone.Warn
        )
        StatusRow(
            icon = if (shizukuState == 2) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            label = "Shizuku",
            value = when (shizukuState) {
                2 -> "ready"
                1 -> "access needed"
                else -> "not running"
            },
            tone = if (shizukuState == 2) Tone.Ok else Tone.Warn
        )
        StatusRow(
            icon = Icons.AutoMirrored.Filled.List,
            label = "Flags in profile",
            value = flagCount.toString()
        )

        if (shizukuState != 2) {
            TextButton(onClick = onOpenHelper) {
                Text("Set up Shizuku")
            }
        }
    }
}
