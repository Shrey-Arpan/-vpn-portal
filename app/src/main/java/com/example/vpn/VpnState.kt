package com.example.vpn

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    AUTHENTICATING,
    CONNECTED,
    DISCONNECTING,
    FAILED
}

data class VpnProfileConfig(
    val serverHost: String = "xgvpn.contata.co.in",
    val serverPort: Int = 443,
    val username: String = "shreya",
    val basePassword: String = "Qwerty@1234",
    val totpSecretHex: String = "e5bd156e098db764fb77816ebdc216ce",
    val totpSecretBase32: String = "4W6RK3QJRW3WJ63XQFXL3QQWZY",
    val timestepSeconds: Long = 20L,
    val attendanceUrl: String = "http://attendance/",
    val customBookmarks: List<String> = listOf("http://attendance/", "http://hr.internal/", "http://portal.company.com/", "http://192.168.1.1/"),
    val ovpnConfigContent: String = """
# Sophos XG SSL VPN Profile
client
dev tun
proto tcp
remote xgvpn.contata.co.in 443
resolv-retry infinite
nobind
persist-key
persist-tun
auth-user-pass
cipher AES-256-GCM
auth SHA256
remote-cert-tls server
comp-lzo
verb 3
    """.trimIndent()
)

data class VpnSessionMetrics(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectedTimeSeconds: Long = 0L,
    val bytesReceived: Long = 0L,
    val bytesSent: Long = 0L,
    val virtualIp: String = "10.8.0.14",
    val gatewayIp: String = "10.8.0.1",
    val attendanceUrl: String = "http://attendance/",
    val errorMessage: String? = null
)
