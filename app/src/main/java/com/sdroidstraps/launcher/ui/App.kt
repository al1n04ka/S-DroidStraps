package com.sdroidstraps.launcher.ui

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdroidstraps.launcher.data.Appearance
import com.sdroidstraps.launcher.data.AppearanceStore
import com.sdroidstraps.launcher.data.FlagStore
import com.sdroidstraps.launcher.data.ShellMode
import com.sdroidstraps.launcher.data.ShellModeStore
import com.sdroidstraps.launcher.data.UiStyle
import com.sdroidstraps.launcher.roblox.RobloxLauncher
import com.sdroidstraps.launcher.shell.RootHelper
import com.sdroidstraps.launcher.shell.ShizukuHelper
import com.sdroidstraps.launcher.shell.ShellResult
import com.sdroidstraps.launcher.ui.theme.SDroidStrapsTheme
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
    var appearance by remember { mutableStateOf(AppearanceStore.load(context)) }
    var showOnboarding by remember {
        mutableStateOf(!AppearanceStore.isOnboardingDone(context))
    }

    SDroidStrapsTheme(accent = appearance.accent, style = appearance.style) {
        AppShell(
            appearance = appearance,
            onStyle = {
                appearance = appearance.copy(style = it)
                AppearanceStore.saveStyle(context, it)
            },
            onAccent = {
                appearance = appearance.copy(accent = it)
                AppearanceStore.saveAccent(context, it)
            },
            showOnboarding = showOnboarding,
            onDismissOnboarding = {
                showOnboarding = false
                AppearanceStore.setOnboardingDone(context, true)
            },
            onShowOnboarding = { showOnboarding = true }
        )
    }
}

