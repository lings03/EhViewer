/*
 * Copyright 2022 Tarsin Norbin
 *
 * This file is part of EhViewer
 *
 * EhViewer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * EhViewer is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with EhViewer.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package com.hippo.ehviewer.client

import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.builtInHosts
import java.net.InetAddress
import java.net.Socket
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt

private const val EXCEPTIONAL_DOMAIN = "hath.network"
private val sslSocketFactory: SSLSocketFactory = SSLContext.getInstance("TLS", Conscrypt.newProvider()).apply {
    init(null, null, null)
}.socketFactory

object EhSSLSocketFactory : SSLSocketFactory() {
    override fun getDefaultCipherSuites(): Array<String> = sslSocketFactory.defaultCipherSuites
    override fun getSupportedCipherSuites(): Array<String> = sslSocketFactory.supportedCipherSuites
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket = createConfiguredSocket(sslSocketFactory.createSocket(s, resolveHost(s, host), port, autoClose) as SSLSocket, host)
    override fun createSocket(host: String, port: Int): Socket = createConfiguredSocket(sslSocketFactory.createSocket(host, port) as SSLSocket, host)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket = createConfiguredSocket(sslSocketFactory.createSocket(host, port, localHost, localPort) as SSLSocket, host)
    override fun createSocket(host: InetAddress, port: Int): Socket = sslSocketFactory.createSocket(host, port)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket = sslSocketFactory.createSocket(address, port, localAddress, localPort)

    private fun createConfiguredSocket(socket: SSLSocket, host: String): SSLSocket {
        Conscrypt.setCheckDnsForEch(socket, true)
        var cachedEchConfig = getCachedEchConfig()
        if (host in echEnabledDomains && Conscrypt.getEchConfigList(socket) == null) {
            if (cachedEchConfig == null) {
                runBlocking {
                    fetchAndCacheEchConfig(dohClient)
                }
            }
            Conscrypt.setEchConfigList(socket, cachedEchConfig)
        }
        logEchConfigList(socket, host)
        return socket
    }

    private fun resolveHost(socket: Socket, host: String): String = if (host in echEnabledDomains) {
        host
    } else {
        socket.inetAddress.hostAddress.takeIf {
            host in builtInHosts || EXCEPTIONAL_DOMAIN in host || host in Settings.dohUrl
        } ?: host
    }
}

fun OkHttpClient.Builder.install(sslSocketFactory: SSLSocketFactory) = apply {
    val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())!!
    factory.init(null as KeyStore?)
    val manager = factory.trustManagers!!
    val trustManager = manager.filterIsInstance<X509TrustManager>().first()
    sslSocketFactory(sslSocketFactory, trustManager)
}
