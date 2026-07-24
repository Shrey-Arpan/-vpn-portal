package com.example.vpn

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.totp.TotpManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VpnViewModel : ViewModel() {

    val profile: StateFlow<VpnProfileConfig> = XgVpnService.profile
    val metrics: StateFlow<VpnSessionMetrics> = XgVpnService.metrics
    val logs: StateFlow<List<String>> = XgVpnService.logs

    private val _currentTotp = MutableStateFlow("------")
    val currentTotp: StateFlow<String> = _currentTotp.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(20)
    val secondsRemaining: StateFlow<Int> = _secondsRemaining.asStateFlow()

    private val _dynamicPassword = MutableStateFlow("Qwerty@1234------")
    val dynamicPassword: StateFlow<String> = _dynamicPassword.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: One-Tap Home, 1: Attendance Web, 2: Config & OVPN
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _attendanceUrl = MutableStateFlow("http://attendance/")
    val attendanceUrl: StateFlow<String> = _attendanceUrl.asStateFlow()

    private val _isWebViewLoading = MutableStateFlow(false)
    val isWebViewLoading: StateFlow<Boolean> = _isWebViewLoading.asStateFlow()

    init {
        startTotpTicker()
    }

    private fun startTotpTicker() {
        viewModelScope.launch {
            while (true) {
                val currentProf = profile.value
                val totp = TotpManager.generateTotp(
                    secretInput = currentProf.totpSecretHex,
                    periodSeconds = currentProf.timestepSeconds
                )
                val remaining = TotpManager.getSecondsRemaining(periodSeconds = currentProf.timestepSeconds)
                val fullPass = TotpManager.buildDynamicPassword(
                    basePassword = currentProf.basePassword,
                    secretInput = currentProf.totpSecretHex,
                    periodSeconds = currentProf.timestepSeconds
                )

                _currentTotp.value = totp
                _secondsRemaining.value = remaining
                _dynamicPassword.value = fullPass

                delay(500)
            }
        }
    }

    fun selectTab(index: Int) {
        _activeTab.value = index
    }

    fun updateProfile(newProfile: VpnProfileConfig) {
        XgVpnService.updateProfile(newProfile)
    }

    fun setAttendanceUrl(url: String) {
        _attendanceUrl.value = url
    }

    fun setWebViewLoading(loading: Boolean) {
        _isWebViewLoading.value = loading
    }

    fun toggleVpnConnection(context: Context) {
        val currentStatus = metrics.value.status
        if (currentStatus == ConnectionStatus.CONNECTED || currentStatus == ConnectionStatus.CONNECTING) {
            val intent = Intent(context, XgVpnService::class.java).apply {
                action = XgVpnService.ACTION_DISCONNECT
            }
            context.startService(intent)
        } else {
            // Check if VpnService permission is needed
            val prepareIntent = VpnService.prepare(context)
            if (prepareIntent != null) {
                XgVpnService.log("Requesting VPN permission from system...")
                // In actual execution, activity launch is handled viaActivityResult, but we start service directly
                val intent = Intent(context, XgVpnService::class.java).apply {
                    action = XgVpnService.ACTION_CONNECT
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                val intent = Intent(context, XgVpnService::class.java).apply {
                    action = XgVpnService.ACTION_CONNECT
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }

    fun oneTapConnectAndLaunchAttendance(context: Context) {
        if (metrics.value.status != ConnectionStatus.CONNECTED) {
            toggleVpnConnection(context)
        }
        selectTab(1) // Open Attendance Portal tab
    }

    fun clearLogs() {
        XgVpnService.clearLogs()
    }

    fun copyToClipboard(context: Context, text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun importOvpnConfig(ovpnText: String, context: Context) {
        if (ovpnText.isBlank()) {
            Toast.makeText(context, "Config is empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse host or remote lines if present
        var extractedHost = profile.value.serverHost
        var extractedPort = profile.value.serverPort

        val lines = ovpnText.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("remote ")) {
                val parts = trimmed.split("\\s+".toRegex())
                if (parts.size >= 2) extractedHost = parts[1]
                if (parts.size >= 3) parts[2].toIntOrNull()?.let { extractedPort = it }
            }
        }

        val updated = profile.value.copy(
            serverHost = extractedHost,
            serverPort = extractedPort,
            ovpnConfigContent = ovpnText
        )
        updateProfile(updated)
        XgVpnService.log("Imported .ovpn config file successfully! Target Host: $extractedHost:$extractedPort")
        Toast.makeText(context, "Imported .ovpn config ($extractedHost)", Toast.LENGTH_SHORT).show()
    }

    fun importOvpnFromUri(context: Context, uri: android.net.Uri) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            }
            if (!content.isNullOrBlank()) {
                importOvpnConfig(content, context)
            } else {
                Toast.makeText(context, "Failed to read file or file is empty", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            XgVpnService.log("Error reading file: ${e.message}")
            Toast.makeText(context, "Error reading file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun addBookmark(url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return
        val currentBookmarks = profile.value.customBookmarks
        if (!currentBookmarks.contains(trimmed)) {
            val updated = profile.value.copy(customBookmarks = currentBookmarks + trimmed)
            updateProfile(updated)
        }
    }

    fun removeBookmark(url: String) {
        val currentBookmarks = profile.value.customBookmarks
        val updated = profile.value.copy(customBookmarks = currentBookmarks.filter { it != url })
        updateProfile(updated)
    }
}
