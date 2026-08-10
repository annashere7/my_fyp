package com.example.robo_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.robo_app.ui.components.SecondaryButton
import com.example.robo_app.ui.components.StatePage
import com.example.robo_app.ui.theme.RvColors

@Composable
fun ServerUnavailableScreen(onRetry: () -> Unit, onCheckSettings: () -> Unit) {
    StatePage(title = "Server Unavailable", onBack = { onRetry() }) {
        ErrorCard(
            title = "Server Unavailable",
            message = "Unable to reach the server. Please check the server IP and try again.",
            primary = "RETRY",
            secondary = "CHECK SETTINGS",
            accent = RvColors.RedBright,
            onPrimaryClick = onRetry,
            onSecondaryClick = onCheckSettings
        )
    }
}

@Composable
fun ConnectionStateCard(title: String, message: String, detail: String, color: Color) {
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
fun ErrorCard(
    title: String, 
    message: String, 
    primary: String, 
    secondary: String, 
    accent: Color,
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {}
) {
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
        Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(4.dp)).background(accent).clickable { onPrimaryClick() }, contentAlignment = Alignment.Center) {
            Text(primary, color = if (accent == RvColors.GreenLight) Color(0xFF121C22) else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        SecondaryButton(secondary, Modifier.fillMaxWidth().height(46.dp)) { onSecondaryClick() }
    }
}
