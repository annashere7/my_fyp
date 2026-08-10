package com.example.robo_app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.robo_app.ui.theme.RvColors

@Composable
fun AppChrome(
    title: String,
    activeRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(RvColors.Black)) {
        TopBar(title = title, status = if (activeRoute == "SettingsRoute") null else "STREAMING")
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            content()
        }
        BottomNav(activeRoute = activeRoute, onNavigate = onNavigate)
    }
}

@Composable
fun ResponsiveTwoPane(isLandscape: Boolean, content: @Composable ColumnScope.() -> Unit) {
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
fun TopBar(title: String, status: String?) {
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
fun BottomNav(activeRoute: String, onNavigate: (String) -> Unit) {
    val items = listOf("DashboardRoute" to "Dashboard", "CameraRoute" to "Camera", "MonitoringRoute" to "Monitoring", "SettingsRoute" to "Settings")
    Row(
        Modifier.fillMaxWidth().height(80.dp).background(RvColors.Nav).border(1.dp, RvColors.Border).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (route, label) ->
            val selected = route == activeRoute
            Column(Modifier.clickable { onNavigate(route) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)).background(if (selected) RvColors.Green else RvColors.Muted))
                Spacer(Modifier.height(6.dp))
                Text(label, color = if (selected) RvColors.Green else RvColors.Muted, fontSize = 10.sp)
                Spacer(Modifier.height(5.dp))
                Box(Modifier.width(if (selected) 32.dp else 0.dp).height(4.dp).clip(RoundedCornerShape(999.dp)).background(RvColors.Green))
            }
        }
    }
}

@Composable
fun HudPill(label: String, value: String, modifier: Modifier = Modifier) {
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
fun StatusBadge(status: String) {
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
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
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
fun StopCircleButton() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(80.dp).clip(CircleShape).background(RvColors.RedBright), contentAlignment = Alignment.Center) {
            Box(Modifier.size(24.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
        }
        Spacer(Modifier.height(12.dp))
        Text("STOP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, color = RvColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
}

@Composable
fun StatePage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
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
fun HudRing() {
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
fun PulseLine() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(70.dp).height(1.dp).background(RvColors.Green))
        Text("^", color = RvColors.Green, fontSize = 22.sp)
        Box(Modifier.width(70.dp).height(1.dp).background(RvColors.Green))
    }
}

@Composable
fun ProgressBar() {
    Box(Modifier.width(280.dp).height(8.dp).clip(RoundedCornerShape(99.dp)).background(Color(0xFF1E293B))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(0.5f).clip(RoundedCornerShape(99.dp)).background(RvColors.GreenLight))
    }
}
