package com.example.ui.components

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.vpn.ConnectionStatus
import com.example.vpn.VpnSessionMetrics

@Composable
fun AttendanceWebView(
    url: String,
    bookmarks: List<String> = listOf("http://attendance/", "http://hr.internal/", "http://portal.company.com/"),
    metrics: VpnSessionMetrics,
    onConnectVpn: () -> Unit,
    onAddBookmark: (String) -> Unit = {},
    onRemoveBookmark: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var webView: WebView? by remember { mutableStateOf(null) }
    var currentUrlInput by remember(url) { mutableStateOf(url) }
    var isLoading by remember { mutableStateOf(false) }
    var isDesktopMode by remember { mutableStateOf(true) } // Windows Desktop User-Agent spoofing by default
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isConnected = metrics.status == ConnectionStatus.CONNECTED

    // Synchronize initial URL loading
    LaunchedEffect(url) {
        if (currentUrlInput != url) {
            currentUrlInput = url
            webView?.loadUrl(url)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // VPN Status Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1E293B)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConnected) "VPN CONNECTED (http://attendance/ accessible)" else "VPN DISCONNECTED",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isConnected) {
                    Button(
                        onClick = onConnectVpn,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("vpn_quick_connect_pill")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Connect",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Connect VPN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Browser Address Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { webView?.goBack() },
                enabled = webView?.canGoBack() == true,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (webView?.canGoBack() == true) Color.White else Color(0xFF475569)
                )
            }

            IconButton(
                onClick = { webView?.goForward() },
                enabled = webView?.canGoForward() == true,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (webView?.canGoForward() == true) Color.White else Color(0xFF475569)
                )
            }

            OutlinedTextField(
                value = currentUrlInput,
                onValueChange = { currentUrlInput = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 4.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    keyboardController?.hide()
                    hasError = false
                    val target = if (!currentUrlInput.startsWith("http://") && !currentUrlInput.startsWith("https://")) {
                        "http://$currentUrlInput"
                    } else currentUrlInput
                    currentUrlInput = target
                    webView?.loadUrl(target)
                }),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A),
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFCBD5E1)
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { onAddBookmark(currentUrlInput) }) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Bookmark",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            IconButton(
                onClick = {
                    keyboardController?.hide()
                    hasError = false
                    val target = if (!currentUrlInput.startsWith("http://") && !currentUrlInput.startsWith("https://")) {
                        "http://$currentUrlInput"
                    } else currentUrlInput
                    currentUrlInput = target
                    webView?.loadUrl(target)
                },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("reload_web_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Go",
                    tint = Color(0xFF38BDF8)
                )
            }

            IconButton(
                onClick = {
                    isDesktopMode = !isDesktopMode
                    webView?.settings?.userAgentString = if (isDesktopMode) {
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                    } else null
                    webView?.reload()
                },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_desktop_mode_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = "Toggle Desktop User-Agent",
                    tint = if (isDesktopMode) Color(0xFF10B981) else Color(0xFF64748B)
                )
            }
        }

        // Quick Intranet Bookmarks Row
        if (bookmarks.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shortcuts:",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                bookmarks.forEach { bmUrl ->
                    AssistChip(
                        onClick = {
                            currentUrlInput = bmUrl
                            hasError = false
                            webView?.loadUrl(bmUrl)
                        },
                        label = {
                            Text(
                                text = bmUrl.removePrefix("http://").removePrefix("https://"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (currentUrlInput == bmUrl) Color(0xFF38BDF8) else Color(0xFFE2E8F0)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(12.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (currentUrlInput == bmUrl) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF1E293B)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = if (currentUrlInput == bmUrl) Color(0xFF38BDF8) else Color(0xFF334155)
                        )
                    )
                }
            }
        }

        // Loading Progress Indicator
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF38BDF8),
                trackColor = Color(0xFF1E293B)
            )
        }

        // Main WebView / Error Display
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            allowFileAccess = true
                            if (isDesktopMode) {
                                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                hasError = false
                                url?.let { currentUrlInput = it }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }

                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: SslErrorHandler?,
                                error: SslError?
                            ) {
                                // Proceed with local SSL certificates on intranet attendance portal
                                handler?.proceed()
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                    errorMessage = error?.description?.toString() ?: "Network unreachable"
                                    isLoading = false
                                }
                            }
                        }

                        webChromeClient = WebChromeClient()
                        loadUrl(url)
                        webView = this
                    }
                },
                update = { view ->
                    webView = view
                }
            )

            // Intranet Disconnected or Unreachable Banner Overlay
            if (!isConnected || hasError) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F172A).copy(alpha = 0.95f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Portal",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Attendance Portal Destination",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "URL: http://attendance/",
                            color = Color(0xFF38BDF8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (!isConnected) {
                                "The internal domain 'http://attendance/' resides inside your corporate network. Please activate Sophos XG VPN connection to load the attendance portal."
                            } else {
                                "Waiting for local DNS resolution of 'http://attendance/' ($errorMessage)."
                            },
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {
                                if (!isConnected) {
                                    onConnectVpn()
                                } else {
                                    hasError = false
                                    webView?.loadUrl("http://attendance/")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("retry_attendance_web_button")
                        ) {
                            Icon(
                                imageVector = if (!isConnected) Icons.Default.VpnKey else Icons.Default.Refresh,
                                contentDescription = "Action"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (!isConnected) "One-Tap Connect Sophos XG VPN" else "Retry Loading http://attendance/",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
