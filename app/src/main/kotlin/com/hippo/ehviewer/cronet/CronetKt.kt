package com.hippo.ehviewer.cronet

import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.builtInHosts
import java.io.File
import org.chromium.net.ExperimentalCronetEngine
import org.json.JSONObject
import splitties.init.appCtx

val CloudflareIP = Settings.cloudflareIp

val cronetHttpClient: ExperimentalCronetEngine = ExperimentalCronetEngine.Builder(appCtx).apply {
    configureCronetEngineBuilder(this)
}.build()
var experimentalOptions = JSONObject()
const val CFSUFFIX = ".cdn.cloudflare.net"
fun randomIP(host: String): String? = builtInHosts[host]?.random()?.hostAddress

fun configureCronetEngineBuilder(builder: ExperimentalCronetEngine.Builder) {
    builder.enableBrotli(true)
        .enableQuic(true)
        .addQuicHint("e-hentai.org", 443, 443)
        .addQuicHint("api.e-hentai.org", 443, 443)
        .addQuicHint("upload.e-hentai.org", 443, 443)
        .addQuicHint("forums.e-hentai.org", 443, 443)
        .addQuicHint("exhentai.org", 443, 443)
        .addQuicHint("s.exhentai.org", 443, 443)
        .addQuicHint("testingcf.jsdelivr.net", 443, 443)
    val cache = File(appCtx.cacheDir, "http_cache").apply { mkdirs() }
    builder.setStoragePath(cache.path)
        .enableHttpCache(ExperimentalCronetEngine.Builder.HTTP_CACHE_DISK_NO_HTTP, 4096)
        .setUserAgent(Settings.userAgent)
    if (Settings.cloudflareIpOverride) {
        experimentalOptions = JSONObject().put(
            "HostResolverRules",
            JSONObject().put(
                "host_resolver_rules",
                "MAP e-hentai.org $CloudflareIP," +
                    "MAP *.e-hentai.org $CloudflareIP," +
                    "MAP exhentai.org $CloudflareIP," +
                    "MAP *.exhentai.org $CloudflareIP," +
                    "MAP testingcf.jsdelivr.net $CloudflareIP," +
                    "MAP api.github.com ${randomIP("api.github.com")}",
            ),
        )
    } else {
        experimentalOptions = JSONObject().put(
            "HostResolverRules",
            JSONObject().put(
                "host_resolver_rules",
                "MAP e-hentai.org e-hentai.org$CFSUFFIX," +
                    "MAP *.e-hentai.org e-hentai.org$CFSUFFIX," +
                    "MAP exhentai.org exhentai.org$CFSUFFIX," +
                    "MAP *.exhentai.org exhentai.org$CFSUFFIX," +
                    "MAP testingcf.jsdelivr.net testingcf.jsdelivr.net$CFSUFFIX," +
                    "MAP api.github.com ${randomIP("api.github.com")}",
            ),
        )
    }
    builder.setExperimentalOptions(experimentalOptions.toString())
}
