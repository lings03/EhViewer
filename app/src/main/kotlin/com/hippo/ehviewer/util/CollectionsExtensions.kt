package com.hippo.ehviewer.util

import androidx.compose.runtime.snapshots.SnapshotStateMap

fun <T> MutableList<T>.insertWith(element: T, comparator: Comparator<T>) {
    val index = binarySearch(element, comparator).let {
        if (it < 0) -it - 1 else it
    }
    add(index, element)
}

inline fun <T> Iterable<Iterable<T>>.flattenForEach(action: (T) -> Unit) {
    for (element in this) {
        for (item in element) {
            action(item)
        }
    }
}

fun <K, V> SnapshotStateMap<K, V>.takeAndClear() = toMap().values.also { clear() }
