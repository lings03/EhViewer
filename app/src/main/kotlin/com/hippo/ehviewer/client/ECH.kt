package com.hippo.ehviewer.client

import android.util.Log
import com.hippo.ehviewer.ui.settings.doh2
import java.util.Base64
import javax.net.ssl.SSLSocket
import org.conscrypt.Conscrypt

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

fun getCachedEchConfig(): ByteArray? = cachedEchConfig?.takeIf { System.currentTimeMillis() < expirationTime }?.also {
    Log.d("ECH", "Cache hit")
}

fun logEchConfigList(socket: SSLSocket, host: String) {
    Conscrypt.getEchConfigList(socket)?.also { echConfigList ->
        Log.d("ECH", "ECH Config List (${echConfigList.size} bytes) for $host:")
        Log.d("ECH", Base64.getEncoder().encodeToString(echConfigList))
    }
}

suspend fun fetchAndCacheEchConfig() {
    runCatching {
        val result = doh2?.lookUp(OUTER_SNI, "HTTPS")?.data?.firstOrNull()?.takeIf { it.isNotEmpty() }
            ?: return@runCatching
        Log.d("ECH", "Response for $OUTER_SNI is $result")
        val echConfig = Regex("ech=([A-Za-z0-9+/=]+)").find(result)
            ?.groupValues?.getOrNull(1)?.let { Base64.getDecoder().decode(it) }
            ?: return@runCatching
        cachedEchConfig = echConfig
        expirationTime = System.currentTimeMillis() + CACHE_EXPIRATION_TIME
    }.onFailure {
        Log.w("ECH", "Failed to fetch ECH config", it)
    }
}
