package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TotpCard(
    totpCode: String,
    secondsRemaining: Int,
    timestepTotal: Int = 20,
    dynamicPassword: String,
    secretBase32: String,
    onCopyPassword: (String) -> Unit,
    onCopyTotp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = secondsRemaining.toFloat() / timestepTotal.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "totp_progress"
    )

    val ringColor by animateColorAsState(
        targetValue = if (secondsRemaining <= 4) Color(0xFFEF4444) else Color(0xFF38BDF8),
        label = "ring_color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)),
        color = Color(0xFF1E293B)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "TOTP Key",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Sophos 20s TOTP Token",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "shreya@contatan.local",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                // Countdown Timer Ring
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(44.dp),
                        color = ringColor,
                        trackColor = Color(0xFF334155),
                        strokeWidth = 3.5.dp
                    )
                    Text(
                        text = "${secondsRemaining}s",
                        color = ringColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Mono Code View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val part1 = if (totpCode.length >= 3) totpCode.substring(0, 3) else "---"
                        val part2 = if (totpCode.length >= 6) totpCode.substring(3, 6) else "---"

                        Text(
                            text = "$part1 $part2",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            letterSpacing = 4.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    IconButton(
                        onClick = { onCopyTotp(totpCode) },
                        modifier = Modifier.testTag("copy_totp_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy TOTP",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Password Preview Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto Password (Qwerty@1234 + TOTP):",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dynamicPassword,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF1F5F9),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = { onCopyPassword(dynamicPassword) },
                        modifier = Modifier.testTag("copy_dynamic_password_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Password",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
