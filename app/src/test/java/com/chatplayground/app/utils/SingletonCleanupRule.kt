package com.chatplayground.app.utils

import com.chatplayground.app.security.AppLock
import com.chatplayground.app.security.BiometricAuthenticator
import com.chatplayground.app.security.EncryptedSharedPreferencesStore
import com.chatplayground.app.security.SecureStorage
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit Test Rule to ensure global singletons (AppLock, DesktopModeHandler,
 * and SecureStorage seams) are reset after EVERY test, preventing
 * cross-test state contamination.
 * Implements Vision Items 100 and 102.
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
        SecureStorage.authenticator = BiometricAuthenticator { false }
        SecureStorage.storeFactory = { context -> EncryptedSharedPreferencesStore(context) }
        DataClearer.resetWebStorageDeleterForTesting()
        DataClearer.resetClearingStepObserverForTesting()
        ImmersiveModeState.resetForTesting()
    }
}
