package dev.mori.launcher

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import dev.mori.launcher.data.LocalMoriWidgetHost
import dev.mori.launcher.data.MoriLauncherActions
import dev.mori.launcher.data.MoriPreferences
import dev.mori.launcher.data.MoriWidgetHost
import dev.mori.launcher.data.MoriWorkspaceStore
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val Ink = Color(0xFF101713)
private val Leaf = Color(0xFF91A98C)
private val Paper = Color(0xFFF0F1E9)
private val Subtle = Color(0xFFB9C4B8)
private val Glass = Color(0xB91B2921)

data class MoriAppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    val isSystem: Boolean
)

class MainActivity : ComponentActivity() {
    private val moriWidgetHost by lazy { MoriWidgetHost(applicationContext) }
    private val widgetIds = mutableStateListOf<Int>()
    private val pinnedPackages = mutableStateListOf<String>()

    private fun persistPinned() {
        MoriWorkspaceStore.setPinnedPackages(this, pinnedPackages.toSet())
    }

    private fun togglePinned(packageName: String) {
        if (packageName in pinnedPackages) pinnedPackages.remove(packageName)
        else if (pinnedPackages.size < 8) pinnedPackages.add(packageName)
        persistPinned()
    }

    private fun movePinned(packageName: String, offset: Int) {
        val from = pinnedPackages.indexOf(packageName)
        val to = from + offset
        if (from < 0 || to !in pinnedPackages.indices) return
        val item = pinnedPackages.removeAt(from)
        pinnedPackages.add(to, item)
        persistPinned()
    }
    private var pendingWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private fun saveWidgetIds() {
        getSharedPreferences("mori_widgets", MODE_PRIVATE).edit()
            .putStringSet("ids", widgetIds.map { it.toString() }.toSet()).apply()
    }

    private fun addWidgetId(id: Int) {
        if (id != AppWidgetManager.INVALID_APPWIDGET_ID && id !in widgetIds) {
            widgetIds.add(id)
            saveWidgetIds()
        }
        pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private fun discardPendingWidget() {
        if (pendingWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            moriWidgetHost.deleteAppWidgetId(pendingWidgetId)
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        }
    }

    private fun launchWidgetPicker() {
        pendingWidgetId = moriWidgetHost.allocateAppWidgetId()
        val pick = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId)
        try { startActivityForResult(pick, REQUEST_PICK_WIDGET) }
        catch (_: Exception) { discardPendingWidget() }
    }

    private fun removeWidget(id: Int) {
        widgetIds.remove(id)
        saveWidgetIds()
        moriWidgetHost.deleteAppWidgetId(id)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_WIDGET) {
            if (resultCode != RESULT_OK) { discardPendingWidget(); return }
            val selectedId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId)
                ?: pendingWidgetId
            pendingWidgetId = selectedId
            val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(selectedId)
            if (provider?.configure != null) {
                try {
                    moriWidgetHost.startAppWidgetConfigureActivityForResult(
                        this, selectedId, 0, REQUEST_CONFIGURE_WIDGET, null
                    )
                } catch (_: Exception) { discardPendingWidget() }
            } else addWidgetId(selectedId)
        } else if (requestCode == REQUEST_CONFIGURE_WIDGET) {
            if (resultCode == RESULT_OK) addWidgetId(pendingWidgetId)
            else discardPendingWidget()
        }
    }

    companion object {
        private const val REQUEST_PICK_WIDGET = 7101
        private const val REQUEST_CONFIGURE_WIDGET = 7102
    }

    override fun onStart() {
        super.onStart()
        moriWidgetHost.startListening()
    }

    override fun onStop() {
        moriWidgetHost.stopListening()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        val savedIds = getSharedPreferences("mori_widgets", MODE_PRIVATE)
            .getStringSet("ids", emptySet()).orEmpty()
        widgetIds.addAll(savedIds.mapNotNull { it.toIntOrNull() })
        pinnedPackages.addAll(MoriWorkspaceStore.pinnedPackages(this))
        setContent {
            CompositionLocalProvider(LocalMoriWidgetHost provides moriWidgetHost) {
                MoriRoot(widgetIds, pinnedPackages, ::togglePinned, ::movePinned, ::launchWidgetPicker, ::removeWidget)
            }
        }
    }
}

