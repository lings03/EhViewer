package com.hippo.ehviewer.client

import android.os.Build
import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.builtInHosts
import com.hippo.ehviewer.ui.settings.EhDoH
import java.net.InetAddress
import okhttp3.AsyncDns
import okhttp3.Dns
import okhttp3.android.AndroidAsyncDns
import tech.relaycorp.doh.DoHClient

private typealias HostsMap = MutableMap<String, List<InetAddress>>

val builtInDoHUrls = listOf(
    "https://185.222.222.222/dns-query",
    "https://45.11.45.11/dns-query",
    "https://9.9.9.9/dns-query",
    "https://149.112.112.112/dns-query",
    "https://208.67.220.220/dns-query",
    "https://208.67.222.222/dns-query",
    "https://146.112.41.5/dns-query",
    "https://101.101.101.101/dns-query",
    "https://130.59.31.248/dns-query",
    "https://130.59.31.251/dns-query",
    // "https://77.88.8.1/dns-query",
    "https://77.88.8.8/dns-query",
    "https://94.140.14.140/dns-query",
    "https://94.140.14.141/dns-query",
    "https://162.159.36.1/dns-query",
    "https://162.159.46.1/dns-query",
    "https://1.1.1.1/dns-query",
    "https://1.0.0.1/dns-query",
)

fun hostsDsl(builder: HostsMap.() -> Unit): HostsMap = mutableMapOf<String, List<InetAddress>>().apply(builder)

fun interface HostMapBuilder {
    infix fun String.blockedInCN(boolean: Boolean)
}

fun HostsMap.hosts(vararg hosts: String, builder: HostMapBuilder.() -> Unit) = apply {
    hosts.forEach { host ->
        fun String.toInetAddress() = InetAddress.getByName(this).let { InetAddress.getByAddress(host, it.address) }
        mutableListOf<InetAddress>().apply {
            HostMapBuilder { if (!it) add(toInetAddress()) }.apply(builder)
            put(host, this)
        }
    }
}

val systemDns = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) AsyncDns.toDns(AndroidAsyncDns.IPv4, AndroidAsyncDns.IPv6) else Dns.SYSTEM

val dohClient = DoHClient(getEffectiveDoHUrl())

fun getEffectiveDoHUrl(): String {
    val userDefinedUrl = Settings.dohUrl
    if (!userDefinedUrl.isNullOrBlank()) {
        return userDefinedUrl
    }
    val randomizedUrls = builtInDoHUrls.shuffled()
    val selectedUrl = randomizedUrls.firstOrNull()
    return selectedUrl ?: throw IllegalStateException("No DoH URLs available")
}

object EhDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> = when {
        (hostname in echEnabledDomains && Settings.enableECH) ->
            EhDoH.lookup(hostname) ?: systemDns.lookup(hostname)
        else ->
            builtInHosts[hostname] ?: EhDoH.lookup(hostname) ?: systemDns.lookup(hostname)
    }.shuffled()
}
