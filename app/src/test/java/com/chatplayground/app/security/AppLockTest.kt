package com.chatplayground.app.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pure-JVM tests for [AppLock]: the gate that decides whether the app
 * content is unlocked on startup. The biometric authenticator is injected
 * as a seam so the fail-closed behaviour can be asserted without a real
 * BiometricPrompt (which cannot run under plain JUnit / Robolectric).
 */
class AppLockTest {
    @Before
    fun setUp() {
        AppLock.authenticator = BiometricAuthenticator { false }
        AppLock.enabled = false
    }

    @Test
    fun isUnlockedDefaultsToFalseWhenLockDisabled() {
        AppLock.enabled = false
        assertFalse("Lock disabled must never block the app", AppLock.isLocked())
    }

    @Test
    fun isLockedReturnsFalseWhenLockDisabledEvenIfAuthenticatorDenies() {
        AppLock.enabled = false
        AppLock.authenticator = BiometricAuthenticator { false }
        assertFalse(AppLock.isLocked())
    }

    @Test
    fun isLockedReturnsTrueWhenLockEnabledAndNoPriorAuthentication() {
        AppLock.enabled = true
        AppLock.authenticator = BiometricAuthenticator { false }
        assertTrue("Lock enabled with no prior unlock must block content", AppLock.isLocked())
    }

    @Test
    fun unlockSucceedsWhenAuthenticatorReturnsTrue() {
        AppLock.enabled = true
        AppLock.authenticator = BiometricAuthenticator { true }
        assertTrue("unlock() must return true when biometric auth succeeds", AppLock.unlock())
        assertFalse("After a successful unlock the app must be unlocked", AppLock.isLocked())
    }

    @Test
    fun unlockFailsClosedWhenAuthenticatorReturnsFalse() {
        AppLock.enabled = true
        AppLock.authenticator = BiometricAuthenticator { false }
        assertFalse("unlock() must return false when biometric auth is denied", AppLock.unlock())
        assertTrue("A failed unlock must leave the app locked", AppLock.isLocked())
    }

    @Test
    fun unlockIsNoOpWhenLockDisabled() {
        AppLock.enabled = false
        AppLock.authenticator = BiometricAuthenticator { false }
        // When the lock is disabled, unlock() must not even consult the
        // authenticator — and must report success so callers can proceed.
        assertTrue(AppLock.unlock())
        assertFalse(AppLock.isLocked())
    }

    @Test
    fun resetForTestingClearsUnlockedState() {
        AppLock.enabled = true
        AppLock.authenticator = BiometricAuthenticator { true }
        AppLock.unlock()
        assertFalse(AppLock.isLocked())
        AppLock.resetForTesting()
        assertTrue("resetForTesting must restore the locked state", AppLock.isLocked())
    }
}
