package com.bufferbrew.perch.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.Until

internal const val PACKAGE = "com.bufferbrew.perch"
private const val WAIT_MS = 5_000L
private const val SWIPE_STEPS = 20

/** Cold start to the notes list. */
internal fun MacrobenchmarkScope.startToNotes() {
    pressHome()
    startActivityAndWait()
    device.wait(Until.hasObject(By.text("New note")), WAIT_MS)
}

/** The main note flow: new note, type a line, back to the list, scroll it. */
internal fun MacrobenchmarkScope.writeANote() {
    clickText("New note")
    device.waitForIdle()
    device.executeShellCommand("input text Groceries")
    device.waitForIdle()
    device.pressBack() // keyboard
    device.pressBack() // back to the list (predictive back on 13+)
    device.wait(Until.hasObject(By.text("New note")), WAIT_MS)
    device.waitForIdle()
    // Raw swipes, not a held list object: screens animate in and out, which leaves held
    // objects stale.
    val x = device.displayWidth / 2
    val top = device.displayHeight / 3
    val bottom = device.displayHeight * 2 / 3
    device.swipe(x, bottom, x, top, SWIPE_STEPS)
    device.swipe(x, top, x, bottom, SWIPE_STEPS)
    device.waitForIdle()
}

/** Finds [text] fresh and clicks it, once more if the screen replaced it mid-way. */
private fun MacrobenchmarkScope.clickText(text: String) {
    repeat(2) {
        try {
            device.wait(Until.findObject(By.text(text)), WAIT_MS)?.click() ?: return
            return
        } catch (_: StaleObjectException) {
            device.waitForIdle()
        }
    }
}

/** Opens the floating note over the launcher and minimises it to the bubble with Back. */
internal fun MacrobenchmarkScope.floatTheNote() {
    device.executeShellCommand("appops set $PACKAGE SYSTEM_ALERT_WINDOW allow")
    pressHome()
    device.executeShellCommand("am start -n $PACKAGE/.FloatLaunchActivity")
    device.wait(Until.hasObject(By.pkg(PACKAGE)), WAIT_MS)
    device.waitForIdle()
    device.pressBack() // minimise to the bubble
    device.waitForIdle()
}
