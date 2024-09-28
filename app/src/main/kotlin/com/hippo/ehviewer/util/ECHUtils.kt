package com.hippo.ehviewer.util

import android.util.Log
import org.conscrypt.Conscrypt
import javax.net.ssl.SSLSocket

fun logEchConfigList(socket: SSLSocket, host: String) {
    Conscrypt.getEchConfigList(socket)?.let { echConfigList ->
        Log.d("ECHConfigList", "ECH Config List (${echConfigList.size} bytes) for $host:")
        logHex(echConfigList)
    }
}

fun logHex(buf: ByteArray) {
    val hexString = buf.joinToString("") { String.format("%02x", it.toInt() and 0xFF) }
    Log.d("ECHConfigList", hexString)
}

fun hexStringToByteArray(hexString: String): ByteArray {
    require(hexString.length % 2 == 0) { "Invalid hex string length." }
    return ByteArray(hexString.length / 2) { i ->
        hexString.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
}