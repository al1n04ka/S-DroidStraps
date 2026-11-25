package com.sdroidstraps.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sdroidstraps.launcher.data.FlagStore
import com.sdroidstraps.launcher.data.PRESETS
import com.sdroidstraps.launcher.data.flagTag

@Composable
fun FlagsScreen(
    flags: Map<String, String>,
    onUpdate: (Map<String, String>) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit
) {
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var jsonInput by remember { mutableStateOf("") }
    var jsonError by remember { mutableStateOf<String?>(null) }
    var showImport by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var exportNote by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    fun importJson(merge: Boolean) {
        FlagStore.fromJson(jsonInput).fold(
            onSuccess = { parsed ->
                onUpdate(if (merge) flags + parsed else parsed)
                jsonError = null
                jsonInput = ""
            },
            onFailure = { jsonError = it.message ?: "Invalid JSON" }
        )
    }

    ScreenColumn {
        ScreenTitle("Flags", "Written to /data/local/tmp/ClientAppSettings.json")
        HintText("Every flag is written as-is. Roblox only honors its official allowlist, the rest is still sent and may be ignored by the client.")

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PRESETS) { preset ->
                AssistChip(
                    onClick = { onUpdate(preset.flags) },
                    label = { Text(preset.title) },
                    shape = MaterialTheme.shapes.small
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { showImport = !showImport }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Import JSON")
            }
            TextButton(onClick = { showAdd = !showAdd }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add flag")
            }
        }

        if (showImport) {
            OutlinedTextField(
                value = jsonInput,
                onValueChange = {
                    jsonInput = it
                    jsonError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("JSON") },
                placeholder = {
                    Text(
                        "{\n  \"FFlagDebugSkyGray\": \"True\",\n  \"FIntDebugForceMSAASamples\": 1\n}"
                    )
                },
                minLines = 5,
                maxLines = 10,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
            if (jsonError != null) {
                Text(
                    jsonError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { importJson(merge = true) },
                    modifier = Modifier.weight(1f),
                    enabled = jsonInput.isNotBlank(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Merge")
                }
                TextButton(
                    onClick = { importJson(merge = false) },
                    modifier = Modifier.weight(1f),
                    enabled = jsonInput.isNotBlank()
                ) {
                    Text("Replace")
                }
            }
        }

        if (showAdd) {
            OutlinedTextField(
                value = newKey,
                onValueChange = { newKey = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Flag name") },
                placeholder = { Text("FFlagDebugSkyGray") },
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Value") },
                    placeholder = { Text("True") },
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        val key = newKey.trim()
                        if (key.isNotEmpty()) {
                            val value = newValue.trim().ifEmpty { "True" }
                            onUpdate(flags + (key to value))
                            newKey = ""
                            newValue = ""
                        }
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add flag")
                }
            }
        }

        Hairline()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel("Active · ${flags.size}")
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { if (exportFlagsJson(context, flags)) exportNote = "No share target — JSON copied to clipboard" }, enabled = flags.isNotEmpty()) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Export")
            }
        }
        if (exportNote != null) {
            HintText(exportNote ?: "")
        }

        SectionCard {
            if (flags.isEmpty()) {
                HintText("Empty: Roblox uses its default flags")
            } else {
                flags.toSortedMap().forEach { (key, value) ->
                    FlagRow(key = key, value = value, onRemove = { onUpdate(flags - key) })
                }
            }
        }

        Hairline()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onApply, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.small) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Apply")
            }
            TextButton(onClick = onClear, modifier = Modifier.weight(1f)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(6.dp))
                Text("Remove file", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun FlagRow(key: String, value: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                key,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "= $value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val tag = flagTag(key)
                if (tag != null) {
                    Text(
                        tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (tag == "allowlist") {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Remove flag",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
