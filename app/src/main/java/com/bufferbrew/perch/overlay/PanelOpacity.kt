package com.bufferbrew.perch.overlay

import com.bufferbrew.perch.overlay.OverlayPrefs.Companion.MIN_OPACITY

/**
 * How the floating panel fades, in two stages, so the slider can both keep text readable and
 * still make the note nearly invisible:
 * - from 100% down to [TEXT_FADE_START] only the paper fades; text and controls stay solid,
 *   so the note reads over any app;
 * - below that, the content fades too, reaching the paper's own level at [MIN_OPACITY].
 */
object PanelOpacity {
    /**
     * Above this only the paper is see-through. 50% keeps body text at about 4:1 or better over
     * white, black, grey and saturated app screens (light papers; 3.2:1 for dark), against under
     * 3:1 when the text faded with the paper; the 30-50% range is for "nearly invisible".
     */
    const val TEXT_FADE_START = 0.5f

    /** The paper and its border: exactly the opacity you chose. */
    fun paper(opacity: Float): Float = opacity.coerceIn(MIN_OPACITY, 1f)

    /** Text and controls: solid down to [TEXT_FADE_START], then down to [MIN_OPACITY] with the paper. */
    fun content(opacity: Float): Float {
        val o = opacity.coerceIn(MIN_OPACITY, 1f)
        if (o >= TEXT_FADE_START) return 1f
        return MIN_OPACITY + (1f - MIN_OPACITY) * (o - MIN_OPACITY) / (TEXT_FADE_START - MIN_OPACITY)
    }
}
