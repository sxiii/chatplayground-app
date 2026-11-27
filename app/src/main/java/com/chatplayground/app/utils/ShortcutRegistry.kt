package com.chatplayground.app.utils

/** A launcher-shortcut definition: id, user-visible label, and the intent
 * action the launcher triggers when the shortcut is tapped.
 */
data class ShortcutDefinition(
    val id: String,
    val label: String,
    val intentAction: String,
    val iconRes: String,
)

/** Static registry of the app's launcher shortcuts (item 58).
 *
 * Pure-JVM, no Android dependencies: only strings, so shortcut definitions
 * can be unit-tested without Robolectric or an Android SDK. The actual
 * `shortcuts.xml` resource is generated from these same strings at build time
 * by `MainActivity.registerShortcuts()` on first launch (dynamic shortcuts
 * via ShortcutManagerCompat), so no shipped resource needs editing.
 */
object ShortcutRegistry {
    private val all =
        listOf(
            ShortcutDefinition(
                id = "clear_cache",
                label = "Очистить кэш",
                intentAction = "com.chatplayground.app.action.CLEAR_CACHE",
                iconRes = "ic_delete",
            ),
            ShortcutDefinition(
                id = "toggle_theme",
                label = "Переключить тему",
                intentAction = "com.chatplayground.app.action.TOGGLE_THEME",
                iconRes = "ic_brightness",
            ),
        )

    fun allShortcuts(): List<ShortcutDefinition> = all

    fun shortcutById(id: String): ShortcutDefinition =
        all.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Unknown shortcut id: $id")
}
