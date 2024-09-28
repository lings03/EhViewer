package com.hippo.ehviewer.client

import android.os.Build
import com.hippo.ehviewer.builtInHosts
import com.hippo.ehviewer.echEnabledDomains
import com.hippo.ehviewer.ui.settings.EhDoH
import java.net.InetAddress
import okhttp3.AsyncDns
import okhttp3.Dns
import okhttp3.android.AndroidAsyncDns

private typealias HostsMap = MutableMap<String, List<InetAddress>>

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

object EhDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> = when {
        hostname in echEnabledDomains ->
            EhDoH.lookup(hostname) ?: systemDns.lookup(hostname)
        else ->
            builtInHosts[hostname] ?: EhDoH.lookup(hostname) ?: systemDns.lookup(hostname)
    }.shuffled()
}
