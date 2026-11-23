package dev.mori.launcher.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

/**
 * Public Android intents only: no privileged permissions or hidden APIs.
 * Every shortcut opens the user's installed/default handler when available.
 */
object MoriLauncherActions {
    fun open(context: Context, target: String) {
        val intent = when (target) {
            "phone" -> Intent(Intent.ACTION_DIAL)
            "browser" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            "music" -> Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC
            )
            "camera" -> Intent(Intent.ACTION_IMAGE_CAPTURE)
            "wallpaper" -> Intent(Intent.ACTION_SET_WALLPAPER)
            "settings" -> Intent(Settings.ACTION_SETTINGS)
            else -> null
        }

        if (intent == null) return
        try {
            if (intent.resolveActivity(context.packageManager) == null) {
                Toast.makeText(context, "No compatible app found", Toast.LENGTH_SHORT).show()
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Couldn't open that app", Toast.LENGTH_SHORT).show()
        }
    }
}
