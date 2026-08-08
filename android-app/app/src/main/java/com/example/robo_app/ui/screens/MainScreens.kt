package com.example.robo_app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.robo_app.data.model.StreamState
import com.example.robo_app.ui.components.*
import com.example.robo_app.ui.theme.RvColors

@Composable
fun DashboardScreen(
    state: StreamState,
    isLandscape: Boolean,
    viewModel: RobotVisionViewModel,
    onNavigate: (String) -> Unit
) {
    AppChrome(title = "Robot Vision", activeRoute = "DashboardRoute", onNavigate = onNavigate) {
        ResponsiveTwoPane(isLandscape = isLandscape) {
            CameraFeedCard(
                state = state,
                viewModel = viewModel,
                aspectRatio = 4f / 3f,
                modifier = Modifier.fillMaxWidth()
            )
            TelemetryGrid(state)
            StreamActionPanel(
                compact = false,
                isStreaming = state.connectionStatus == "CONNECTED",
                onStart = { viewModel.startStreaming() },
                onStop = { viewModel.stopStreaming() }
            )
            ConnectionPerformanceRow(state)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryButton("CONNECTION STATES", Modifier.weight(1f)) { onNavigate("ConnectionStatesRoute") }
                SecondaryButton("ERROR STATES", Modifier.weight(1f)) { onNavigate("ErrorStatesRoute") }
            }
        }
    }
}

