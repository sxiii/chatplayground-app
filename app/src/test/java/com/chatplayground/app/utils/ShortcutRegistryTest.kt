package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure-JVM regression suite for [ShortcutRegistry].
 *
 * No Robolectric runner, no @Config, no Android shadows: this class must stay
 * runnable on a plain JVM so shortcut definitions can be verified without
 * an Android SDK. If any Android import creeps in, the build will fail here.
 */
class ShortcutRegistryTest {
    @Test
    fun registryContainsClearCacheShortcut() {
        val shortcut = ShortcutRegistry.shortcutById("clear_cache")
        assertEquals("Очистить кэш", shortcut.label)
        assertEquals("com.chatplayground.app.action.CLEAR_CACHE", shortcut.intentAction)
    }

    @Test
    fun registryContainsToggleThemeShortcut() {
        val shortcut = ShortcutRegistry.shortcutById("toggle_theme")
        assertEquals("Переключить тему", shortcut.label)
        assertEquals("com.chatplayground.app.action.TOGGLE_THEME", shortcut.intentAction)
    }

    @Test
    fun allShortcutsHaveUniqueIds() {
        val shortcuts = ShortcutRegistry.allShortcuts()
        val ids = shortcuts.map { it.id }.toSet()
        assertEquals(shortcuts.size, ids.size)
    }

    @Test
    fun everyShortcutHasNonEmptyLabelAndIcon() {
        for (shortcut in ShortcutRegistry.allShortcuts()) {
            assertEquals(false, shortcut.label.isBlank())
            assertEquals(false, shortcut.iconRes.isBlank())
        }
    }
}
