package dev.mori.launcher

import android.os.Bundle
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val Night = Color(0xFF101713)
private val Forest = Color(0xFF1D3028)
private val Glass = Color(0xAA18221E)
private val SoftWhite = Color(0xFFE9ECE7)
private val Muted = Color(0xFFB7C0B8)

private data class LaunchableApp(val label: String, val packageName: String, val className: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.BLACK
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContent { MoriApp() }
    }
}

@Composable
private fun MoriApp() {
    var screen by remember { mutableStateOf(false) }
    var drawer by remember { mutableStateOf(false) }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Night) {
            when {
                drawer -> AppDrawer(onBack = { drawer = false })
                screen -> LockPreview(onBack = { screen = false })
                else -> HomeScreen(onOpenLock = { screen = true }, onOpenDrawer = { drawer = true })
            }
        }
    }
}

@Composable
private fun LiveClock(pattern: String, size: Int, color: Color, weight: FontWeight = FontWeight.Light) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }
    Text(
        text = now.format(DateTimeFormatter.ofPattern(pattern)),
        color = color,
        fontSize = size.sp,
        fontWeight = weight,
        fontFamily = FontFamily.SansSerif,
        letterSpacing = (-2).sp,
        lineHeight = (size * 0.92f).sp
    )
}

@Composable
private fun LiveDate(pattern: String) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(60_000)
        }
    }
    Text(
        text = now.format(DateTimeFormatter.ofPattern(pattern)),
        color = Muted,
        fontSize = 12.sp,
        letterSpacing = 2.sp
    )
}

@Composable
private fun MoriBackdrop(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF35463B), Color(0xFF1A2822), Color(0xFF080B09))
                )
            )
    ) {
        // Placeholder atmosphere until the wallpaper picker and bundled image assets land.
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(Color(0x334F725D), Color.Transparent),
                    radius = 900f
                )
            )
        )
        content()
    }
}

@Composable
private fun HomeScreen(onOpenLock: () -> Unit, onOpenDrawer: () -> Unit) {
    var islandExpanded by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(true) }

    MoriBackdrop {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            DynamicIsland(expanded = islandExpanded, onClick = { islandExpanded = !islandExpanded }, playing = playing, onPlay = { playing = !playing })
            Spacer(Modifier.height(42.dp))
            LiveDate("EEEE  ·  MMMM")
            Spacer(Modifier.height(6.dp))
            LiveClock("HH:mm", 104, Color(0xDDE9ECE7))
            Spacer(Modifier.height(28.dp))
            MusicCard(playing = playing, onPlay = { playing = !playing })
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeviceWidget()
                CircleAction(Icons.Default.Fingerprint, "Lock preview", onOpenLock)
                CircleAction(Icons.Default.CameraAlt, "Camera") {}
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                    .background(Color(0x55232D28)).padding(vertical = 17.dp, horizontal = 26.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppGlyph("Phone", "☎")
                AppGlyph("Browser", "◉")
                AppGlyph("Music", "♫")
                AppGlyph("Photos", "❋")
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onOpenDrawer)) {
                Icon(Icons.Default.Search, null, tint = Muted, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(7.dp))
                Text("All apps  ·  Search", color = Muted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text("Hold an empty space to customise  ·  Mori 0.1", color = Muted.copy(alpha = .55f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun DynamicIsland(expanded: Boolean, onClick: () -> Unit, playing: Boolean, onPlay: () -> Unit) {
    val width by animateFloatAsState(if (expanded) 0.94f else 0.48f, label = "island-width")
    Column(
        modifier = Modifier.fillMaxWidth(width).clip(RoundedCornerShape(34.dp))
            .background(Color(0xFF050706)).clickable(onClick = onClick).padding(horizontal = 17.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF8CA58E)))
                if (expanded) {
                    Spacer(Modifier.width(9.dp))
                    Text("MORI  ·  NOW PLAYING", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(4) { index ->
                    Box(Modifier.padding(start = 2.dp).width(2.dp).height((5 + index * 3).dp).clip(CircleShape).background(SoftWhite.copy(alpha = .8f)))
                }
            }
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Row(Modifier.fillMaxWidth().padding(top = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(9.dp)).background(Forest), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, null, tint = SoftWhite)
                }
                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text("Ambient Forest", color = SoftWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("Mori Radio · Demo", color = Muted, fontSize = 11.sp)
                }
                Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = SoftWhite, modifier = Modifier.size(25.dp).clickable(onClick = onPlay))
            }
        }
    }
}

