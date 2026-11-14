package com.bufferbrew.perch.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Records the code startup and the main note flow run, so it ships precompiled. Run with
 * `./gradlew :app:generateBaselineProfile`; the result lands in app/src/release/generated/.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = PACKAGE, includeInStartupProfile = true) {
        // No mic: the overlay service then starts without the microphone type, which a slow
        // device can be refused (and crash on) once the launch Activity has already finished.
        // Recording isn't part of what this profiles.
        device.executeShellCommand("pm revoke $PACKAGE android.permission.RECORD_AUDIO")
        startToNotes()
        writeANote()
        floatTheNote()
    }
}
