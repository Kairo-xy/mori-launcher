package dev.mori.launcher.data

import android.content.Context

/**
 * Small, device-local preference store. Mori deliberately keeps launcher
 * preferences on-device and does not require an account or network access.
 */
object MoriPreferences {
    private const val FILE = "mori_launcher_preferences"
    private const val KEY_GLASS = "glass_surfaces"
    private const val KEY_LABELS = "app_labels"

    private fun store(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun glassEnabled(context: Context): Boolean =
        store(context).getBoolean(KEY_GLASS, true)

    fun setGlassEnabled(context: Context, enabled: Boolean) {
        store(context).edit().putBoolean(KEY_GLASS, enabled).apply()
    }

    fun labelsEnabled(context: Context): Boolean =
        store(context).getBoolean(KEY_LABELS, true)

    fun setLabelsEnabled(context: Context, enabled: Boolean) {
        store(context).edit().putBoolean(KEY_LABELS, enabled).apply()
    }
}
