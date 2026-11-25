package com.sdroidstraps.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sdroidstraps.launcher.data.MODS
import com.sdroidstraps.launcher.data.isModEnabled
import com.sdroidstraps.launcher.data.isModExperimental

@Composable
fun ModsScreen(
    flags: Map<String, String>,
    onUpdate: (Map<String, String>) -> Unit
) {
    ScreenColumn {
        ScreenTitle("Mods", "One-tap client tweaks")
        HintText("Toggles only edit your flag profile. Mods marked experimental are outside Roblox's allowlist and may be ignored by the client.")

        MODS.groupBy { it.category }.forEach { (category, mods) ->
            Hairline()
            SectionLabel(category)

            mods.forEach { mod ->
                val enabled = isModEnabled(flags, mod)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mod.title, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                        HintText(mod.description)
                        if (isModExperimental(mod)) {
                            Text(
                                "experimental · may be ignored",
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { on ->
                            onUpdate(if (on) flags + mod.flags else flags - mod.flags.keys)
                        }
                    )
                }
            }
        }
    }
}
