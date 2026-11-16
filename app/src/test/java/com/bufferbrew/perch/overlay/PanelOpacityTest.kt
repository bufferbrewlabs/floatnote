package com.bufferbrew.perch.overlay

import com.bufferbrew.perch.overlay.OverlayPrefs.Companion.MIN_OPACITY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PanelOpacityTest {
    @Test
    fun `text stays solid while only the paper fades`() {
        listOf(1f, 0.9f, 0.75f, PanelOpacity.TEXT_FADE_START).forEach {
            assertEquals(1f, PanelOpacity.content(it), 0.0001f)
            assertEquals(it, PanelOpacity.paper(it), 0.0001f)
        }
    }

    @Test
    fun `at the bottom of the slider everything is nearly invisible`() {
        assertEquals(MIN_OPACITY, PanelOpacity.content(MIN_OPACITY), 0.0001f)
        assertEquals(MIN_OPACITY, PanelOpacity.paper(MIN_OPACITY), 0.0001f)
    }

    @Test
    fun `below the midpoint text fades steadily, never below the paper`() {
        var previous = PanelOpacity.content(MIN_OPACITY)
        var o = MIN_OPACITY
        while (o < PanelOpacity.TEXT_FADE_START) {
            o += 0.01f
            val content = PanelOpacity.content(o)
            assertTrue("content rises with opacity at $o", content >= previous)
            assertTrue("text never fainter than the paper at $o", content >= PanelOpacity.paper(o) - 0.0001f)
            previous = content
        }
    }

    @Test
    fun `out of range values are clamped`() {
        assertEquals(1f, PanelOpacity.content(2f), 0.0001f)
        assertEquals(MIN_OPACITY, PanelOpacity.content(0f), 0.0001f)
    }
}
