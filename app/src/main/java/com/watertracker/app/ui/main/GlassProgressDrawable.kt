package com.watertracker.app.ui.main

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable

/** The tumbler from the campaign, filled by the ProgressBar's 0..10000 level. */
class GlassProgressDrawable : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val vessel = Path().apply {
        moveTo(-121f, -272f)
        cubicTo(-129f, -310f, 125f, -310f, 121f, -272f)
        lineTo(91f, -8f)
        cubicTo(89f, 28f, -88f, 28f, -91f, -8f)
        close()
    }
    private val facets = (0 until 8).map { i ->
        val left = -1f + i / 4f
        val right = left + .25f
        val top = -245f + 18f * (1 - left * left)
        Path().apply {
            moveTo(left * 118, top)
            lineTo((left + .08f) * 118, top - 9)
            lineTo(right * 118, top + 3)
            lineTo(right * 87, -7f)
            lineTo(left * 87, -4f)
            close()
        }
    }
    private val water = LinearGradient(-110f, -260f, 115f, 0f,
        intArrayOf(0xff8eaaa9.toInt(), 0xffb9ccbf.toInt(), 0xff66848a.toInt()),
        null, Shader.TileMode.CLAMP)

    override fun draw(canvas: Canvas) {
        val scale = minOf(bounds.width() / 360f, bounds.height() / 360f)
        canvas.save()
        canvas.translate(bounds.exactCenterX() - 15 * scale, bounds.exactCenterY() + 137 * scale)
        canvas.scale(scale, scale)
        canvas.rotate(3.4f)
        paint.style = Paint.Style.FILL
        paint.color = 0x37637a71
        canvas.drawOval(-89f, -12f, 185f, 27f, paint)
        paint.color = 0xb0e7e7cf.toInt()
        canvas.drawPath(vessel, paint)
        canvas.save()
        canvas.clipPath(vessel)
        if (level > 0) {
            val surface = -9f - 251f * level / 10000f
            val radius = 90f - surface * .117f
            paint.color = -1
            paint.shader = water
            canvas.drawRect(-125f, surface, 125f, 28f, paint)
            paint.shader = null
            paint.color = 0xffc4d9cb.toInt()
            canvas.drawOval(-radius, surface - radius * .17f, radius, surface + radius * .17f, paint)
        }
        facets.forEachIndexed { index, path ->
            paint.color = if (index % 2 == 0) 0x32455c60 else 0x60fff9de
            canvas.drawPath(path, paint)
        }
        canvas.restore()
        paint.color = 0xa6486360.toInt()
        paint.strokeWidth = 2.4f
        paint.style = Paint.Style.STROKE
        canvas.drawPath(vessel, paint)
        paint.style = Paint.Style.FILL
        paint.color = 0xffa5b5a9.toInt()
        canvas.drawOval(-121f, -297f, 121f, -247f, paint)
        paint.color = 0xffd7decb.toInt()
        canvas.drawOval(-116f, -293f, 116f, -253f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = 0xfffff8df.toInt()
        paint.strokeWidth = 4f
        canvas.drawArc(-119f, -296f, 119f, -248f, 4f, 215f, false, paint)
        canvas.restore()
    }

    override fun onLevelChange(level: Int): Boolean {
        invalidateSelf()
        return true
    }
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
