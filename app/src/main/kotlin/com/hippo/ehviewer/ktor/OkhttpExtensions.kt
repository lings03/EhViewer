package com.hippo.ehviewer.ktor

import com.hippo.ehviewer.EhApplication.Companion.nonCacheOkHttpClient
import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.util.isAtLeastQ
import eu.kanade.tachiyomi.network.interceptor.UncaughtExceptionInterceptor
import io.ktor.client.engine.okhttp.OkHttpConfig
import java.security.Security
import okhttp3.AsyncDns
import okhttp3.ExperimentalOkHttpApi
import okhttp3.android.AndroidAsyncDns
import org.conscrypt.Conscrypt

@OptIn(ExperimentalOkHttpApi::class)
fun OkHttpConfig.configureClient() {
    if (Settings.enableECH) {
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }
    if (Settings.dF) {
        preconfigured = nonCacheOkHttpClient
    }
    addInterceptor(UncaughtExceptionInterceptor())
    config {
        if (isAtLeastQ && !Settings.dF) {
            dns(AsyncDns.toDns(AndroidAsyncDns.IPv4, AndroidAsyncDns.IPv6))
        }
    }
}
