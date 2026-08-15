package com.sushant.ringcompanion.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sushant.ringcompanion.data.RingSnapshot
import com.sushant.ringcompanion.ui.theme.Gold
import com.sushant.ringcompanion.ui.theme.GoldSoft
import com.sushant.ringcompanion.ui.theme.Ink
import com.sushant.ringcompanion.ui.theme.Mist
import com.sushant.ringcompanion.ui.theme.Panel
import com.sushant.ringcompanion.ui.theme.PanelEdge
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private enum class Tab(val label: String) {
    Home("Home"),
    Sleep("Sleep"),
    Heart("Heart"),
    Gestures("Gestures")
}

@Composable
fun RingCompanionApp(vm: RingViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    val permissionLauncher = rememberLauncherForActivityResult(vm.permissionContract) {
        vm.onPermissionResult(it)
    }

    Scaffold(
        containerColor = Ink,
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Panel)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Tab.entries.forEachIndexed { index, item ->
                    val selected = tab == index
                    Text(
                        item.label,
                        color = if (selected) Gold else Mist,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { tab = index }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    ) { padding ->
        when (Tab.entries[tab]) {
            Tab.Home -> HomeScreen(padding, state.snapshot, state.loading, state.message, {
                permissionLauncher.launch(vm.repo.permissions)
            }, vm::refresh, vm.repo::openSamsungHealth, vm.repo::openGalaxyWearable, vm.repo::openOrInstallHealthConnect)
            Tab.Sleep -> SleepScreen(padding, state.snapshot)
            Tab.Heart -> HeartScreen(padding, state.snapshot)
            Tab.Gestures -> GestureScreen(padding, vm.repo::openGalaxyWearable, vm.repo::openSamsungHealth)
        }
    }
}

@Composable
private fun HomeScreen(
    padding: PaddingValues,
    snap: RingSnapshot,
    loading: Boolean,
    message: String?,
    onConnect: () -> Unit,
    onRefresh: () -> Unit,
    openHealth: () -> Unit,
    openWearable: () -> Unit,
    openHc: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Galaxy Ring", color = Mist, fontSize = 14.sp)
                    Text("Companion", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = Gold)
                }
            }
            Text(snap.sourceLabel, color = GoldSoft, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            RingHero(snap.energyScore)
            Text(snap.energyLabel, color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Medium)
            Text("Energy from last sleep and resting heart rate", color = Mist, fontSize = 13.sp)
            Spacer(Modifier.height(18.dp))
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Sleep", formatHours(snap.sleepHours), snap.sleepQuality, Modifier.weight(1f))
                StatCard("Resting HR", snap.restingHr?.let { "$it" } ?: "—", "bpm", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Steps", "%,d".format(Locale.US, snap.steps), "today", Modifier.weight(1f))
                StatCard("SpO₂", snap.spo2?.let { "$it%" } ?: "—", "latest", Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
        }
        item {
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (snap.usingSampleData) "Connect Health Connect" else "Manage health access")
            }
            TextButton(onClick = openHc) { Text("Open Health Connect settings", color = GoldSoft) }
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Gold)
            }
            message?.let { Text(it, color = GoldSoft, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp)) }
            Spacer(Modifier.height(8.dp))
            LinkRow("Samsung Health", Icons.Outlined.HealthAndSafety, openHealth)
            LinkRow("Galaxy Wearable / find ring", Icons.Outlined.Watch, openWearable)
            Spacer(Modifier.height(12.dp))
            snap.notes.forEach {
                Text("• $it", color = Mist, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SleepScreen(padding: PaddingValues, snap: RingSnapshot) {
    Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
        Text("Sleep", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Text(snap.sleepQuality, color = Gold)
        Spacer(Modifier.height(16.dp))
        StatCard("Last night", formatHours(snap.sleepHours), "hours in bed / asleep", Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        InfoCard(
            Icons.Outlined.Bedtime,
            "How Galaxy Ring sleep works",
            "The ring measures sleep, skin temperature, and overnight heart rate. Samsung Health scores the night. This app reads the same store through Health Connect."
        )
        Spacer(Modifier.height(12.dp))
        InfoCard(
            Icons.Outlined.Info,
            "Tip",
            "Wear the ring snug on your index or ring finger. Charge before bed if battery is low so overnight tracking is complete."
        )
    }
}

@Composable
private fun HeartScreen(padding: PaddingValues, snap: RingSnapshot) {
    Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
        Text("Heart", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Latest", snap.latestHr?.let { "$it" } ?: "—", "bpm", Modifier.weight(1f))
            StatCard("Resting", snap.restingHr?.let { "$it" } ?: "—", "bpm", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        StatCard("Calories", "${snap.calories}", "kcal today", Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        InfoCard(
            Icons.Outlined.FavoriteBorder,
            "Wellness only",
            "Heart rate, SpO₂, and energy here are for fitness and rest — not medical diagnosis. Irregular rhythm alerts stay in Samsung Health."
        )
    }
}

@Composable
private fun GestureScreen(padding: PaddingValues, openWearable: () -> Unit, openHealth: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
        Text("Ring controls", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Text("Gestures are handled by Galaxy Wearable, not this APK.", color = Mist, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        GestureRow("Pinch", "Answer or end calls, control media, or take a photo — depending on what you set.")
        GestureRow("Double pinch", "Next track / extra action after you assign it in Wearable.")
        GestureRow("Cover / dismiss", "Mute an alarm or dismiss an alert if enabled.")
        Spacer(Modifier.height(16.dp))
        InfoCard(
            Icons.Outlined.TouchApp,
            "Turn gestures on",
            "Open Galaxy Wearable → Galaxy Ring → Gestures. Pick which apps respond. This companion cannot flash firmware or pair the ring."
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = openWearable,
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Open Galaxy Wearable") }
        TextButton(onClick = openHealth) { Text("Open Samsung Health", color = GoldSoft) }
    }
}

@Composable
private fun RingHero(score: Int) {
    val spin by rememberInfiniteTransition(label = "ring").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "spin"
    )
    Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val c = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Color(0xFF1B1E27), radius = r * 0.62f)
            drawCircle(
                brush = Brush.sweepGradient(listOf(Gold, Color(0xFF7A6240), GoldSoft, Gold)),
                radius = r * 0.78f,
                style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
            )
            val rad = Math.toRadians((spin).toDouble())
            val spark = Offset(
                c.x + (r * 0.78f * cos(rad)).toFloat(),
                c.y + (r * 0.78f * sin(rad)).toFloat()
            )
            drawCircle(Color.White.copy(alpha = 0.9f), radius = 7.dp.toPx(), center = spark)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Light)
            Text("energy", color = Mist, fontSize = 13.sp)
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Panel)
            .border(1.dp, PanelEdge, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(title, color = Mist, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Text(caption, color = GoldSoft, fontSize = 12.sp)
    }
}

@Composable
private fun InfoCard(icon: ImageVector, title: String, body: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Panel)
            .padding(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Gold)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium)
            Text(body, color = Mist, fontSize = 13.sp)
        }
    }
}

@Composable
private fun GestureRow(title: String, body: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(title, color = Gold, fontWeight = FontWeight.Medium)
        Text(body, color = Mist, fontSize = 14.sp)
    }
}

@Composable
private fun LinkRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Panel),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Gold)
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = Color.White, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = Mist)
    }
}

private fun formatHours(hours: Double): String {
    if (hours <= 0.0) return "—"
    val h = hours.toInt()
    val m = ((hours - h) * 60).toInt()
    return "${h}h ${m}m"
}
