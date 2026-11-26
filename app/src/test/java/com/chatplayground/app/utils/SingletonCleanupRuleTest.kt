package com.chatplayground.app.utils

import com.chatplayground.app.security.AppLock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Unit tests for [SingletonCleanupRule] verifying that global singletons
 * ([AppLock] and [DesktopModeHandler]) are correctly reset after test execution.
 * Implements Vision Item 101.
 */
class SingletonCleanupRuleTest {

    @Test
    fun ruleResetsAppLockAndDesktopModeOnFinished() {
        AppLock.enabled = true
        AppLock.authenticator = { true }
        DesktopModeHandler.applyDesktopMode(true)

        val rule = SingletonCleanupRule()
        val dummyStatement = object : Statement() {
            override fun evaluate() {
                assertTrue(AppLock.enabled)
                assertTrue(DesktopModeHandler.isDesktop)
            }
        }

        val evaluatedStatement = rule.apply(dummyStatement, Description.createTestDescription(javaClass, "dummy"))
        evaluatedStatement.evaluate()

        assertFalse("AppLock.enabled should be reset to false", AppLock.enabled)
        assertFalse("DesktopModeHandler.isDesktop should be reset to false", DesktopModeHandler.isDesktop)
    }
}
