package dev.mori.launcher.data

import android.content.Context

/** Stores user-pinned launcher shortcuts as package names, never as executable intents. */
object MoriWorkspaceStore {
    private const val FILE = "mori_workspace"
    private const val KEY_PINNED = "pinned_packages"

    fun pinnedPackages(context: Context): Set<String> =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getStringSet(KEY_PINNED, emptySet())?.toSet() ?: emptySet()

    fun setPinnedPackages(context: Context, packages: Set<String>) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putStringSet(KEY_PINNED, packages.toSet()).apply()
    }
}
