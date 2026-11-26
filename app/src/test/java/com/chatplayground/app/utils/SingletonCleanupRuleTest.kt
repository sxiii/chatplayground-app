package com.chatplayground.app.utils

import android.content.Context
import com.chatplayground.app.security.AppLock
import com.chatplayground.app.security.BiometricAuthenticator
import com.chatplayground.app.security.EncryptedStore
import com.chatplayground.app.security.SecureStorage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Unit tests for [SingletonCleanupRule] verifying that global singletons
 * ([AppLock], [DesktopModeHandler] and the [SecureStorage] seams) are correctly
 * reset after test execution.
 * Implements Vision Items 101 and 102.
 */
class SingletonCleanupRuleTest {
    /** Minimal [Statement] so the rule can be exercised without anonymous objects. */
    private class NoOpStatement : Statement() {
        override fun evaluate() = Unit
    }

    @Test
    fun ruleResetsAppLockAndDesktopModeOnFinished() {
        AppLock.enabled = true
        AppLock.authenticator = { true }
        DesktopModeHandler.applyDesktopMode(true)

        val rule = SingletonCleanupRule()
        val description = Description.createTestDescription(javaClass, "dummy")
        val statement = rule.apply(NoOpStatement(), description)

        statement.evaluate()

        assertFalse("AppLock.enabled should be reset to false", AppLock.enabled)
        assertFalse("AppLock must be locked again after reset", AppLock.isLocked())
        assertFalse("DesktopModeHandler.isDesktop should be reset to false", DesktopModeHandler.isDesktop)
    }

    @Test
    fun ruleResetsSecureStorageSeamsOnFinished() {
        val injectedStoreFactory: (Context) -> EncryptedStore =
            { _ -> throw AssertionError("injected store must not be used after cleanup") }
        SecureStorage.authenticator = BiometricAuthenticator { true }
        SecureStorage.storeFactory = injectedStoreFactory

        val rule = SingletonCleanupRule()
        val description = Description.createTestDescription(javaClass, "dummySecureStorage")
        val statement = rule.apply(NoOpStatement(), description)

        statement.evaluate()

        // The injected store factory must be replaced by the production one.
        assertTrue(
            "SecureStorage.storeFactory must be reset to the production factory",
            SecureStorage.storeFactory !== injectedStoreFactory,
        )
    }
}
