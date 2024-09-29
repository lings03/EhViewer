package com.hippo.ehviewer.client

import android.util.Log
import java.util.Base64
import javax.net.ssl.SSLSocket
import org.conscrypt.Conscrypt
import tech.relaycorp.doh.DoHClient

private const val OUTER_SNI = "cloudflare-ech.com"
private const val CACHE_EXPIRATION_TIME = 5 * 60 * 1000
private var cachedEchConfig: ByteArray? = null
private var expirationTime: Long = 0
val echEnabledDomains = listOf(
    "e-hentai.org",
    "exhentai.org",
    "forums.e-hentai.org",
    "testingcf.jsdelivr.net",
)

fun getCachedEchConfig(): ByteArray? {
    val currentTime = System.currentTimeMillis()
    return if (currentTime < expirationTime) {
        Log.d("ECH", "Cache hit")
        cachedEchConfig
    } else {
        cachedEchConfig = null
        null
    }
}

fun logEchConfigList(socket: SSLSocket, host: String) {
    Conscrypt.getEchConfigList(socket)?.let { echConfigList ->
        Log.d("ECH", "ECH Config List (${echConfigList.size} bytes) for $host:")
        Log.d("ECH", Base64.getEncoder().encodeToString(echConfigList))
    }
}

suspend fun fetchAndCacheEchConfig(dohClient: DoHClient) {
    try {
        val echConfig = getEchConfigListFromDns(OUTER_SNI, dohClient)
        cachedEchConfig = echConfig
        expirationTime = System.currentTimeMillis() + CACHE_EXPIRATION_TIME
    } catch (e: Exception) {
        Log.e("ECH", "Failed to fetch ECH config", e)
    }
}

suspend fun getEchConfigListFromDns(domain: String, doh: DoHClient): ByteArray {
    val result = doh.lookUp(domain, "HTTPS").data
    if (result.isNotEmpty()) {
        Log.d("ECH", "Response for $domain is ${result[0]}")
        val echValue = Regex("ech=([A-Za-z0-9+/=]+)").find(result[0])?.groupValues?.get(1)
        return echValue?.let { Base64.getDecoder().decode(it) } ?: ByteArray(0)
    }
    return ByteArray(0)
}
