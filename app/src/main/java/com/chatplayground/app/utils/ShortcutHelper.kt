package com.chatplayground.app.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.chatplayground.app.MainActivity

object ShortcutHelper {
    fun registerShortcuts(context: Context) {
        val shortcuts = ShortcutRegistry.allShortcuts().map { def ->
            val intent = Intent(context, MainActivity::class.java).apply {
                action = def.intentAction
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            ShortcutInfoCompat.Builder(context, def.id)
                .setShortLabel(def.label)
                .setLongLabel(def.label)
                .setIntent(intent)
                .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_brightness))
                .build()
        }
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }
}