@Composable
private fun MoriRoot(
    widgetIds: List<Int>,
    pinnedPackages: List<String>,
    onTogglePinned: (String) -> Unit,
    onMovePinned: (String, Int) -> Unit,
    onAddWidget: () -> Unit,
    onRemoveWidget: (Int) -> Unit
) {
    var page by remember { mutableStateOf("home") }
    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            AnimatedContent(targetState = page, label = "mori-page") { current ->
                when (current) {
                    "drawer" -> MoriDrawer(pinnedPackages, onTogglePinned, onBack = { page = "home" })
                    "settings" -> MoriSettings(onBack = { page = "home" })
                    "lock" -> MoriLockPreview(onBack = { page = "home" })
                    "widgets" -> MoriWidgetsScreen(widgetIds, { page = "home" }, onAddWidget, onRemoveWidget)
                    else -> MoriHome(
                        onDrawer = { page = "drawer" },
                        onSettings = { page = "settings" },
                        onLock = { page = "lock" },
                        onWidgets = { page = "widgets" },
                        pinnedPackages = pinnedPackages,
                        onMovePinned = onMovePinned,
                        onTogglePinned = onTogglePinned
                    )
                }
            }
        }
    }
}

@Composable
private fun MoriBackdrop(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF344B3A), Color(0xFF1C3026), Color(0xFF0B100D)))
    )) {
        Box(Modifier.align(Alignment.TopEnd).offset(x = 90.dp, y = 90.dp).size(300.dp)
            .blur(70.dp).background(Color(0x557D9A72), CircleShape))
        Box(Modifier.align(Alignment.BottomStart).offset(x = (-100).dp, y = 100.dp).size(260.dp)
            .blur(80.dp).background(Color(0x444E765B), CircleShape))
        content()
    }
}

@Composable
private fun ClockBlock() {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) { now = LocalDateTime.now(); delay(1000) }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(now.format(DateTimeFormatter.ofPattern("EEEE  ·  d MMMM")).uppercase(),
            color = Subtle, fontSize = 10.sp, letterSpacing = 2.4.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(now.format(DateTimeFormatter.ofPattern("HH:mm")), color = Paper,
            fontSize = 86.sp, lineHeight = 90.sp, letterSpacing = (-5).sp, fontWeight = FontWeight.Light)
    }
}

