package com.sdroidstraps.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sdroidstraps.launcher.data.Appearance
import com.sdroidstraps.launcher.data.COLOR_PRESETS
import com.sdroidstraps.launcher.data.parseHexColor
import com.sdroidstraps.launcher.data.UiStyle

@Composable
fun SettingsScreen(
    appearance: Appearance,
    onStyle: (UiStyle) -> Unit,
    onAccent: (Long) -> Unit,
    autoApply: Boolean,
    onAutoApply: (Boolean) -> Unit,
    flags: Map<String, String>,
    shizukuState: Int,
    onClearAll: () -> Unit,
    onShowOnboarding: () -> Unit
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.2"
    }

    val hsv = remember(appearance.accent) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(appearance.accent.toInt(), it) }
    }
    var hue by remember(appearance.accent) { mutableFloatStateOf(hsv[0]) }
    val customColor = Color.hsv(hue, 0.62f, 1f)
    val customAccent = customColor.toArgb().toLong() and 0xFFFFFFFFL
    var hexText by remember(appearance.accent) {
        mutableStateOf("#%06X".format(appearance.accent and 0xFFFFFF))
    }
    var hexError by remember { mutableStateOf<String?>(null) }

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
        SectionLabel("Appearance")
        SectionCard {
            Text("UI style", style = MaterialTheme.typography.bodyMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UiStyle.entries.forEach { style ->
                    FilterChip(
                        selected = appearance.style == style,
                        onClick = { onStyle(style) },
                        label = { Text(style.label) },
                        shape = MaterialTheme.shapes.small
                    )
                }
            }

            Spacer(Modifier.size(2.dp))
            Text("Accent color", style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                COLOR_PRESETS.forEach { preset ->
                    ColorDot(
                        color = Color(preset.accent),
                        selected = appearance.accent == preset.accent,
                        onClick = { onAccent(preset.accent) }
                    )
                }
            }

            Spacer(Modifier.size(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorDot(
                    color = customColor,
                    selected = appearance.accent == customAccent,
                    onClick = { onAccent(customAccent) }
                )
                Slider(
                    value = hue,
                    onValueChange = { hue = it },
                    onValueChangeFinished = {
                        onAccent(customAccent)
                    },
                    valueRange = 0f..360f,
                    modifier = Modifier.weight(1f)
                )
            }
            HintText("Drag the slider for a custom accent color")

            Spacer(Modifier.size(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = hexText,
                    onValueChange = {
                        hexText = it
                        hexError = null
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Hex color") },
                    placeholder = { Text("#7C5CFF") },
                    singleLine = true,
                    isError = hexError != null,
                    shape = MaterialTheme.shapes.small
                )
                TextButton(
                    onClick = {
                        val parsed = parseHexColor(hexText)
                        if (parsed == null) {
                            hexError = "Use #RRGGBB or #AARRGGBB"
                        } else {
                            onAccent(parsed)
                        }
                    }
                ) {
                    Text("Set")
                }
            }
            if (hexError != null) {
                Text(
                    hexError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Hairline()
        SectionLabel("Profile")
        SectionCard {
            StatusRow(
                icon = Icons.AutoMirrored.Filled.List,
                label = "Flags",
                value = flags.size.toString()
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
        }

        TextButton(
            onClick = { exportFlagsJson(context, flags) },
            enabled = flags.isNotEmpty()
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Export JSON")
        }

        Hairline()
        SectionLabel("About")
        SectionCard {
            HintText("S-DroidStraps $versionName — an open launcher that never modifies the client.")
            HintText("All flags from your profile are written, including ones outside Roblox's allowlist. Roblox itself ignores unlisted names — that check runs inside the client and no launcher can switch it off.")
            StatusRow(
                icon = Icons.Filled.Info,
                label = "Version",
                value = versionName
            )
        }

        TextButton(onClick = onShowOnboarding) {
            Text("Show setup guide")
        }

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
    }
}

@Composable
private fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
    )
}
