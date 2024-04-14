package com.hippo.ehviewer.ui.settings

import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import arrow.atomic.Atomic
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.hippo.ehviewer.EhApplication.Companion.nonH2OkHttpClient
import com.hippo.ehviewer.R
import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.client.CHROME_ACCEPT
import com.hippo.ehviewer.client.CHROME_ACCEPT_LANGUAGE
import com.hippo.ehviewer.client.EhCookieStore
import com.hippo.ehviewer.client.EhUrl
import com.hippo.ehviewer.ui.LockDrawer
import com.hippo.ehviewer.util.setDefaultSettings
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import okhttp3.Request

private const val APPLY_JS = "javascript:(function(){var apply = document.getElementById(\"apply\").children[0];apply.click();})();"

@Destination<RootGraph>
@Composable
fun UConfigScreen(navigator: DestinationsNavigator) {
    LockDrawer(true)
    val url = EhUrl.uConfigUrl
    val webview = remember { Atomic<WebView?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    class OkHttpWebViewClient() : AccompanistWebViewClient() {

        override fun shouldInterceptRequest(view: WebView, request: android.webkit.WebResourceRequest): WebResourceResponse? {
            val url = request.url.toString()
            val cookieHeader = EhCookieStore.getCookieHeader(url)
            val okHttpRequest = Request.Builder()
                .url(url)
                .addHeader("Referer", url)
                .addHeader("Origin", EhUrl.origin)
                .addHeader("User-Agent", Settings.userAgent)
                .addHeader("Accept", CHROME_ACCEPT)
                .addHeader("Accept-Language", CHROME_ACCEPT_LANGUAGE)
                .addHeader("Cookie", cookieHeader)
                .build()

            return try {
                val response = nonH2OkHttpClient.newCall(okHttpRequest).execute()
                if (response.isSuccessful) {
                    // Log.d("shouldInterceptRequest", response.headers.toString())
                    val contentTypeValue = response.header("Content-Type") ?: "text/html"
                    val mimeType = contentTypeValue.split(";")[0].trim()
                    WebResourceResponse(
                        mimeType,
                        response.header("Content-Encoding") ?: "utf-8",
                        response.body.byteStream(),
                    ).apply {
                        setStatusCodeAndReasonPhrase(response.code, response.message)
                    }
                } else {
                    response.body.close()
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    val okHttpWebViewClient = remember {
        OkHttpWebViewClient()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.u_config)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            webview.get()?.loadUrl(APPLY_JS)
                            navigator.popBackStack()
                        },
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        val state = rememberWebViewState(url = url)
        WebView(
            state = state,
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            onCreated = { it.setDefaultSettings() },
            factory = { WebView(it).apply { webview.set(this) } },
            client = okHttpWebViewClient,
        )
        val applyTip = stringResource(id = R.string.apply_tip)
        LaunchedEffect(Unit) { snackbarHostState.showSnackbar(applyTip) }
        DisposableEffect(Unit) {
            onDispose {
                EhCookieStore.flush()
            }
        }
    }
}
