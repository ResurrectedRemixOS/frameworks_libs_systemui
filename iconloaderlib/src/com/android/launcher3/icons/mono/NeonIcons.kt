/*
 * SPDX-FileCopyrightText: The Resurrected Remix Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.launcher3.icons.mono

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface

/**
 * RR neon icons: themed icons drawn as the app's glyph inside a glowing ring, like the RR
 * Settings homepage. The launcher sets these from its preferences at start.
 */
object NeonIcons {
    @JvmField var enabled = false
    @JvmField var ringColor = 0xFFFF3B5C.toInt()
    @JvmField var discColor = 0xFF14090C.toInt()
    /** Each ring takes the app's own color instead of the accent. */
    @JvmField var perAppColor = false
    /** Glow strength, 0 to 100. */
    @JvmField var glow = 60
    /** 0 thin, 1 medium, 2 bold. */
    @JvmField var thickness = 1
    /** A light wallpaper: the disc turns white and the glow softens. */
    @JvmField var light = false
    /** Folders get the ring too. */
    @JvmField var folders = true

    private const val LIGHT_DISC = 0xFFF7F3F4.toInt()
    private val STROKES = floatArrayOf(0.03f, 0.045f, 0.065f)

    private val disc = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    /** The ring's color for an app whose icon averages [iconColor]. */
    @JvmStatic
    fun colorFor(iconColor: Int): Int {
        if (!perAppColor || Color.alpha(iconColor) == 0) return ringColor
        val hsv = FloatArray(3)
        Color.colorToHSV(iconColor, hsv)
        // Bright and saturated enough to glow on the dark disc.
        hsv[1] = hsv[1].coerceAtLeast(0.55f)
        hsv[2] = if (light) hsv[2].coerceIn(0.45f, 0.75f) else hsv[2].coerceAtLeast(0.85f)
        return Color.HSVToColor(hsv)
    }

    /** Draws the disc and ring into [bounds]; the glyph goes on top. Returns the glyph bounds. */
    @JvmStatic
    @JvmOverloads
    fun drawRing(canvas: Canvas, bounds: Rect, alpha: Int, color: Int = ringColor): Rect {
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val size = minOf(bounds.width(), bounds.height()).toFloat()
        val r = drawRing(canvas, cx, cy, size, alpha, color)
        val glyph = (r * 1.2f).toInt()
        return Rect((cx - glyph / 2).toInt(), (cy - glyph / 2).toInt(),
            (cx + glyph / 2).toInt(), (cy + glyph / 2).toInt())
    }

    /** The ring around a folder of [radius] centered at [cx], [cy]. */
    @JvmStatic
    fun drawFolderRing(canvas: Canvas, cx: Float, cy: Float, radius: Float, alpha: Int) {
        drawRing(canvas, cx, cy, radius * 2f, alpha, ringColor)
    }

    /** Returns the ring's radius. */
    private fun drawRing(canvas: Canvas, cx: Float, cy: Float, size: Float, alpha: Int,
            color: Int): Float {
        val stroke = size * STROKES[thickness.coerceIn(0, STROKES.size - 1)]
        val r = size / 2f - stroke * 2.2f
        disc.color = if (light) LIGHT_DISC else discColor
        disc.alpha = alpha
        canvas.drawCircle(cx, cy, r, disc)
        val glowStrength = glow.coerceIn(0, 100) / 100f * (if (light) 0.5f else 1f)
        if (glowStrength > 0f) {
            glowPaint.color = color
            glowPaint.alpha = (alpha * 0.85f * glowStrength).toInt()
            glowPaint.strokeWidth = stroke * 2.4f
            glowPaint.maskFilter = BlurMaskFilter(stroke * (1f + 2f * glowStrength),
                BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(cx, cy, r, glowPaint)
        }
        ring.color = color
        ring.alpha = alpha
        ring.strokeWidth = stroke
        canvas.drawCircle(cx, cy, r, ring)
        return r
    }

    /**
     * The first letter of the app's name as an alpha mask the size of an icon, for apps without
     * a monochrome icon. Null if the app has no usable name.
     */
    @JvmStatic
    fun letterBitmap(context: Context, packageName: String, size: Int): Bitmap? {
        val label = try {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) {
            return null
        }
        val first = label.trim().firstOrNull { it.isLetterOrDigit() } ?: return null
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ALPHA_8)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = size * 0.42f
        }
        val text = first.uppercaseChar().toString()
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        Canvas(bitmap).drawText(text, size / 2f, size / 2f + bounds.height() / 2f, paint)
        return bitmap
    }
}
