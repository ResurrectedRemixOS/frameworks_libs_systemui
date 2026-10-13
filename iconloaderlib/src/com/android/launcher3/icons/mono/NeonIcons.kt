/*
 * SPDX-FileCopyrightText: The Resurrected Remix Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.launcher3.icons.mono

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

/**
 * RR neon icons: themed icons drawn as the app's glyph in the accent inside a glowing ring,
 * like the RR Settings homepage. The launcher sets these at start.
 */
object NeonIcons {
    @JvmField var enabled = false
    @JvmField var ringColor = 0xFFFF3B5C.toInt()
    @JvmField var discColor = 0xFF14090C.toInt()

    private val disc = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    /** Draws the ring and disc into [bounds]; the glyph goes on top. Returns the glyph bounds. */
    @JvmStatic
    fun drawRing(canvas: Canvas, bounds: Rect, alpha: Int): Rect {
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val size = minOf(bounds.width(), bounds.height()).toFloat()
        val stroke = size * 0.045f
        val r = size / 2f - stroke * 2.2f
        disc.color = discColor
        disc.alpha = alpha
        canvas.drawCircle(cx, cy, r, disc)
        glow.color = ringColor
        glow.alpha = alpha * 140 / 255
        glow.strokeWidth = stroke * 2.4f
        glow.maskFilter = BlurMaskFilter(stroke * 2f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawCircle(cx, cy, r, glow)
        ring.color = ringColor
        ring.alpha = alpha
        ring.strokeWidth = stroke
        canvas.drawCircle(cx, cy, r, ring)
        val glyph = (r * 1.2f).toInt()
        return Rect((cx - glyph / 2).toInt(), (cy - glyph / 2).toInt(),
            (cx + glyph / 2).toInt(), (cy + glyph / 2).toInt())
    }
}
