package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vpn.VpnProfileConfig

@Composable
fun VpnConfigView(
    profile: VpnProfileConfig,
    logs: List<String>,
    onSaveProfile: (VpnProfileConfig) -> Unit,
    onImportOvpn: (String) -> Unit,
    onImportOvpnUri: (Uri) -> Unit,
    onClearLogs: () -> Unit,
    onCopyLogs: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var configSubTab by remember { mutableStateOf(0) } // 0: Settings, 1: .OVPN Import, 2: Terminal Logs

    var serverHost by remember(profile) { mutableStateOf(profile.serverHost) }
    var serverPort by remember(profile) { mutableStateOf(profile.serverPort.toString()) }
    var username by remember(profile) { mutableStateOf(profile.username) }
    var basePassword by remember(profile) { mutableStateOf(profile.basePassword) }
    var secretHex by remember(profile) { mutableStateOf(profile.totpSecretHex) }
    var secretBase32 by remember(profile) { mutableStateOf(profile.totpSecretBase32) }
    var attendanceUrl by remember(profile) { mutableStateOf(profile.attendanceUrl) }
    var ovpnText by remember(profile) { mutableStateOf(profile.ovpnConfigContent) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportOvpnUri(it) }
    }

    val listState = rememberLazyListState()

    // Auto scroll logs
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Sub Tab selector
        TabRow(
            selectedTabIndex = configSubTab,
            containerColor = Color(0xFF1E293B),
            contentColor = Color(0xFF38BDF8),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[configSubTab]),
                    color = Color(0xFF38BDF8)
                )
            }
        ) {
            Tab(
                selected = configSubTab == 0,
                onClick = { configSubTab = 0 },
                text = { Text("Account Credentials", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Config", modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = configSubTab == 1,
                onClick = { configSubTab = 1 },
                text = { Text(".OVPN File Import", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.FileDownload, contentDescription = "Import", modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = configSubTab == 2,
                onClick = { configSubTab = 2 },
                text = { Text("Live Terminal Logs", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Terminal, contentDescription = "Logs", modifier = Modifier.size(18.dp)) }
            )
        }

        when (configSubTab) {
            0 -> {
                // Account Credentials & Sophos XG Parameters
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Sophos XG VPN Configuration",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Set connection endpoints, TOTP parameters, and authentication credentials.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = serverHost,
                        onValueChange = { serverHost = it },
                        label = { Text("VPN Gateway Host") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = serverPort,
                        onValueChange = { serverPort = it },
                        label = { Text("Gateway Port") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = basePassword,
                        onValueChange = { basePassword = it },
                        label = { Text("Base Password (before TOTP)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = secretHex,
                        onValueChange = { secretHex = it },
                        label = { Text("TOTP Secret (HEX)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = secretBase32,
                        onValueChange = { secretBase32 = it },
                        label = { Text("TOTP Secret (Base32)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = attendanceUrl,
                        onValueChange = { attendanceUrl = it },
                        label = { Text("Default Attendance / Intranet Portal URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val updated = profile.copy(
                                serverHost = serverHost,
                                serverPort = serverPort.toIntOrNull() ?: 443,
                                username = username,
                                basePassword = basePassword,
                                totpSecretHex = secretHex,
                                totpSecretBase32 = secretBase32,
                                attendanceUrl = attendanceUrl
                            )
                            onSaveProfile(updated)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Account & Portal Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }

            1 -> {
                // .OVPN Config File Raw Editor & File Importer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Local SSL VPN Config (.ovpn)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Upload a .ovpn file directly or paste your OpenVPN / Sophos XG SSL profile text below.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Upload File Button
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("upload_ovpn_file_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Upload File")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload .ovpn File from Storage", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = ovpnText,
                        onValueChange = { ovpnText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        ),
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { ovpnText = "" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear")
                        }

                        Button(
                            onClick = { onImportOvpn(ovpnText) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_ovpn_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Import")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            2 -> {
                // Live Connection Handshake Console
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Connection Console Log",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Row {
                            IconButton(onClick = { onCopyLogs(logs.joinToString("\n")) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF38BDF8))
                            }
                            IconButton(onClick = onClearLogs) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color(0xFFEF4444))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                        color = Color(0xFF020617)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            items(logs) { line ->
                                val color = when {
                                    line.contains("SUCCESS") || line.contains("CONNECTED") -> Color(0xFF10B981)
                                    line.contains("ERROR") || line.contains("FAILED") -> Color(0xFFEF4444)
                                    line.contains("AUTH") || line.contains("OTP") -> Color(0xFF38BDF8)
                                    else -> Color(0xFFCBD5E1)
                                }
                                Text(
                                    text = line,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = color,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF1E293B),
    unfocusedContainerColor = Color(0xFF1E293B),
    focusedBorderColor = Color(0xFF38BDF8),
    unfocusedBorderColor = Color(0xFF334155),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color(0xFFCBD5E1),
    focusedLabelColor = Color(0xFF38BDF8),
    unfocusedLabelColor = Color(0xFF94A3B8)
)
