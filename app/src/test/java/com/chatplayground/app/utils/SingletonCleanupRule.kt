package com.chatplayground.app.utils

import com.chatplayground.app.security.AppLock
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit Test Rule to ensure global singletons (AppLock and DesktopModeHandler)
 * are reset after EVERY test, preventing cross-test state contamination.
 * Implements Vision Item 100.
 */
class SingletonCleanupRule : TestWatcher() {
    override fun starting(description: Description) {
        // No-op: test begins with whatever state the previous test left
    }

    override fun finished(description: Description) {
        // Reset all global singletons to a clean slate so no test can leak state
        AppLock.enabled = false
        AppLock.resetForTesting()
        DesktopModeHandler.applyDesktopMode(false)
    }
}
