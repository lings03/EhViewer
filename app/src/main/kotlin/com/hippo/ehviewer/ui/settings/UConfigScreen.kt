package com.hippo.ehviewer.ui.settings

import android.util.Log
import android.webkit.JavascriptInterface
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.hippo.ehviewer.util.setDefaultSettings
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject

private const val APPLY_JS = "javascript:(function(){var apply = document.getElementById(\"apply\").children[0];apply.click();})();"
private val jsCode = """
(function() {
    var applyButton = document.querySelector('#apply input[type="submit"]');
    if (applyButton) {
        var form = applyButton.form;
        if (form) {
            form.addEventListener('submit', function(event) {
                event.preventDefault();

                var formData = {};
                Array.from(form.elements).forEach(function(field) {
                    if (!field.name || field.disabled) {
                        return;
                    }
                    switch (field.type) {
                        case 'checkbox':
                        case 'radio':
                            if (field.checked) {
                                formData[field.name] = field.value;
                            }
                            break;
                        // case 'submit':
                        case 'button':
                            break;
                        default:
                            formData[field.name] = field.value;
                            break;
                    }
                });
                var json = JSON.stringify(formData);
                Android.postFormData(form.action, json);
            }, false);
        }
    }
})();
"""
val url = EhUrl.uConfigUrl
val cookieHeader = EhCookieStore.getCookieHeader(url)

@Destination<RootGraph>
@Composable
fun UConfigScreen(navigator: DestinationsNavigator) {
    val url = EhUrl.uConfigUrl
    val webview = remember { Atomic<WebView?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    class OkHttpWebViewClient : AccompanistWebViewClient() {

        override fun shouldInterceptRequest(view: WebView, request: android.webkit.WebResourceRequest): WebResourceResponse? {
            val url = request.url.toString()
            val okHttpRequest = Request.Builder()
                .url(url)
                .addHeader("Referer", url)
                .addHeader("Origin", EhUrl.origin)
                .addHeader("User-Agent", Settings.userAgent)
                .addHeader("Accept", CHROME_ACCEPT)
                .addHeader("Accept-Language", CHROME_ACCEPT_LANGUAGE)
                .addHeader("Cookie", cookieHeader.toString())
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
        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            view.evaluateJavascript(jsCode, null)
        }
    }
    val okHttpWebViewClient = remember {
        OkHttpWebViewClient()
    }
    fun handlePostRequest(
        webView: WebView,
        url: String,
        formData: String,
        scope: CoroutineScope,
    ) {
        val formBodyBuilder = FormBody.Builder()
        val formFields = JSONObject(formData)
        formFields.keys().forEach {
            formBodyBuilder.add(it, formFields.getString(it))
        }
        val formBody = formBodyBuilder.build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .addHeader("Referer", url)
            .addHeader("Origin", EhUrl.origin)
            .addHeader("User-Agent", Settings.userAgent)
            .addHeader("Accept", CHROME_ACCEPT)
            .addHeader("Accept-Language", CHROME_ACCEPT_LANGUAGE)
            .addHeader("Cookie", cookieHeader.toString())
            .build()
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    nonH2OkHttpClient.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Log.d("handlePostRequest", "$response")
                    }
                } else {
                    Log.e("handlePostRequest", "Request failed: $response")
                }
            } catch (e: IOException) {
                Log.e("handlePostRequest", "Request error: ${e.localizedMessage}")
            }
        }
    }
    class WebAppInterface(
        private val webView: WebView,
        private val handler: (WebView, String, String, CoroutineScope) -> Unit,
    ) {
        @JavascriptInterface
        fun postFormData(url: String, formData: String) {
            Log.d("WebAppInterface", "Received form data: $formData")
            handler(webView, url, formData, coroutineScope)
        }
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
            onCreated = {
                it.setDefaultSettings()
                it.settings.javaScriptEnabled = true
                it.addJavascriptInterface(
                    WebAppInterface(it, ::handlePostRequest),
                    "Android",
                )
            },
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
