package com.hippo.ehviewer.ktbuilder

import java.io.File
import okhttp3.Cache
import okhttp3.OkHttpClient
import okio.FileSystem
import okio.Path

inline fun httpClient(builder: OkHttpClient.Builder.() -> Unit): OkHttpClient = OkHttpClient.Builder().apply(builder).build()

inline fun httpClient(
    client: OkHttpClient,
    builder: OkHttpClient.Builder.() -> Unit,
): OkHttpClient = client.newBuilder().apply(builder).build()

fun OkHttpClient.Builder.cache(
    directory: Path,
    maxSize: Long,
    fileSystem: FileSystem = FileSystem.SYSTEM,
): OkHttpClient.Builder {
    val cacheDirFile = File(directory.toString())
    return cache(Cache(cacheDirFile, maxSize))
}
