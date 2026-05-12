package com.UniaccStressMonitor.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.UniaccStressMonitor.domain.model.StressLevel
import com.UniaccStressMonitor.domain.model.StressSession
import com.UniaccStressMonitor.presentation.sensors.SensorDataCollector
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

import com.UniaccStressMonitor.data.remote.AuthService
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val authService: AuthService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainNavigation()
                }
            }
        }
    }

    @Composable
    fun MainNavigation(viewModel: StressViewModel = koinViewModel()) {
        var isLoggedIn by remember { mutableStateOf(authService.isUserLoggedIn()) }
        var currentScreen by remember { mutableStateOf("dashboard") }

        if (isLoggedIn) {
            LaunchedEffect(isLoggedIn) {
                viewModel.refreshUser()
            }
            
            when (currentScreen) {
                "dashboard" -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToMonitor = { currentScreen = "monitor" },
                    onLogout = {
                        authService.logout()
                        isLoggedIn = false
                    }
                )
                "monitor" -> StressMonitorScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" },
                    onLogout = {
                        authService.logout()
                        isLoggedIn = false
                    }
                )
            }
        } else {
            LoginScreen(
                onLoginSuccess = { isLoggedIn = true },
                authService = authService
            )
        }
    }

    @Composable
    fun StressMonitorScreen(
        viewModel: StressViewModel = koinViewModel(),
        onBack: () -> Unit,
        onLogout: () -> Unit
    ) {
        val sessions by viewModel.recentSessions.collectAsState()
        var isMonitoring by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onBack) {
                            Text("← Volver", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Monitoreo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }
                    TextButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Salir", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Monitoring Control Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                if (isMonitoring) "Monitoreo ACTIVO" else "Monitoreo INACTIVO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isMonitoring) Color(0xFF2E7D32) else Color.Black
                            )
                            Text(
                                "Captura de sensores en tiempo real",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = isMonitoring,
                            onCheckedChange = {
                                isMonitoring = it
                                toggleService(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Eventos Recientes",
                    modifier = Modifier.align(Alignment.Start),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(sessions) { session ->
                        StressItem(session)
                    }
                }
            }
        }
    }

    @Composable
    fun StressItem(session: StressSession) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateStr = sdf.format(Date(session.timestamp))
        
        val color = when(session.stressLevel) {
            StressLevel.HIGH_STRESS -> Color(0xFFE53935) // Modern Red
            StressLevel.MEDIUM_STRESS -> Color(0xFFFFB300) // Modern Amber
            StressLevel.LOW_STRESS -> Color(0xFF43A047) // Modern Green
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color Indicator Circle
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.stressLevel.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Detectado a las $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                if (session.isSynced) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = CircleShape
                    ) {
                        Text(
                            "Sincronizado",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    private fun toggleService(start: Boolean) {
        val intent = Intent(this, SensorDataCollector::class.java)
        if (start) {
            startForegroundService(intent)
        } else {
            stopService(intent)
        }
    }
}
