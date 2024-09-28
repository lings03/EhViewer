package com.hippo.ehviewer.util

import android.util.Log
import com.hippo.ehviewer.ui.settings.DoHClient
import com.hippo.ehviewer.updateEchConfig
import java.util.Base64
import javax.net.ssl.SSLSocket
import org.conscrypt.Conscrypt

fun logEchConfigList(socket: SSLSocket, host: String) {
    Conscrypt.getEchConfigList(socket)?.let { echConfigList ->
        Log.d("ECHConfigList", "ECH Config List (${echConfigList.size} bytes) for $host:")
        logBase64(echConfigList)
    }
}

fun logBase64(buf: ByteArray) {
    val base64String = Base64.getEncoder().encodeToString(buf)
    Log.d("ECHConfigList", base64String)
}

suspend fun echUpdater(domain: String) {
    DoHClient.use {
        val result = DoHClient.lookUp(domain, "HTTPS").data
        if (result.isNotEmpty()) {
            Log.d("echUpdater", result[0])
            val echRegex = "ech=([A-Za-z0-9+/=]+)".toRegex()
            val echMatch = echRegex.find(result[0])
            if (echMatch != null) {
                val echValue = echMatch.groupValues[1]
                Log.d("echUpdater", "Extracted ECH: $echValue")
                updateEchConfig(echValue)
            } else {
                Log.d("echUpdater", "ECH value not found in DNS response")
            }
        }
        DoHClient.close()
    }
}
