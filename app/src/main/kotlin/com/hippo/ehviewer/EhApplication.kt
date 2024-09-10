/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.collection.LruCache
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import coil3.EventListener
import coil3.SingletonImageLoader
import coil3.asImage
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.serviceLoaderEnabled
import coil3.util.DebugLogger
import com.google.net.cronet.okhttptransport.RedirectStrategy.withoutRedirects
import com.hippo.ehviewer.client.EhCookieStore
import com.hippo.ehviewer.client.EhDns
import com.hippo.ehviewer.client.EhSSLSocketFactory
import com.hippo.ehviewer.client.EhTagDatabase
import com.hippo.ehviewer.client.data.GalleryDetail
import com.hippo.ehviewer.client.install
import com.hippo.ehviewer.coil.CropBorderInterceptor
import com.hippo.ehviewer.coil.DownloadThumbInterceptor
import com.hippo.ehviewer.coil.HardwareBitmapInterceptor
import com.hippo.ehviewer.coil.MergeInterceptor
import com.hippo.ehviewer.cronet.cronetHttpClient
import com.hippo.ehviewer.dailycheck.checkDawn
import com.hippo.ehviewer.dao.SearchDatabase
import com.hippo.ehviewer.download.DownloadManager
import com.hippo.ehviewer.download.DownloadsFilterMode
import com.hippo.ehviewer.ktbuilder.cache
import com.hippo.ehviewer.ktbuilder.cronet
import com.hippo.ehviewer.ktbuilder.diskCache
import com.hippo.ehviewer.ktbuilder.httpClient
import com.hippo.ehviewer.ktbuilder.imageLoader
import com.hippo.ehviewer.ktor.Cronet
import com.hippo.ehviewer.legacy.cleanObsoleteCache
import com.hippo.ehviewer.ui.keepNoMediaFileStatus
import com.hippo.ehviewer.ui.lockObserver
import com.hippo.ehviewer.ui.tools.dataStateFlow
import com.hippo.ehviewer.ui.tools.initSETConnection
import com.hippo.ehviewer.util.AppConfig
import com.hippo.ehviewer.util.Crash
import com.hippo.ehviewer.util.FavouriteStatusRouter
import com.hippo.ehviewer.util.FileUtils
import com.hippo.ehviewer.util.isAtLeastO
import com.hippo.ehviewer.util.isAtLeastP
import com.hippo.ehviewer.util.isAtLeastQ
import com.hippo.ehviewer.util.isAtLeastS
import com.hippo.ehviewer.util.isCronetAvailable
import eu.kanade.tachiyomi.network.interceptor.UncaughtExceptionInterceptor
import eu.kanade.tachiyomi.util.lang.launchIO
import eu.kanade.tachiyomi.util.lang.withUIContext
import eu.kanade.tachiyomi.util.system.logcat
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cookies.HttpCookies
import kotlinx.coroutines.launch
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.asLog
import okhttp3.AsyncDns
import okhttp3.Protocol
import okhttp3.android.AndroidAsyncDns
import okio.Path.Companion.toOkioPath
import splitties.arch.room.roomDb
import splitties.init.appCtx

private val lifecycle = ProcessLifecycleOwner.get().lifecycle
private val lifecycleScope = lifecycle.coroutineScope