@Composable
private fun MoriHome(
    onDrawer: () -> Unit, onSettings: () -> Unit, onLock: () -> Unit, onWidgets: () -> Unit,
    pinnedPackages: List<String>,
    onMovePinned: (String, Int) -> Unit,
    onTogglePinned: (String) -> Unit
) {
    var islandOpen by remember { mutableStateOf(false) }
    var editingShortcuts by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val glassEnabled = remember { MoriPreferences.glassEnabled(context) }
    val dockLabelsEnabled = remember { MoriPreferences.labelsEnabled(context) }
    MoriBackdrop {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("mori", color = Paper, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (pinnedPackages.isNotEmpty()) {
                        Text(if (editingShortcuts) "DONE" else "EDIT",
                            color = Leaf, fontSize = 10.sp, letterSpacing = 1.2.sp,
                            modifier = Modifier.clickable { editingShortcuts = !editingShortcuts })
                    }
                    Icon(Icons.Default.Settings, "Settings", tint = Subtle,
                        modifier = Modifier.size(19.dp).clickable { onSettings() })
                }
            }
            Spacer(Modifier.height(24.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.clip(RoundedCornerShape(30.dp)).background(Color(0xDD080D0A))
                    .clickable { islandOpen = !islandOpen }
                    .padding(horizontal = 18.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Box(Modifier.size(7.dp).background(Leaf, CircleShape))
                        Text(if (islandOpen) "MORI  /  YOUR SPACE" else "•  MORI  •",
                            color = Subtle, fontSize = 9.sp, letterSpacing = 1.6.sp)
                        if (islandOpen) {
                            Icon(Icons.Default.MusicNote, "Open music", tint = Paper,
                                modifier = Modifier.size(15.dp).clickable { MoriLauncherActions.open(context, "music") })
                        }
                    }
                }
                AnimatedVisibility(islandOpen, enter = fadeIn(), exit = fadeOut()) {
                    Text("Tap the note to open your music app",
                        color = Subtle, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
            Spacer(Modifier.height(35.dp))
            ClockBlock()
            Spacer(Modifier.height(25.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(if (glassEnabled) Glass else Color(0xFF202B24))
                .clickable { MoriLauncherActions.open(context, "music") }
                .padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF71866B), Color(0xFF263B2E)))),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, null, tint = Paper, modifier = Modifier.size(25.dp))
                }
                Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
                    Text("Your music", color = Paper, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Tap to open your music app", color = Subtle, fontSize = 11.sp)
                }
                Icon(Icons.Default.ChevronRight, "Open music", tint = Paper, modifier = Modifier.size(20.dp))
            }
            if (pinnedPackages.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                PinnedAppsRow(pinnedPackages, editingShortcuts, onMovePinned, onTogglePinned)
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                MiniTile(Icons.Default.CalendarMonth, "Wallpaper", "Change", onClick = { MoriLauncherActions.open(context, "wallpaper") })
                MiniTile(Icons.Default.Fingerprint, "Lock", "Preview", onClick = onLock)
                MiniTile(Icons.Default.Apps, "Widgets", "Your widgets", onClick = onWidgets)
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp))
                .background(Color(0x55202D25)).padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                DockShortcut("Phone", "☎", dockLabelsEnabled) { MoriLauncherActions.open(context, "phone") }
                DockShortcut("Browser", "◉", dockLabelsEnabled) { MoriLauncherActions.open(context, "browser") }
                DockShortcut("Music", "♫", dockLabelsEnabled) { MoriLauncherActions.open(context, "music") }
                DockShortcut("Camera", "⌑", dockLabelsEnabled) { MoriLauncherActions.open(context, "camera") }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth().clickable { onDrawer() }
                .clip(RoundedCornerShape(18.dp)).background(Color(0x33202D25))
                .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = Subtle, modifier = Modifier.size(17.dp))
                Text("Search your apps", color = Subtle, fontSize = 12.sp,
                    modifier = Modifier.weight(1f).padding(start = 10.dp))
                Icon(Icons.Default.ChevronRight, null, tint = Subtle, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MiniTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String,
                     subtitle: String, onClick: () -> Unit = {}) {
    Column(Modifier.width(88.dp).clip(RoundedCornerShape(20.dp))
        .background(Color(0x44202D25)).clickable { onClick() }.padding(vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Leaf, modifier = Modifier.size(19.dp))
        Spacer(Modifier.height(7.dp))
        Text(title, color = Paper, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(subtitle, color = Subtle, fontSize = 9.sp)
    }
}

@Composable
private fun DockShortcut(name: String, glyph: String, showLabel: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable { onClick() }.padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(43.dp).clip(RoundedCornerShape(15.dp)).background(Color(0x554E6755)),
            contentAlignment = Alignment.Center) {
            Text(glyph, color = Paper, fontSize = 21.sp)
        }
        if (showLabel) {
            Spacer(Modifier.height(5.dp))
            Text(name, color = Subtle, fontSize = 9.sp)
        }
    }
}

