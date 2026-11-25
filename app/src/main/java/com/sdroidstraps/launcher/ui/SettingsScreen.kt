package com.sdroidstraps.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    autoApply: Boolean,
    onAutoApply: (Boolean) -> Unit,
    flagCount: Int,
    shizukuState: Int,
    onClearAll: () -> Unit,
    status: String
) {
    ScreenColumn {
        ScreenTitle("More")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Write flags on launch", style = MaterialTheme.typography.bodyMedium)
                HintText("Rewrites the file from the current profile before every launch")
            }
            Switch(checked = autoApply, onCheckedChange = onAutoApply)
        }

        Hairline()
        SectionLabel("Profile")

        StatusRow(
            icon = Icons.AutoMirrored.Filled.List,
            label = "Flags",
            value = flagCount.toString()
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

        Hairline()
        SectionLabel("About")

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HintText("S-DroidStraps 1.0 — an open launcher that never modifies the client.")
            HintText("All flags from your profile are written, including ones outside Roblox's allowlist. Roblox itself ignores unlisted names — that check runs inside the client and no launcher can switch it off.")
            HintText("Not affiliated with Roblox Corporation. Use third-party tools at your own risk.")
        }

        StatusRow(
            icon = Icons.Filled.Info,
            label = "Version",
            value = "1.0"
        )

        TextButton(onClick = onClearAll) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(6.dp))
            Text("Reset flags", color = MaterialTheme.colorScheme.error)
        }

        Text(
            status,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
