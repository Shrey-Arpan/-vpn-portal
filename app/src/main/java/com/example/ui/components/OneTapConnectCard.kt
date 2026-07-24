package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vpn.ConnectionStatus
import com.example.vpn.VpnSessionMetrics

@Composable
fun OneTapConnectCard(
    metrics: VpnSessionMetrics,
    serverHost: String,
    username: String,
    onTapToggle: () -> Unit,
    onLaunchAttendance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = metrics.status

    val isConnected = status == ConnectionStatus.CONNECTED
    val isConnecting = status == ConnectionStatus.CONNECTING || status == ConnectionStatus.AUTHENTICATING

    // Pulsing pulse animation for connecting state
    val infiniteTransition = rememberInfiniteTransition(label = "vpn_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val buttonBgColor by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.CONNECTED -> Color(0xFF10B981)
            ConnectionStatus.CONNECTING, ConnectionStatus.AUTHENTICATING -> Color(0xFFF59E0B)
            ConnectionStatus.FAILED -> Color(0xFFEF4444)
            else -> Color(0xFF0284C7)
        },
        label = "button_color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp)),
        color = Color(0xFF1E293B)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (status) {
                                ConnectionStatus.CONNECTED -> Color(0xFF10B981)
                                ConnectionStatus.CONNECTING, ConnectionStatus.AUTHENTICATING -> Color(0xFFF59E0B)
                                ConnectionStatus.FAILED -> Color(0xFFEF4444)
                                else -> Color(0xFF64748B)
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (status) {
                        ConnectionStatus.CONNECTED -> "CONNECTED • $serverHost"
                        ConnectionStatus.CONNECTING -> "CONNECTING TO $serverHost..."
                        ConnectionStatus.AUTHENTICATING -> "AUTHENTICATING TOTP..."
                        ConnectionStatus.FAILED -> "CONNECTION FAILED"
                        else -> "VPN DISCONNECTED"
                    },
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Big Glowing One-Tap Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(150.dp)
            ) {
                if (isConnecting) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(buttonBgColor.copy(alpha = 0.2f))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    buttonBgColor,
                                    buttonBgColor.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .clickable { onTapToggle() }
                        .testTag("one_tap_vpn_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.PowerSettingsNew,
                        contentDescription = "One-Tap Connect",
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Text Status Title
            Text(
                text = when (status) {
                    ConnectionStatus.CONNECTED -> "Sophos XG VPN Active"
                    ConnectionStatus.CONNECTING -> "Connecting to Server..."
                    ConnectionStatus.AUTHENTICATING -> "Validating TOTP Credential..."
                    ConnectionStatus.FAILED -> "Connection Failed"
                    else -> "Tap to Connect Sophos XG VPN"
                },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Account: $username | Target: http://attendance/",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Seamless Attendance Launch CTA
            Button(
                onClick = onLaunchAttendance,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("open_attendance_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) Color(0xFF10B981) else Color(0xFF334155),
                    contentColor = Color.White
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Attendance Portal",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isConnected) "Open Attendance Portal (http://attendance/)" else "One-Tap Connect & Open Attendance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Launch",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Extended stats when connected
            AnimatedVisibility(visible = isConnected) {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "DURATION",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val mins = metrics.connectedTimeSeconds / 60
                                val secs = metrics.connectedTimeSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "VIRTUAL IP",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = metrics.virtualIp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TRAFFIC",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val totalKb = (metrics.bytesReceived + metrics.bytesSent) / 1024
                                Text(
                                    text = "${totalKb} KB",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
