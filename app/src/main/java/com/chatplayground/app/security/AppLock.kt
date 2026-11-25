package com.chatplayground.app.security

/**
 * Startup gate that keeps the app's content locked until the user authenticates.
 *
 * When [enabled] is true and no successful authentication has occurred since the
 * process started, [isLocked] returns `true` and callers (e.g. `MainActivity`
 * gating `webView.loadUrl`) must refuse to render content. [unlock] runs the
 * injected [authenticator]; on success the app becomes unlocked (fail closed on
 * denial — [isLocked] stays `true`). When [enabled] is false, [isLocked] always
 * returns `false` and [unlock] returns `true` without consulting the authenticator.
 *
 * The [authenticator] seam defaults to a deny-all implementation so protected
 * reads fail closed until production wiring supplies a BiometricPrompt-backed
 * authenticator. Unit tests substitute a fake because BiometricPrompt cannot run
 * under Robolectric.
 */
object AppLock {
    /** Whether the startup lock is active. Defaults to false. */
    @Volatile var enabled: Boolean = false

    /** True when the lock is enabled and no successful unlock has occurred yet. */
    @Volatile private var unlocked: Boolean = false

    /** Injectable authenticator seam; deny-all by default (fail closed). */
    @Volatile var authenticator: BiometricAuthenticator = BiometricAuthenticator { false }

    /** Returns true when the app's content is locked (blocked from rendering). */
    fun isLocked(): Boolean = enabled && !unlocked

    /**
     * Runs the authenticator. Returns `true` on success (records the unlocked
     * state so subsequent [isLocked] calls return `false`); returns `false`
     * when authentication is denied and leaves the app locked. Returns `true`
     * immediately when [enabled] is false (lock disabled — no gate).
     */
    fun unlock(): Boolean {
        if (!enabled) {
            unlocked = false
            return true
        }
        val success = authenticator.authenticate(/* context */ null)
        if (success) unlocked = true
        return success
    }

    /** Clears the unlocked state so the lock re-engages. Tests only. */
    fun resetForTesting() {
        unlocked = false
    }
}
