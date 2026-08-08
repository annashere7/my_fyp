package com.example.robo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.robo_app.ui.theme.RoboappTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoboappTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = RvColors.Black
                ) { innerPadding ->
                    RobotVisionApp(
                        modifier = Modifier
                            .padding(innerPadding)
                            .safeDrawingPadding()
                    )
                }
            }
        }
    }
}

@Composable
fun RobotVisionApp(modifier: Modifier = Modifier) {
    var screen by remember { mutableStateOf(RobotScreen.Splash) }
    val state = MockStreamState()

    LaunchedEffect(Unit) {
        delay(900)
        screen = RobotScreen.Dashboard
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(RvColors.Black)
    ) {
        val isLandscape = maxWidth >= 700.dp
        when (screen) {
            RobotScreen.Splash -> SplashScreen()
            RobotScreen.Dashboard -> DashboardScreen(state, isLandscape, onNavigate = { screen = it })
            RobotScreen.Camera -> CameraScreen(state, isLandscape, onNavigate = { screen = it })
            RobotScreen.Monitoring -> MonitoringScreen(isLandscape, onNavigate = { screen = it })
            RobotScreen.Settings -> SettingsScreen(isLandscape, onNavigate = { screen = it })
            RobotScreen.ConnectionStates -> ConnectionStatesScreen(onBack = { screen = RobotScreen.Dashboard })
            RobotScreen.ErrorStates -> ErrorStatesScreen(onBack = { screen = RobotScreen.Dashboard })
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x3322C55E), RvColors.SplashBackground),
                    radius = 560f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HudRing()
            Spacer(modifier = Modifier.height(36.dp))
            Text("SYSTEM", color = RvColors.GreenLight, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("MONITORING", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            PulseLine()
            Spacer(modifier = Modifier.height(24.dp))
            Text("Monitor. Stream. Navigate.", color = RvColors.Muted, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(280.dp))
            ProgressBar()
            Spacer(modifier = Modifier.height(14.dp))
            Text("Initializing...", color = RvColors.Muted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun DashboardScreen(
    state: MockStreamState,
    isLandscape: Boolean,
    onNavigate: (RobotScreen) -> Unit
) {
    AppChrome(title = "Robot Vision", active = RobotScreen.Dashboard, onNavigate = onNavigate) {
        ResponsiveTwoPane(isLandscape = isLandscape) {
            CameraFeedCard(
                state = state,
                imageRes = R.drawable.dashboard_camera_feed,
                aspectRatio = 4f / 3f,
                modifier = Modifier.fillMaxWidth()
            )
            TelemetryGrid(state)
            StreamActionPanel(compact = false)
            ConnectionPerformanceRow(state)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryButton("CONNECTION STATES", Modifier.weight(1f)) { onNavigate(RobotScreen.ConnectionStates) }
                SecondaryButton("ERROR STATES", Modifier.weight(1f)) { onNavigate(RobotScreen.ErrorStates) }
            }
        }
    }
}

@Composable
private fun CameraScreen(
    state: MockStreamState,
    isLandscape: Boolean,
    onNavigate: (RobotScreen) -> Unit
) {
    AppChrome(title = "Camera", active = RobotScreen.Camera, onNavigate = onNavigate) {
        if (isLandscape) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
                CameraFeedCard(state, R.drawable.camera_stream_hallway, Modifier.weight(1.5f), 16f / 9f)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    StopCircleButton()
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
                CameraFeedCard(state, R.drawable.camera_stream_hallway, Modifier.fillMaxWidth(), 16f / 9f)
                StopCircleButton()
                SecondaryButton("Pause Stream", Modifier.fillMaxWidth())
                SecondaryButton("Switch Camera", Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun MonitoringScreen(isLandscape: Boolean, onNavigate: (RobotScreen) -> Unit) {
    AppChrome(title = "Monitoring", active = RobotScreen.Monitoring, onNavigate = onNavigate) {
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
private fun SettingsScreen(isLandscape: Boolean, onNavigate: (RobotScreen) -> Unit) {
    AppChrome(title = "Settings", active = RobotScreen.Settings, onNavigate = onNavigate) {
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
private fun ConnectionStatesScreen(onBack: () -> Unit) {
    StatePage(title = "7. CONNECTION\nSTATES", onBack = onBack) {
        ConnectionStateCard("Connected", "Connected to server", "192.168.1.100:5000", RvColors.Green)
        ConnectionStateCard("Connecting", "Connecting to server", "192.168.1.100:5000", RvColors.Blue)
        ConnectionStateCard("Disconnected", "Not connected to server", "CONNECT", RvColors.Yellow)
        ConnectionStateCard("Reconnecting", "Reconnecting...", "Attempt 2 / 5", RvColors.Purple)
        ConnectionStateCard("Connection\nFailed", "Failed to connect to server", "RETRY", RvColors.RedBright)
    }
}

@Composable
private fun ErrorStatesScreen(onBack: () -> Unit) {
    StatePage(title = "Error States", onBack = onBack) {
        ErrorCard(
            title = "Camera Permission\nRequired",
            message = "Camera permission is required to use this application.",
            primary = "GRANT PERMISSION",
            secondary = "GO TO SETTINGS",
            accent = RvColors.GreenLight
        )
        ErrorCard(
            title = "Server Unavailable",
            message = "Unable to reach the server. Please check the server IP and try again.",
            primary = "RETRY",
            secondary = "CHECK SETTINGS",
            accent = RvColors.RedBright
        )
    }
}

@Composable
private fun AppChrome(
    title: String,
    active: RobotScreen,
    onNavigate: (RobotScreen) -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(RvColors.Black)) {
        TopBar(title = title, status = if (active == RobotScreen.Settings) null else "STREAMING")
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            content()
        }
        BottomNav(active = active, onNavigate = onNavigate)
    }
}

@Composable
private fun ResponsiveTwoPane(isLandscape: Boolean, content: @Composable ColumnScope.() -> Unit) {
    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
private fun TopBar(title: String, status: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).background(RvColors.TopBar).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("<", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (status != null) StatusBadge(status)
            Spacer(Modifier.width(14.dp))
            Text("...", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CameraFeedCard(state: MockStreamState, imageRes: Int, modifier: Modifier, aspectRatio: Float) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, RvColors.Border, RoundedCornerShape(16.dp))
            .background(RvColors.Panel)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Mock rear camera feed",
            modifier = Modifier.fillMaxWidth().aspectRatio(aspectRatio).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        HudPill("FPS", state.fps, Modifier.align(Alignment.TopStart).padding(12.dp))
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
private fun HudPill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(RvColors.Hud)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 13.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = RvColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = RvColors.Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TelemetryGrid(state: MockStreamState) {
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
private fun StreamActionPanel(compact: Boolean) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (compact) StopCircleButton() else RedActionButton()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("PAUSE STREAM", Modifier.weight(1f))
            SecondaryButton("SWITCH CAMERA", Modifier.weight(1f))
        }
    }
}

@Composable
private fun RedActionButton() {
    Row(
        Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)).background(RvColors.Red),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(14.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
        Spacer(Modifier.width(12.dp))
        Text("STOP STREAMING", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StopCircleButton() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(80.dp).clip(CircleShape).background(RvColors.RedBright), contentAlignment = Alignment.Center) {
            Box(Modifier.size(24.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
        }
        Spacer(Modifier.height(12.dp))
        Text("STOP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Box(
        modifier = modifier.height(50.dp).clip(RoundedCornerShape(12.dp)).background(RvColors.Button)
            .border(1.dp, RvColors.Border, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ConnectionPerformanceRow(state: MockStreamState) {
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
private fun StatusBadge(status: String) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(RvColors.LiveBadge)
            .border(1.dp, RvColors.GreenDim, RoundedCornerShape(4.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(status, color = RvColors.Green, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(8.dp).clip(CircleShape).background(RvColors.RedBright))
    }
}

@Composable
private fun BottomNav(active: RobotScreen, onNavigate: (RobotScreen) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(80.dp).background(RvColors.Nav).border(1.dp, RvColors.Border).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(RobotScreen.Dashboard, RobotScreen.Camera, RobotScreen.Monitoring, RobotScreen.Settings).forEach { item ->
            val selected = item == active
            Column(Modifier.clickable { onNavigate(item) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)).background(if (selected) RvColors.Green else RvColors.Muted))
                Spacer(Modifier.height(6.dp))
                Text(item.label, color = if (selected) RvColors.Green else RvColors.Muted, fontSize = 10.sp)
                Spacer(Modifier.height(5.dp))
                Box(Modifier.width(if (selected) 32.dp else 0.dp).height(4.dp).clip(RoundedCornerShape(999.dp)).background(RvColors.Green))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = RvColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
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

@Composable
private fun StatePage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFF111827)).padding(24.dp)) {
        Row(Modifier.fillMaxWidth().clickable { onBack() }, verticalAlignment = Alignment.CenterVertically) {
            Text("<", color = Color.White, fontSize = 28.sp)
            Spacer(Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
        }
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
private fun ConnectionStateCard(title: String, message: String, detail: String, color: Color) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(12.dp)).padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Box(Modifier.size(20.dp).clip(CircleShape).background(color))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = color, fontSize = 18.sp, lineHeight = 24.sp)
            Text(message, color = RvColors.Muted, fontSize = 14.sp)
            Text(detail, color = RvColors.Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ErrorCard(title: String, message: String, primary: String, secondary: String, accent: Color) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF121C22)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(Color(0xFF6B7280)), contentAlignment = Alignment.BottomEnd) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(accent))
        }
        Spacer(Modifier.height(28.dp))
        Text(title, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 28.sp)
        Spacer(Modifier.height(18.dp))
        Text(message, color = RvColors.Muted, fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
        Spacer(Modifier.height(40.dp))
        Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(4.dp)).background(accent), contentAlignment = Alignment.Center) {
            Text(primary, color = if (accent == RvColors.GreenLight) Color(0xFF121C22) else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        SecondaryButton(secondary, Modifier.fillMaxWidth().height(46.dp))
    }
}

@Composable
private fun HudRing() {
    Box(Modifier.size(192.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(RvColors.Green.copy(alpha = 0.25f), style = Stroke(width = 3.dp.toPx()))
            drawCircle(RvColors.Green.copy(alpha = 0.45f), radius = size.minDimension * 0.38f, style = Stroke(width = 2.dp.toPx()))
        }
        Box(Modifier.size(98.dp).clip(CircleShape).background(Color(0xFF1E293B)).border(1.dp, Color(0xFF334155), CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).border(4.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp)))
        }
    }
}

@Composable
private fun PulseLine() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(70.dp).height(1.dp).background(RvColors.Green))
        Text("^", color = RvColors.Green, fontSize = 22.sp)
        Box(Modifier.width(70.dp).height(1.dp).background(RvColors.Green))
    }
}

@Composable
private fun ProgressBar() {
    Box(Modifier.width(280.dp).height(8.dp).clip(RoundedCornerShape(99.dp)).background(Color(0xFF1E293B))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(0.5f).clip(RoundedCornerShape(99.dp)).background(RvColors.GreenLight))
    }
}

private enum class RobotScreen(val label: String) {
    Splash("Splash"),
    Dashboard("Dashboard"),
    Camera("Camera"),
    Monitoring("Monitoring"),
    Settings("Settings"),
    ConnectionStates("Connection"),
    ErrorStates("Errors")
}

private data class MockStreamState(
    val previewWidth: Int = 1280,
    val previewHeight: Int = 720,
    val rotationDegrees: Int = 90,
    val fps: String = "20",
    val latencyMs: String = "42",
    val targetFps: Int = 20,
    val connectionStatus: String = "Connected",
    val endpoint: String = "192.168.1.100:5000"
)

private object RvColors {
    val Black = Color(0xFF000000)
    val SplashBackground = Color(0xFF0B1016)
    val TopBar = Color(0xFF070B0D)
    val Nav = Color(0xFF0A0E12)
    val Panel = Color(0xFF111827)
    val Card = Color(0xFF121214)
    val MonitorCard = Color(0xFF121820)
    val Button = Color(0xFF1E242B)
    val Hud = Color(0xCC0F141A)
    val Border = Color(0xFF2D3741)
    val Green = Color(0xFF22C55E)
    val GreenLight = Color(0xFF4ADE80)
    val GreenDim = Color(0x8014532D)
    val LiveBadge = Color(0xCC052E16)
    val Blue = Color(0xFF3B82F6)
    val Yellow = Color(0xFFEAB308)
    val Purple = Color(0xFFA855F7)
    val Red = Color(0xFFB91C1C)
    val RedBright = Color(0xFFEF4444)
    val Muted = Color(0xFF9CA3AF)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 884)
@Composable
fun RobotVisionPortraitPreview() {
    RoboappTheme { RobotVisionApp() }
}

@Preview(showBackground = true, widthDp = 900, heightDp = 430)
@Composable
fun RobotVisionLandscapePreview() {
    RoboappTheme { RobotVisionApp() }
}
