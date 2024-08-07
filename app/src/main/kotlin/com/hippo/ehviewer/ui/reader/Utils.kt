package com.hippo.ehviewer.ui.reader

import android.app.Activity
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import com.hippo.ehviewer.Settings
import eu.kanade.tachiyomi.ui.reader.setting.OrientationType
import eu.kanade.tachiyomi.ui.reader.setting.ReadingModeType
import eu.kanade.tachiyomi.ui.reader.setting.ReadingModeType.RIGHT_TO_LEFT
import eu.kanade.tachiyomi.ui.reader.setting.ReadingModeType.VERTICAL
import kotlinx.coroutines.flow.onCompletion

fun Window.updateKeepScreenOn(enabled: Boolean) {
    if (enabled) {
        addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
fun Activity.ConfigureKeepScreenOn() {
    LaunchedEffect(Unit) {
        Settings.keepScreenOn.valueFlow()
            .onCompletion { window.updateKeepScreenOn(false) }
            .collect { window.updateKeepScreenOn(it) }
    }
}

/**
 * Forces the user preferred [orientation] on the activity.
 */
fun Activity.setOrientation(orientation: Int) {
    val newOrientation = OrientationType.fromPreference(orientation)
    if (newOrientation.flag != requestedOrientation) {
        requestedOrientation = newOrientation.flag
    }
}

/**
 * Sets the brightness of the screen. Range is [-75, 100].
 * From -75 to -1 a semi-transparent black view is overlaid with the minimum brightness.
 * From 1 to 100 it sets that value as brightness.
 * 0 sets system brightness and hides the overlay.
 */
fun Activity.setCustomBrightnessValue(value: Int) {
    val readerBrightness = when {
        value > 0 -> value / 100f
        value < 0 -> 0.01f
        else -> WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }

    window.attributes = window.attributes.apply { screenBrightness = readerBrightness }
}

fun Alignment.Companion.fromPreferences(value: Int, mode: ReadingModeType) = when (value) {
    1 -> when (mode) {
        VERTICAL -> CenterHorizontally
        RIGHT_TO_LEFT -> AbsoluteAlignment.Right
        else -> AbsoluteAlignment.Left
    }
    2 -> AbsoluteAlignment.Left
    3 -> AbsoluteAlignment.Right
    else -> CenterHorizontally
}

@Stable
fun ContentScale.Companion.fromPreferences(value: Int, srcSize: Size, dstSize: Size) = when (value) {
    2 -> Crop
    3 -> FillWidth
    4 -> FillHeight
    5 -> FixedScale(1 / Inside.computeScaleFactor(srcSize, dstSize).scaleX)
    6 -> if (srcSize.width > srcSize.height) FillHeight else FillWidth
    else -> Fit
}