@Composable
private fun MusicCard(playing: Boolean, onPlay: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp))
            .background(Glass).padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(54.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(Color(0xFF687B69), Color(0xFF1B2922)))), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Headphones, null, tint = SoftWhite, modifier = Modifier.size(28.dp))
        }
        Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
            Text("A quiet place", color = SoftWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("Mori Radio  ·  Your soundtrack", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(9.dp))
            Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(Color.White.copy(alpha = .16f))) {
                Box(Modifier.fillMaxWidth(.34f).height(3.dp).clip(CircleShape).background(SoftWhite))
            }
        }
        Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = SoftWhite, modifier = Modifier.size(28.dp).clickable(onClick = onPlay))
    }
}

@Composable
private fun DeviceWidget() {
    Column(Modifier.width(145.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Headphones, null, tint = SoftWhite, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text("Connected", color = SoftWhite, fontSize = 13.sp)
        }
        Text("Your devices", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Color.White.copy(alpha = .16f))) {
            Box(Modifier.fillMaxWidth(.72f).height(5.dp).clip(CircleShape).background(SoftWhite))
        }
    }
}

@Composable
private fun CircleAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(Modifier.size(54.dp).clip(CircleShape).background(Color(0x665D6C61)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = SoftWhite, modifier = Modifier.size(23.dp))
        }
        Text(label, color = Muted, fontSize = 9.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun AppGlyph(name: String, glyph: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Color(0x665C6A60)), contentAlignment = Alignment.Center) {
            Text(glyph, color = SoftWhite, fontSize = 25.sp)
        }
        Text(name, color = Muted, fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
    }
}


@Composable
private fun AppDrawer(onBack: () -> Unit) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var query by remember { mutableStateOf("") }
    val apps = remember(context) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        packageManager.queryIntentActivities(intent, 0)
            .map { info ->
                LaunchableApp(
                    label = info.loadLabel(packageManager).toString(),
                    packageName = info.activityInfo.packageName,
                    className = info.activityInfo.name
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
    val filtered = apps.filter { it.label.contains(query, ignoreCase = true) }

    MoriBackdrop {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 26.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowBack, "Back", tint = SoftWhite, modifier = Modifier.size(24.dp).clickable(onClick = onBack))
                Spacer(Modifier.width(16.dp))
                Text("Your apps", color = SoftWhite, fontSize = 25.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.weight(1f))
                Text(filtered.size.toString(), color = Muted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search installed apps", color = Muted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(Modifier.height(18.dp))
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No matching apps", color = Muted)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(filtered, key = { it.packageName }) { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                val launch = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_LAUNCHER)
                                    setClassName(app.packageName, app.className)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                runCatching { context.startActivity(launch) }
                            }
                        ) {
                            AppIcon(app = app)
                            Spacer(Modifier.height(7.dp))
                            Text(app.label, color = SoftWhite, fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 12.sp)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun AppIcon(app: LaunchableApp) {
    val context = LocalContext.current
    val icon = remember(app.packageName) {
        runCatching {
            val drawable = context.packageManager.getApplicationIcon(app.packageName)
            val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }.getOrNull()
    }
    Box(
        Modifier.size(58.dp).clip(RoundedCornerShape(19.dp))
            .background(Color(0x665D7062)),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(bitmap = icon, contentDescription = app.label, modifier = Modifier.fillMaxSize().padding(5.dp))
        } else {
            Text(app.label.take(1).uppercase(), color = SoftWhite, fontSize = 25.sp, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
private fun LockPreview(onBack: () -> Unit) {
    MoriBackdrop {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = SoftWhite, modifier = Modifier.size(24.dp).clickable(onClick = onBack))
                }
                Spacer(Modifier.height(74.dp))
                LiveDate("EEEE  ·  MMMM d")
                Spacer(Modifier.height(10.dp))
                LiveClock("HH:mm", 112, Color(0xE6F0F1E9))
                Text("A little quiet goes a long way.", color = SoftWhite.copy(alpha = .75f), fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                MusicCard(playing = true, onPlay = {})
                Spacer(Modifier.height(16.dp))
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Glass).padding(18.dp)) {
                    Text("NOTIFICATIONS", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Nothing urgent.", color = SoftWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Your quiet space is waiting.", color = Muted, fontSize = 12.sp)
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    CircleAction(Icons.Default.FlashlightOn, "Flashlight") {}
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Fingerprint, null, tint = SoftWhite.copy(alpha = .9f), modifier = Modifier.size(34.dp))
                        Text("Preview only · Android handles secure unlock", color = Muted, fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                    }
                    CircleAction(Icons.Default.CameraAlt, "Camera") {}
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