class EhApplication :
    Application(),
    SingletonImageLoader.Factory {
    override fun onCreate() {
        initSETConnection()
        // Initialize Settings on first access
        lifecycleScope.launchIO {
            val mode = Settings.theme
            if (!isAtLeastS) {
                withUIContext {
                    AppCompatDelegate.setDefaultNightMode(mode)
                }
            }
            if (!LogcatLogger.isInstalled && Settings.saveCrashLog) {
                LogcatLogger.install(AndroidLogcatLogger(LogPriority.VERBOSE))
            }
        }
        lifecycle.addObserver(lockObserver)
        val handler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                if (Settings.saveCrashLog) {
                    Crash.saveCrashLog(e)
                }
            } catch (ignored: Throwable) {
            }
            handler?.uncaughtException(t, e)
        }
        super.onCreate()
        System.loadLibrary("ehviewer")
        lifecycleScope.launchIO {
            launch { EhTagDatabase }
            launch { EhDB }
            dataStateFlow.value
            launch {
                if (DownloadManager.labelList.isNotEmpty() && Settings.downloadFilterMode.key !in Settings.prefs) {
                    Settings.downloadFilterMode.value = DownloadsFilterMode.CUSTOM.flag
                }
                DownloadManager.readMetadataFromLocal()
            }
            launch {
                FileUtils.cleanupDirectory(AppConfig.externalCrashDir)
                FileUtils.cleanupDirectory(AppConfig.externalParseErrorDir)
            }
            launch {
                cleanupDownload()
            }
            if (Settings.requestNews) {
                launch {
                    checkDawn()
                }
            }
            launch {
                cleanObsoleteCache()
            }
        }
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults()
            Snapshot.registerApplyObserver { anies, _ ->
                logcat(LogPriority.VERBOSE) { anies.toString() }
            }
        }
    }

    private suspend fun cleanupDownload() {
        runCatching {
            keepNoMediaFileStatus()
        }.onFailure {
            logcat(it)
        }
        runCatching {
            clearTempDir()
        }.onFailure {
            logcat(it)
        }
    }

    private fun clearTempDir() {
        var dir = AppConfig.tempDir
        if (null != dir) {
            FileUtils.deleteContent(dir)
        }
        dir = AppConfig.externalTempDir
        if (null != dir) {
            FileUtils.deleteContent(dir)
        }
    }

    override fun newImageLoader(context: Context) = context.imageLoader {
        components {
            serviceLoaderEnabled(false)
            add(KtorNetworkFetcherFactory { ktorClient })
            add(MergeInterceptor)
            add(DownloadThumbInterceptor)
            if (isAtLeastO) {
                add(HardwareBitmapInterceptor)
            }
            add(CropBorderInterceptor)
            if (isAtLeastP) {
                add(AnimatedImageDecoder.Factory(false))
            } else {
                add(GifDecoder.Factory())
            }
        }
        diskCache { imageCache }
        crossfade(300)
        val drawable = AppCompatResources.getDrawable(appCtx, R.drawable.image_failed)
        if (drawable != null) error(drawable.asImage(true))
        if (BuildConfig.DEBUG) {
            logger(DebugLogger())
        } else {
            eventListener(object : EventListener() {
                override fun onError(request: ImageRequest, result: ErrorResult) {
                    logcat("ImageLoader", LogPriority.ERROR) {
                        "🚨 Failed - ${request.data}\n${result.throwable.asLog()}"
                    }
                }
            })
        }
    }

    companion object {
        val ktorClient by lazy {
            if (Settings.enableQuic && isCronetAvailable) {
                HttpClient(Cronet) {
                    engine {
                        client = cronetHttpClient
                    }
                    install(HttpCookies) {
                        storage = EhCookieStore
                    }
                }
            } else {
                HttpClient(OkHttp) {
                    install(HttpCookies) {
                        storage = EhCookieStore
                    }
                    engine {
                        preconfigured = nonCacheOkHttpClient
                    }
                }
                // Not using Apache5 for preversing domain fronting
                // HttpClient(Apache5) {
                //    install(HttpCookies) {
                //        storage = EhCookieStore
                //    }
                // }
            }
        }

        val noRedirectKtorClient by lazy {
            HttpClient(ktorClient.engine) {
                followRedirects = false
                install(HttpCookies) {
                    storage = EhCookieStore
                }
            }
        }

        // Fallback to CIO when cronet unavailable after coil 3.0 release
        private val baseOkHttpClient by lazy {
            httpClient {
                if (isAtLeastQ) {
                    dns(AsyncDns.toDns(AndroidAsyncDns.IPv4, AndroidAsyncDns.IPv6))
                }
                addInterceptor(UncaughtExceptionInterceptor())
            }
        }

        val nonCacheOkHttpClient by lazy {
            httpClient(baseOkHttpClient) {
                // TODO: Rewrite CronetInterceptor to use android.net.http.HttpEngine and make it Android 14 only when released
                // if (isCronetAvailable) {
                //    cronet(cronetHttpClient)
                // } else if (Settings.dF) {
                dns(EhDns)
                install(EhSSLSocketFactory)
            }
        }

        val nonH2OkHttpClient = nonCacheOkHttpClient.newBuilder()
            .protocols(listOf(Protocol.HTTP_3, Protocol.HTTP_1_1))
            .build()

        val noRedirectOkHttpClient by lazy {
            httpClient(baseOkHttpClient) {
                followRedirects(false)
                if (isCronetAvailable) {
                    cronet(cronetHttpClient) {
                        setRedirectStrategy(withoutRedirects())
                    }
                }
            }
        }

        // Never use this okhttp client to download large blobs!!!
        val okHttpClient by lazy {
            httpClient(nonCacheOkHttpClient) {
                cache(
                    appCtx.cacheDir.toOkioPath() / "http_cache",
                    20L * 1024L * 1024L,
                )
            }
        }

        // Use KtorClient directly when coil 3.0 released
        private val coilClient by lazy {
            httpClient(nonCacheOkHttpClient) {
                addInterceptor {
                    val req = it.request()
                    val newReq = req.newBuilder().apply {
                        // addHeader(HttpHeaders.Cookie, EhCookieStore.getCookieHeader(req.url.toString()))
                    }.build()
                    it.proceed(newReq)
                }
            }
        }

        val galleryDetailCache by lazy {
            LruCache<Long, GalleryDetail>(25).also {
                lifecycleScope.launch {
                    FavouriteStatusRouter.globalFlow.collect { (gid, slot) -> it[gid]?.favoriteSlot = slot }
                }
            }
        }

        val imageCache by lazy {
            diskCache {
                directory(appCtx.cacheDir.toOkioPath() / "image_cache")
                maxSizeBytes(Settings.readCacheSize.coerceIn(320, 5120).toLong() * 1024 * 1024)
            }
        }

        val searchDatabase by lazy { roomDb<SearchDatabase>("search_database.db") }
    }
}
