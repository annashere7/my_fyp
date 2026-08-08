package com.example.robo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.robo_app.navigation.*
import com.example.robo_app.ui.screens.*
import com.example.robo_app.ui.theme.RoboappTheme
import com.example.robo_app.ui.theme.RvColors

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
fun RobotVisionApp(modifier: Modifier = Modifier, viewModel: RobotVisionViewModel = viewModel()) {
    val navController = rememberNavController()
    val state = viewModel.uiState.collectAsState().value

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(RvColors.Black)
    ) {
        val isLandscape = maxWidth >= 700.dp

        NavHost(
            navController = navController,
            startDestination = SplashRoute
        ) {
            composable<SplashRoute> {
                SplashScreen(onNavigateToDashboard = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                })
            }
            composable<DashboardRoute> {
                DashboardScreen(state, isLandscape, viewModel = viewModel, onNavigate = { route ->
                    when (route) {
                        "DashboardRoute" -> navController.navigate(DashboardRoute)
                        "CameraRoute" -> navController.navigate(CameraRoute)
                        "MonitoringRoute" -> navController.navigate(MonitoringRoute)
                        "SettingsRoute" -> navController.navigate(SettingsRoute)
                        "ConnectionStatesRoute" -> navController.navigate(ConnectionStatesRoute)
                        "ErrorStatesRoute" -> navController.navigate(ErrorStatesRoute)
                    }
                })
            }
            composable<CameraRoute> {
                CameraScreen(state, isLandscape, viewModel = viewModel, onNavigate = { route ->
                    when (route) {
                        "DashboardRoute" -> navController.navigate(DashboardRoute)
                        "CameraRoute" -> navController.navigate(CameraRoute)
                        "MonitoringRoute" -> navController.navigate(MonitoringRoute)
                        "SettingsRoute" -> navController.navigate(SettingsRoute)
                    }
                })
            }
            composable<MonitoringRoute> {
                MonitoringScreen(isLandscape, onNavigate = { route ->
                    when (route) {
                        "DashboardRoute" -> navController.navigate(DashboardRoute)
                        "CameraRoute" -> navController.navigate(CameraRoute)
                        "MonitoringRoute" -> navController.navigate(MonitoringRoute)
                        "SettingsRoute" -> navController.navigate(SettingsRoute)
                    }
                })
            }
            composable<SettingsRoute> {
                SettingsScreen(isLandscape, onNavigate = { route ->
                    when (route) {
                        "DashboardRoute" -> navController.navigate(DashboardRoute)
                        "CameraRoute" -> navController.navigate(CameraRoute)
                        "MonitoringRoute" -> navController.navigate(MonitoringRoute)
                        "SettingsRoute" -> navController.navigate(SettingsRoute)
                    }
                })
            }
            composable<ConnectionStatesRoute> {
                ConnectionStatesScreen(onBack = { navController.popBackStack() })
            }
            composable<ErrorStatesRoute> {
                ErrorStatesScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
