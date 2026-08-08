package com.example.robo_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.robo_app.ui.components.HudRing
import com.example.robo_app.ui.components.ProgressBar
import com.example.robo_app.ui.components.PulseLine
import com.example.robo_app.ui.theme.RvColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(900)
        onNavigateToDashboard()
    }

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
