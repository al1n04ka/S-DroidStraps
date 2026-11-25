package com.sdroidstraps.launcher.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sdroidstraps.launcher.data.FlagStore
import com.sdroidstraps.launcher.roblox.RobloxLauncher
import com.sdroidstraps.launcher.shell.ShizukuHelper
import rikka.shizuku.Shizuku

private const val PERMISSION_CODE = 4242

private data class Tab(val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("Home", Icons.Filled.Home),
    Tab("Flags", Icons.AutoMirrored.Filled.List),
    Tab("Mods", Icons.Filled.Star),
    Tab("Helper", Icons.Filled.Build),
    Tab("More", Icons.Filled.Settings)
)

@Composable
fun App() {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var flags by remember { mutableStateOf(FlagStore.load(context)) }
    var shizukuState by remember { mutableIntStateOf(0) }
    var rootMode by remember { mutableStateOf(false) }
    var autoApply by remember { mutableStateOf(FlagStore.isAutoApply(context)) }
    var robloxInstalled by remember { mutableStateOf(RobloxLauncher.isInstalled(context)) }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun refreshShizuku() {
        shizukuState = when {
            ShizukuHelper.hasPermission() -> 2
            ShizukuHelper.isRunning() -> 1
            else -> 0
        }
        rootMode = ShizukuHelper.isRoot()
    }

    DisposableEffect(Unit) {
        val onBinder = Shizuku.OnBinderReceivedListener { refreshShizuku() }
        val onDead = Shizuku.OnBinderDeadListener {
            shizukuState = 0
            rootMode = false
        }
        val onPermission = Shizuku.OnRequestPermissionResultListener { code, _ ->
            if (code == PERMISSION_CODE) refreshShizuku()
        }
        Shizuku.addBinderReceivedListener(onBinder)
        Shizuku.addBinderDeadListener(onDead)
        Shizuku.addRequestPermissionResultListener(onPermission)
        refreshShizuku()
        onDispose {
            Shizuku.removeBinderReceivedListener(onBinder)
            Shizuku.removeBinderDeadListener(onDead)
            Shizuku.removeRequestPermissionResultListener(onPermission)
        }
    }

    fun applyFlags(): Boolean {
        val result = ShizukuHelper.writeFlags(FlagStore.toJson(flags))
        if (!result.ok) toast(result.output)
        return result.ok
    }

    fun clearFlags() {
        val result = ShizukuHelper.clearFlags()
        if (result.ok) {
            flags = emptyMap()
            FlagStore.save(context, emptyMap())
        } else {
            toast(result.output)
        }
    }

    fun updateFlags(newFlags: Map<String, String>) {
        flags = newFlags
        FlagStore.save(context, newFlags)
    }

    fun launchRoblox() {
        if (autoApply) applyFlags()
        RobloxLauncher.launch(context).onFailure { toast("Couldn't launch: ${it.message}") }
        robloxInstalled = RobloxLauncher.isInstalled(context)
    }

    val adbCommand = remember(flags) { ShizukuHelper.adbCommand(FlagStore.toJson(flags)) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                TABS.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = {
                            Icon(item.icon, contentDescription = item.label)
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tab) {
                0 -> HomeScreen(
                    robloxInstalled = robloxInstalled,
                    flagCount = flags.size,
                    shizukuState = shizukuState,
                    onLaunch = { launchRoblox() },
                    onApply = { applyFlags() },
                    onOpenFlags = { tab = 1 },
                    onOpenHelper = { tab = 3 }
                )

                1 -> FlagsScreen(
                    flags = flags,
                    onUpdate = { updateFlags(it) },
                    onClear = { clearFlags() },
                    onApply = { applyFlags() }
                )

                2 -> ModsScreen(
                    flags = flags,
                    onUpdate = { updateFlags(it) }
                )

                3 -> HelperScreen(
                    shizukuState = shizukuState,
                    rootMode = rootMode,
                    adbCommand = adbCommand,
                    onRequestPermission = {
                        ShizukuHelper.requestPermission(PERMISSION_CODE)
                        refreshShizuku()
                    }
                )

                else -> SettingsScreen(
                    autoApply = autoApply,
                    onAutoApply = {
                        autoApply = it
                        FlagStore.setAutoApply(context, it)
                    },
                    flags = flags,
                    shizukuState = shizukuState,
                    onClearAll = { clearFlags() }
                )
            }
        }
    }
}
