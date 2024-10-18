package com.hippo.ehviewer.cronet

import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.builtInHosts
import java.io.File
import org.chromium.net.ExperimentalCronetEngine
import org.json.JSONObject
import splitties.init.appCtx

val CloudflareIP = Settings.cloudflareIp
const val CFSUFFIX = ".cdn.cloudflare.net"
fun randomIP(host: String): String? = builtInHosts[host]?.random()?.hostAddress

val cronetHttpClient: ExperimentalCronetEngine = ExperimentalCronetEngine.Builder(appCtx).apply {
    configureCronetEngineBuilder(this)
}.build()

fun configureCronetEngineBuilder(builder: ExperimentalCronetEngine.Builder) {
    builder.apply {
        enableBrotli(true)
        enableQuic(true)
        setQuicHints()
        setCacheSettings()
        setUserAgent(Settings.userAgent)
        setExperimentalOptions(buildExperimentalOptions().toString())
    }
}

private fun ExperimentalCronetEngine.Builder.setQuicHints() {
    val quicHosts = listOf(
        "e-hentai.org",
        "api.e-hentai.org",
        "upload.e-hentai.org",
        "forums.e-hentai.org",
        "exhentai.org",
        "s.exhentai.org",
        "testingcf.jsdelivr.net",
    )
    quicHosts.forEach { addQuicHint(it, 443, 443) }
}

private fun ExperimentalCronetEngine.Builder.setCacheSettings() {
    val cacheDir = File(appCtx.cacheDir, "http_cache").apply { mkdirs() }
    setStoragePath(cacheDir.path)
    enableHttpCache(ExperimentalCronetEngine.Builder.HTTP_CACHE_DISK_NO_HTTP, 4096)
}

private fun buildExperimentalOptions(): JSONObject {
    val quicParams = JSONObject().apply {
        put("exponential_backoff_on_initial_delay", true)
        put("initial_delay_for_broken_alternative_service_seconds", 0)
        put("retry_without_alt_svc_on_quic_errors", false)
    }

    val hostResolverParams = JSONObject().put(
        "host_resolver_rules",
        buildHostResolverRules(),
    )

    return JSONObject().apply {
        put("QUIC", quicParams)
        put("HostResolverRules", hostResolverParams)
    }
}

private fun buildHostResolverRules(): String {
    val cloudflareMapping = listOf(
        "e-hentai.org",
        "*.e-hentai.org",
        "exhentai.org",
        "*.exhentai.org",
        "testingcf.jsdelivr.net",
    ).joinToString(",") { "MAP $it $CloudflareIP" }

    val defaultMapping = listOf(
        "e-hentai.org" to "e-hentai.org$CFSUFFIX",
        "*.e-hentai.org" to "e-hentai.org$CFSUFFIX",
        "exhentai.org" to "exhentai.org$CFSUFFIX",
        "*.exhentai.org" to "exhentai.org$CFSUFFIX",
        "testingcf.jsdelivr.net" to "testingcf.jsdelivr.net$CFSUFFIX",
    ).joinToString(",") { "MAP ${it.first} ${it.second}" }

    val githubMapping = "MAP api.github.com ${randomIP("api.github.com")}"

    return if (Settings.cloudflareIpOverride) {
        "$cloudflareMapping,$githubMapping"
    } else {
        "$defaultMapping,$githubMapping"
    }
}