@Composable
fun CameraScreen(
    state: StreamState,
    isLandscape: Boolean,
    viewModel: RobotVisionViewModel,
    onNavigate: (String) -> Unit
) {
    AppChrome(title = "Camera", activeRoute = "CameraRoute", onNavigate = onNavigate) {
        if (isLandscape) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
                CameraFeedCard(state, viewModel, Modifier.weight(1.5f), 16f / 9f)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    StopCircleButton(
                        isStreaming = state.connectionStatus == "CONNECTED",
                        onStart = { viewModel.startStreaming() },
                        onStop = { viewModel.stopStreaming() }
                    )
                    SecondaryButton("Pause Stream", Modifier.fillMaxWidth())
                    SecondaryButton("Switch Camera", Modifier.fillMaxWidth())
                    TelemetryGrid(state)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                CameraFeedCard(state, viewModel, Modifier.fillMaxWidth(), 16f / 9f)
                StopCircleButton(
                    isStreaming = state.connectionStatus == "CONNECTED",
                    onStart = { viewModel.startStreaming() },
                    onStop = { viewModel.stopStreaming() }
                )
                SecondaryButton("Pause Stream", Modifier.fillMaxWidth())
                SecondaryButton("Switch Camera", Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun MonitoringScreen(isLandscape: Boolean, onNavigate: (String) -> Unit) {
    AppChrome(title = "Monitoring", activeRoute = "MonitoringRoute", onNavigate = onNavigate) {
        ResponsiveTwoPane(isLandscape = isLandscape) {
            SectionTitle("STATUS")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatusCard("Connection", "Connected", Modifier.weight(1f))
                StatusCard("Streaming", "Active", Modifier.weight(1f))
                StatusCard("Camera", "Active", Modifier.weight(1f))
            }
            SectionTitle("CONNECTION STABILITY")
            ChartCard()
            SectionTitle("PERFORMANCE")
            MetricGrid()
        }
    }
}

@Composable
fun SettingsScreen(isLandscape: Boolean, onNavigate: (String) -> Unit) {
    AppChrome(title = "Settings", activeRoute = "SettingsRoute", onNavigate = onNavigate) {
        ResponsiveTwoPane(isLandscape = isLandscape) {
            SettingsGroup("SERVER SETTINGS") {
                SettingsRow("Server IP Address", "192.168.1.100")
                SettingsRow("Port", "5000")
            }
            SettingsGroup("STREAM SETTINGS") {
                SettingsRow("Resolution", "1280 x 720 (HD)")
                SettingsRow("Target FPS", "20 FPS")
                SettingsRow("Streaming Quality", "Medium")
                SettingsRow("Protocol", "WebSocket")
            }
            SettingsGroup("CAMERA SETTINGS") {
                SettingsRow("Camera", "Rear Camera")
                SettingsRow("Focus Mode", "Auto")
                TorchRow()
            }
        }
    }
}

@Composable
private fun CameraFeedCard(
    state: StreamState,
    viewModel: RobotVisionViewModel,
    modifier: Modifier,
    aspectRatio: Float
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, RvColors.Border, RoundedCornerShape(16.dp))
            .background(RvColors.Panel)
            .aspectRatio(aspectRatio)
    ) {
        if (hasCameraPermission) {
            CameraPreviewArea(
                modifier = Modifier.fillMaxSize(),
                analyzer = viewModel.frameAnalyzer
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera Permission Required", color = RvColors.Muted)
            }
        }

        HudPill("FPS", state.fps, Modifier.align(Alignment.TopStart).padding(12.dp))
        
        val isLive = state.connectionStatus == "CONNECTED"
        if (isLive) {
            Text(
                "LIVE",
                color = RvColors.Green,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 78.dp, top = 12.dp)
                    .clip(RoundedCornerShape(4.dp)).background(RvColors.LiveBadge)
                    .border(1.dp, RvColors.GreenDim, RoundedCornerShape(4.dp))
                    .padding(horizontal = 13.dp, vertical = 7.dp)
            )
        }
        
        Text(
            "${state.previewWidth} x ${state.previewHeight} | rotation ${state.rotationDegrees} deg",
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
                .clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun TelemetryGrid(state: StreamState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RvColors.Card),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth().padding(17.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TelemetryMetric("FPS", state.fps, "fps", RvColors.Green, Modifier.weight(1f))
            TelemetryMetric("LATENCY", state.latencyMs, "ms", RvColors.Blue, Modifier.weight(1f))
            TelemetryMetric("RESOLUTION", "1280\nx 720", "HD", Color.White, Modifier.weight(1f))
            TelemetryMetric("CAMERA", "Rear", "cam", Color.White, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TelemetryMetric(label: String, value: String, unit: String, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = RvColors.Muted, fontSize = 10.sp)
        Text(value, color = color, fontSize = if (value.contains('\n')) 18.sp else 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Text(unit, color = RvColors.Muted, fontSize = 10.sp)
    }
}

@Composable
private fun StreamActionPanel(compact: Boolean, isStreaming: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (compact) {
            StopCircleButton(isStreaming, onStart, onStop)
        } else {
            RedActionButton(isStreaming, onStart, onStop)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("PAUSE STREAM", Modifier.weight(1f))
            SecondaryButton("SWITCH CAMERA", Modifier.weight(1f))
        }
    }
}

@Composable
private fun RedActionButton(isStreaming: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    val bgColor = if (isStreaming) RvColors.Red else RvColors.Green
    val text = if (isStreaming) "STOP STREAMING" else "START STREAMING"
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { if (isStreaming) onStop() else onStart() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(14.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
        Spacer(Modifier.width(12.dp))
        Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StopCircleButton(isStreaming: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    val bgColor = if (isStreaming) RvColors.RedBright else RvColors.GreenLight
    val text = if (isStreaming) "STOP" else "START"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(80.dp).clip(CircleShape).background(bgColor)
            .clickable { if (isStreaming) onStop() else onStart() }, 
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(24.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
        }
        Spacer(Modifier.height(12.dp))
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ConnectionPerformanceRow(state: StreamState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoCard("CONNECTION", state.connectionStatus, state.endpoint, RvColors.Green, Modifier.weight(1f))
        InfoCard("TARGET FPS", "${state.targetFps} FPS", "configured", RvColors.Green, Modifier.weight(1f))
    }
}

@Composable
private fun InfoCard(label: String, value: String, detail: String, accent: Color, modifier: Modifier = Modifier) {
    Row(
        modifier.height(101.dp).clip(RoundedCornerShape(16.dp)).background(RvColors.Card)
            .border(1.dp, RvColors.Border, RoundedCornerShape(16.dp)).padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(accent.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(accent))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = RvColors.Muted, fontSize = 10.sp)
            Text(value, color = accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = RvColors.Muted, fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun StatusCard(label: String, value: String, modifier: Modifier) {
    Column(
        modifier.height(131.dp).clip(RoundedCornerShape(12.dp)).background(RvColors.MonitorCard)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).border(1.dp, RvColors.Border, CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(16.dp).clip(CircleShape).background(RvColors.Green))
        }
        Spacer(Modifier.height(12.dp))
        Text(label, color = RvColors.Muted, fontSize = 11.sp)
        Text(value, color = RvColors.GreenLight, fontSize = 14.sp)
    }
}

@Composable
private fun ChartCard() {
    Box(
        Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).background(RvColors.MonitorCard)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)).padding(16.dp)
    ) {
        Text("100%", color = RvColors.GreenLight, fontSize = 14.sp, modifier = Modifier.align(Alignment.TopEnd))
        Canvas(Modifier.fillMaxSize().padding(top = 24.dp, start = 30.dp, end = 4.dp, bottom = 8.dp)) {
            val grid = Color(0xFF1F2937)
            repeat(3) { i ->
                val y = size.height * i / 2f
                drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            val points = listOf(0.05f, 0.08f, 0.45f, 0.42f, 0.48f, 0.52f, 0.50f, 0.58f, 0.55f, 0.66f, 0.63f, 0.72f, 0.76f, 0.72f, 0.80f, 0.82f, 0.78f, 0.86f)
            val path = Path()
            points.forEachIndexed { index, value ->
                val x = size.width * index / (points.lastIndex)
                val y = size.height * (1f - value)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, RvColors.GreenLight, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun MetricGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PerfCard("FPS", "20", "fps", RvColors.GreenLight, Modifier.weight(1f))
            PerfCard("Latency", "42", "ms", RvColors.Blue, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PerfCard("Frames Sent", "1,245", "frames", Color.White, Modifier.weight(1f))
            PerfCard("Dropped Frames", "8", "frames", RvColors.RedBright, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PerfCard("Stream Duration", "00:05:23", "", Color.White, Modifier.weight(1f))
            PerfCard("Quality", "Excellent", "100%", RvColors.GreenLight, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PerfCard(label: String, value: String, unit: String, color: Color, modifier: Modifier) {
    Column(
        modifier.height(112.dp).clip(RoundedCornerShape(12.dp)).background(RvColors.MonitorCard)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)).padding(17.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFFD1D5DB), fontSize = 14.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = color, fontSize = if (value.length > 6) 20.sp else 30.sp, fontWeight = FontWeight.Bold)
            if (unit.isNotBlank()) {
                Spacer(Modifier.width(4.dp))
                Text(unit, color = Color(0xFF6B7280), fontSize = 12.sp, modifier = Modifier.padding(bottom = 5.dp))
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, rows: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF1E293B)), content = rows)
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().border(0.5.dp, Color(0xFF334155)).padding(16.dp)) {
        Text(label, color = Color(0xFFF8FAFC), fontSize = 14.sp)
        Text("$value  >", color = Color(0xFF94A3B8), fontSize = 14.sp)
    }
}

@Composable
private fun TorchRow() {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Enable Torch", color = Color(0xFFF8FAFC), fontSize = 14.sp)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.size(width = 44.dp, height = 24.dp).clip(RoundedCornerShape(99.dp)).background(Color(0xFF374151))) {
            Box(Modifier.padding(2.dp).size(20.dp).clip(CircleShape).background(Color.White))
        }
    }
}