@Composable
private fun AppShell(
    appearance: Appearance,
    onStyle: (UiStyle) -> Unit,
    onAccent: (Long) -> Unit,
    showOnboarding: Boolean,
    onDismissOnboarding: () -> Unit,
    onShowOnboarding: () -> Unit
) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var flags by remember { mutableStateOf(FlagStore.load(context)) }
    var shizukuState by remember { mutableIntStateOf(0) }
    var rootMode by remember { mutableStateOf(false) }
    var shellMode by remember { mutableStateOf(ShellModeStore.load(context)) }
    var rootManager by remember { mutableStateOf(RootHelper.manager(context)) }
    var rootAvailable by remember { mutableStateOf(false) }
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

    fun checkRoot() {
        rootManager = RootHelper.manager(context)
        val label = rootManager?.label ?: "su"
        Thread {
            val ok = RootHelper.checkRoot()
            Handler(Looper.getMainLooper()).post {
                rootAvailable = ok
                toast(
                    if (ok) {
                        "Root granted via $label"
                    } else {
                        "No root access — allow this app in $label"
                    }
                )
            }
        }.start()
    }

    fun openRootManager() {
        val manager = rootManager ?: return
        val intent = context.packageManager.getLaunchIntentForPackage(manager.packageName)
        if (intent != null) {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            toast("Can't open ${manager.label}")
        }
    }

    fun writeFlags(): ShellResult {
        val json = FlagStore.toJson(flags)
        return when (shellMode) {
            ShellMode.Root -> RootHelper.writeFlags(json)
            ShellMode.Shizuku -> ShizukuHelper.writeFlags(json)
            ShellMode.Auto -> when {
                ShizukuHelper.hasPermission() -> ShizukuHelper.writeFlags(json)
                rootAvailable -> RootHelper.writeFlags(json)
                else -> ShellResult(false, "No shell access — set up Shizuku or grant root")
            }
        }
    }

    fun applyFlags(): Boolean {
        val result = writeFlags()
        if (!result.ok) toast(result.output)
        return result.ok
    }

    fun clearFlags() {
        val result = when (shellMode) {
            ShellMode.Root -> RootHelper.clearFlags()
            ShellMode.Shizuku -> ShizukuHelper.clearFlags()
            ShellMode.Auto -> if (ShizukuHelper.hasPermission()) {
                ShizukuHelper.clearFlags()
            } else if (rootAvailable) {
                RootHelper.clearFlags()
            } else {
                ShellResult(false, "No shell access — set up Shizuku or grant root")
            }
        }
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
    @Suppress("DEPRECATION")
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "1.3"
    }
    val shellLabel = when (shellMode) {
        ShellMode.Auto -> when {
            ShizukuHelper.hasPermission() -> "Auto · Shizuku"
            rootAvailable -> "Auto · Root"
            else -> "Auto · not ready"
        }

        ShellMode.Shizuku -> "Shizuku"
        ShellMode.Root -> "Root · ${
            if (rootAvailable) rootManager?.label ?: "su" else "not granted"
        }"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            when (appearance.style) {
                UiStyle.Bloxstrap -> StrapTopBar(
                    selected = tab,
                    onSelect = { tab = it },
                    shizukuState = shizukuState,
                    versionName = versionName
                )

                UiStyle.Terminal -> TerminalTopBar(
                    shizukuState = shizukuState,
                    versionName = versionName
                )

                else -> Unit
            }
        },
        bottomBar = {
            if (appearance.style != UiStyle.Bloxstrap) {
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
                    shellLabel = shellLabel,
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
                    shellMode = shellMode,
                    rootManager = rootManager?.label,
                    rootAvailable = rootAvailable,
                    adbCommand = adbCommand,
                    onRequestPermission = {
                        ShizukuHelper.requestPermission(PERMISSION_CODE)
                        refreshShizuku()
                    },
                    onCheckRoot = { checkRoot() },
                    onOpenManager = { openRootManager() },
                    onShellMode = {
                        shellMode = it
                        ShellModeStore.save(context, it)
                        if (it == ShellMode.Root && !rootAvailable) checkRoot()
                    }
                )

                else -> SettingsScreen(
                    appearance = appearance,
                    onStyle = onStyle,
                    onAccent = onAccent,
                    autoApply = autoApply,
                    onAutoApply = {
                        autoApply = it
                        FlagStore.setAutoApply(context, it)
                    },
                    flags = flags,
                    shizukuState = shizukuState,
                    onClearAll = { clearFlags() },
                    onShowOnboarding = onShowOnboarding
                )
            }
        }
    }

    if (showOnboarding) {
        ShizukuOnboardingDialog(
            shizukuState = shizukuState,
            onRequestPermission = {
                ShizukuHelper.requestPermission(PERMISSION_CODE)
                refreshShizuku()
            },
            onDismiss = onDismissOnboarding
        )
    }
}

@Composable
private fun StrapTopBar(
    selected: Int,
    onSelect: (Int) -> Unit,
    shizukuState: Int,
    versionName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "S-DroidStraps",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "v$versionName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ShizukuPill(shizukuState)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyRow(
            contentPadding = PaddingValues(horizontal = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(TABS) { index, item ->
                val active = selected == index
                Column(
                    modifier = Modifier
                        .clickable { onSelect(index) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        item.label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 0.8.sp,
                        color = if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth()
                            .background(
                                if (active) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalTopBar(shizukuState: Int, versionName: String) {
    val statusColor = when (shizukuState) {
        2 -> MaterialTheme.colorScheme.primary
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    val statusText = when (shizukuState) {
        2 -> "ready"
        1 -> "access needed"
        else -> "offline"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                "sdroidstraps v$versionName",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                Text(
                    "shizuku: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
                Text(
                    " _",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ShizukuPill(shizukuState: Int) {
    val dotColor = when (shizukuState) {
        2 -> MaterialTheme.colorScheme.secondary
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    val label = when (shizukuState) {
        2 -> "shizuku ready"
        1 -> "access needed"
        else -> "shizuku off"
    }

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
