/*
 * Adapted from ARK Launcher (https://github.com/jrs8205/ARK-launcher)
 * Original project license: Apache License 2.0.
 *
 * Adaptation: Mori-specific host identifier and composition local. The upstream
 * provider-generation pattern is retained; unrelated ARK repository dependencies
 * and layout-specific helpers are intentionally not copied.
 */
package dev.mori.launcher.data

import android.appwidget.AppWidgetHost
import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Stable, app-unique identifier for Mori's AppWidgetHost. */
const val MORI_APPWIDGET_HOST_ID = 0x4D4F5249

/** The Activity-owned widget host; null when no launcher Activity is composed. */
val LocalMoriWidgetHost = staticCompositionLocalOf<AppWidgetHost?> { null }

/**
 * Tracks provider changes so Compose UI can refresh provider metadata after an
 * installed widget app is updated or a provider becomes available.
 */
class MoriWidgetHost(context: Context) : AppWidgetHost(context, MORI_APPWIDGET_HOST_ID) {
    private val _providerGeneration = MutableStateFlow(0)
    val providerGeneration: StateFlow<Int> = _providerGeneration.asStateFlow()

    override fun onProvidersChanged() {
        super.onProvidersChanged()
        _providerGeneration.value++
    }
}