@Composable
private fun PinnedAppsRow(
    packages: List<String>,
    editing: Boolean,
    onMove: (String, Int) -> Unit,
    onRemove: (String) -> Unit
) {
    val context = LocalContext.current
    val pinned = packages.mapNotNull { pkg ->
        runCatching {
            val appInfo = context.packageManager.getApplicationInfo(pkg, 0)
            Triple(pkg, context.packageManager.getApplicationLabel(appInfo).toString(), context.packageManager.getApplicationIcon(pkg))
        }.getOrNull()
    }
    Column(Modifier.fillMaxWidth()) {
        Text("YOUR SHORTCUTS", color = Subtle, fontSize = 9.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            pinned.forEach { (pkg, label, drawable) ->
                Column(Modifier.width(68.dp).clip(RoundedCornerShape(16.dp))
                    .clickable { context.packageManager.getLaunchIntentForPackage(pkg)?.let(context::startActivity) }
                    .padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val bitmap = remember(pkg) { drawableBitmap(drawable) }
                    androidx.compose.foundation.Image(bitmap.asImageBitmap(), contentDescription = label, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(5.dp))
                    Text(label, color = Paper, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (editing) {
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("←", color = Leaf, fontSize = 14.sp,
                                modifier = Modifier.clickable { onMove(pkg, -1) }.padding(3.dp))
                            Text("→", color = Leaf, fontSize = 14.sp,
                                modifier = Modifier.clickable { onMove(pkg, 1) }.padding(3.dp))
                            Text("×", color = Color(0xFFE5A39A), fontSize = 14.sp,
                                modifier = Modifier.clickable { onRemove(pkg) }.padding(3.dp))
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun MoriDrawer(
    pinnedPackages: List<String>,
    onTogglePinned: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("all") }
    var apps by remember { mutableStateOf(emptyList<MoriAppInfo>()) }
    LaunchedEffect(Unit) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = context.packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { info ->
                val label = info.loadLabel(context.packageManager)?.toString() ?: return@mapNotNull null
                MoriAppInfo(
                    label,
                    info.activityInfo.packageName,
                    info.loadIcon(context.packageManager),
                    (info.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }
    val filtered = remember(apps, query, filter) {
        apps.filter { app ->
            val matchesQuery = app.label.contains(query.trim(), ignoreCase = true) ||
                app.packageName.contains(query.trim(), ignoreCase = true)
            val matchesFilter = when (filter) {
                "user" -> !app.isSystem
                "system" -> app.isSystem
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }
    Column(Modifier.fillMaxSize().background(Ink).statusBarsPadding().navigationBarsPadding()
        .padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Paper, modifier = Modifier.clickable { onBack() })
            Text("  Library", color = Paper, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("${apps.size} apps", color = Subtle, fontSize = 11.sp)
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF202B24))
            .padding(horizontal = 15.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = Subtle, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            BasicTextField(value = query, onValueChange = { query = it }, singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Paper, fontSize = 14.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Leaf),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) Text("Find anything...", color = Subtle, fontSize = 14.sp)
                        inner()
                    }
                }, modifier = Modifier.weight(1f))
            if (query.isNotEmpty()) Icon(Icons.Default.Close, "Clear", tint = Subtle,
                modifier = Modifier.clickable { query = "" }.size(17.dp))
        }
        Spacer(Modifier.height(15.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all" to "All", "user" to "Downloaded", "system" to "System").forEach { (key, label) ->
                val selected = filter == key
                Text(label, color = if (selected) Ink else Subtle, fontSize = 11.sp,
                    modifier = Modifier.clip(RoundedCornerShape(18.dp))
                        .background(if (selected) Leaf else Color(0xFF202B24))
                        .clickable { filter = key }
                        .padding(horizontal = 13.dp, vertical = 8.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(if (query.isBlank()) "APPLICATIONS" else "SEARCH RESULTS",
            color = Subtle, fontSize = 9.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No apps found", color = Subtle, fontSize = 14.sp)
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                items(filtered, key = { it.packageName }) { app ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                        .combinedClickable(
                            onClick = {
                                context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                            },
                            onLongClick = {
                                val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + app.packageName))
                                runCatching { context.startActivity(details) }
                            }
                        ).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val bitmap = remember(app.packageName) { drawableBitmap(app.icon) }
                        androidx.compose.foundation.Image(bitmap.asImageBitmap(), contentDescription = app.label,
                            modifier = Modifier.size(46.dp))
                        Spacer(Modifier.height(7.dp))
                        Text(app.label, color = Paper, fontSize = 10.sp, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (app.isSystem) Text("SYS", color = Leaf.copy(alpha = 0.75f), fontSize = 7.sp)
                            Text(if (app.packageName in pinnedPackages) "PINNED" else "+ PIN",
                                color = if (app.packageName in pinnedPackages) Leaf else Subtle,
                                fontSize = 7.sp,
                                modifier = Modifier.clickable { onTogglePinned(app.packageName) }
                                    .padding(start = 4.dp, top = 4.dp, bottom = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun drawableBitmap(drawable: Drawable): Bitmap {
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width.coerceAtMost(192), height.coerceAtMost(192), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@Composable
private fun MoriWidgetsScreen(
    widgetIds: List<Int>,
    onBack: () -> Unit,
    onAddWidget: () -> Unit,
    onRemoveWidget: (Int) -> Unit
) {
    val context = LocalContext.current
    val host = LocalMoriWidgetHost.current
    val manager = remember { AppWidgetManager.getInstance(context) }
    Column(Modifier.fillMaxSize().background(Ink).statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Paper, modifier = Modifier.clickable { onBack() })
            Text("  Widgets", color = Paper, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text(widgetIds.size.toString(), color = Subtle, fontSize = 11.sp)
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF263B2E)).clickable { onAddWidget() }.padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Apps, null, tint = Leaf, modifier = Modifier.size(19.dp))
            Text("  Add a widget", color = Paper, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Subtle)
        }
        Spacer(Modifier.height(18.dp))
        if (widgetIds.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Apps, null, tint = Leaf, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Your space is still empty", color = Paper, fontSize = 15.sp)
                    Text("Add Android widgets from the system picker", color = Subtle, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(widgetIds, key = { it }) { id ->
                    val provider = remember(id) { manager.getAppWidgetInfo(id) }
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color(0xFF202B24)).padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(provider?.loadLabel(context.packageManager) ?: "Android widget", color = Paper, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("Remove", color = Leaf, fontSize = 11.sp, modifier = Modifier.clickable { onRemoveWidget(id) }.padding(horizontal = 8.dp, vertical = 6.dp))
                        }
                        if (host != null && provider != null) {
                            Spacer(Modifier.height(8.dp))
                            key(id) { AndroidView(factory = { host.createView(context, id, provider) }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)) }
                        } else {
                            Text("This widget is unavailable. Remove it and add it again.", color = Subtle, fontSize = 11.sp, modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun MoriSettings(onBack: () -> Unit) {
    val context = LocalContext.current
    var glass by remember { mutableStateOf(MoriPreferences.glassEnabled(context)) }
    var labels by remember { mutableStateOf(MoriPreferences.labelsEnabled(context)) }
    Column(Modifier.fillMaxSize().background(Ink).statusBarsPadding().padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Paper, modifier = Modifier.clickable { onBack() })
            Text("  Settings", color = Paper, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(26.dp))
        Text("PERSONALISE", color = Leaf, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        SettingRow("Frosted surfaces", "Soft translucent panels", glass) {
            glass = it
            MoriPreferences.setGlassEnabled(context, it)
        }
        SettingRow("Dock labels", "Show names under dock icons", labels) {
            labels = it
            MoriPreferences.setLabelsEnabled(context, it)
        }
        Spacer(Modifier.height(12.dp))
        Text("QUICK ACTIONS", color = Leaf, fontSize = 10.sp, letterSpacing = 2.sp)
        Text("Change wallpaper", color = Paper, fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().clickable { MoriLauncherActions.open(context, "wallpaper") }
                .padding(vertical = 16.dp))
        Text("Android settings", color = Paper, fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().clickable { MoriLauncherActions.open(context, "settings") }
                .padding(vertical = 16.dp))
        Spacer(Modifier.height(25.dp))
        Text("ABOUT MORI", color = Leaf, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        Text("Mori Launcher  ·  Experimental build", color = Paper, fontSize = 14.sp)
        Text("A quiet, forest-inspired home for Android.", color = Subtle, fontSize = 12.sp,
            modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Paper, fontSize = 14.sp)
            Text(subtitle, color = Subtle, fontSize = 11.sp)
        }
        Box(Modifier.size(width = 43.dp, height = 25.dp).clip(CircleShape)
            .background(if (enabled) Leaf else Color(0xFF465148)).clickable { onChange(!enabled) }
            .padding(3.dp), contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(Modifier.size(19.dp).background(if (enabled) Ink else Paper, CircleShape))
        }
    }
}

@Composable
private fun MoriLockPreview(onBack: () -> Unit) {
    MoriBackdrop {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Paper, modifier = Modifier.clickable { onBack() })
                Text("  Lock screen concept", color = Paper, fontSize = 15.sp)
            }
            Spacer(Modifier.weight(1f))
            ClockBlock()
            Spacer(Modifier.height(15.dp))
            Text("The forest is quiet tonight.", color = Subtle, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            Text("LOCK SCREEN PREVIEW  ·  NOT SYSTEM LOCK", color = Subtle,
                fontSize = 9.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(12.dp))
        }
    }
}
