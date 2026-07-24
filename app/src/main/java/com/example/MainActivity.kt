package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AttendanceWebView
import com.example.ui.components.OneTapConnectCard
import com.example.ui.components.TotpCard
import com.example.ui.components.VpnConfigView
import com.example.ui.theme.MyApplicationTheme
import com.example.vpn.ConnectionStatus
import com.example.vpn.VpnViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VpnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: VpnViewModel) {
    val context = LocalContext.current

    val activeTab by viewModel.activeTab.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val logs by viewModel.logs.collectAsState()

    val currentTotp by viewModel.currentTotp.collectAsState()
    val secondsRemaining by viewModel.secondsRemaining.collectAsState()
    val dynamicPassword by viewModel.dynamicPassword.collectAsState()
    val attendanceUrl by viewModel.attendanceUrl.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sophos XG VPN & Attendance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "User: ${profile.username} | Host: ${profile.serverHost}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E293B)
                ),
                actions = {
                    Surface(
                        modifier = Modifier.padding(end = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = when (metrics.status) {
                            ConnectionStatus.CONNECTED -> Color(0xFF10B981).copy(alpha = 0.2f)
                            ConnectionStatus.CONNECTING, ConnectionStatus.AUTHENTICATING -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            else -> Color(0xFF64748B).copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = when (metrics.status) {
                                ConnectionStatus.CONNECTED -> "CONNECTED"
                                ConnectionStatus.CONNECTING, ConnectionStatus.AUTHENTICATING -> "CONNECTING"
                                else -> "OFFLINE"
                            },
                            color = when (metrics.status) {
                                ConnectionStatus.CONNECTED -> Color(0xFF10B981)
                                ConnectionStatus.CONNECTING, ConnectionStatus.AUTHENTICATING -> Color(0xFFF59E0B)
                                else -> Color(0xFF94A3B8)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFF38BDF8)
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = "One-Tap Connect") },
                    label = { Text("One-Tap Connect", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF0284C7).copy(alpha = 0.2f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_one_tap")
                )

                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Language, contentDescription = "Attendance Portal") },
                    label = { Text("http://attendance/", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF0284C7).copy(alpha = 0.2f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_attendance")
                )

                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "VPN Config") },
                    label = { Text("Config & Logs", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF0284C7).copy(alpha = 0.2f),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_config")
                )
            }
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> {
                    // One-Tap Home View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OneTapConnectCard(
                            metrics = metrics,
                            serverHost = profile.serverHost,
                            username = profile.username,
                            onTapToggle = { viewModel.toggleVpnConnection(context) },
                            onLaunchAttendance = { viewModel.oneTapConnectAndLaunchAttendance(context) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TotpCard(
                            totpCode = currentTotp,
                            secondsRemaining = secondsRemaining,
                            timestepTotal = profile.timestepSeconds.toInt(),
                            dynamicPassword = dynamicPassword,
                            secretBase32 = profile.totpSecretBase32,
                            onCopyPassword = { pass -> viewModel.copyToClipboard(context, pass, "Dynamic Password") },
                            onCopyTotp = { code -> viewModel.copyToClipboard(context, code, "20s TOTP") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                1 -> {
                    // Attendance Portal Browser View
                    AttendanceWebView(
                        url = profile.attendanceUrl,
                        bookmarks = profile.customBookmarks,
                        metrics = metrics,
                        onConnectVpn = { viewModel.toggleVpnConnection(context) },
                        onAddBookmark = { newBm -> viewModel.addBookmark(newBm) },
                        onRemoveBookmark = { oldBm -> viewModel.removeBookmark(oldBm) }
                    )
                }

                2 -> {
                    // VPN Configuration & Logs View
                    VpnConfigView(
                        profile = profile,
                        logs = logs,
                        onSaveProfile = { newProf -> viewModel.updateProfile(newProf) },
                        onImportOvpn = { text -> viewModel.importOvpnConfig(text, context) },
                        onImportOvpnUri = { uri -> viewModel.importOvpnFromUri(context, uri) },
                        onClearLogs = { viewModel.clearLogs() },
                        onCopyLogs = { text -> viewModel.copyToClipboard(context, text, "Terminal Logs") }
                    )
                }
            }
        }
    }
}
