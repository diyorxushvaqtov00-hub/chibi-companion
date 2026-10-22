package uz.diyor.companion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import kotlin.math.sin

/**
 * Small, locally rendered Nino-inspired chibi.
 * The hair, twin tails, butterfly ribbons and body have independent gentle motion.
 */
class ChibiView(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density
    private fun d(v: Float) = v * density

    init {
        contentDescription = "Pushti sochli, qora kapalak bantli anime chibi"
        layoutParams = android.view.ViewGroup.LayoutParams(d(64f).toInt(), d(78f).toInt())
        isClickable = true
    }

    var compactMode: Boolean = false
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = d(if (compactMode) 32f else 64f).toInt()
        val h = d(if (compactMode) 39f else 78f).toInt()
        setMeasuredDimension(w, h)
    }

    private fun color(c: Int, style: Paint.Style = Paint.Style.FILL, stroke: Float = 1f) {
        p.color = c
        p.style = style
        p.strokeWidth = d(stroke)
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val t = System.currentTimeMillis() / 1000.0
        val sway = sin(t * 2.1).toFloat()
        val swaySlow = sin(t * 1.25 + 0.8).toFloat()
        val breathe = sin(t * 2.8).toFloat()
        val blink = (t % 4.1) in 0.0..0.13

        canvas.save()
        canvas.scale(width / d(104f), height / d(126f))

        // Gentle idle bounce; keep all movement inside the compact drawing area.
        canvas.translate(0f, d(1.5f + breathe * 1.1f))
        canvas.rotate(sway * 1.1f, d(52f), d(67f))

        val cx = d(52f)
        val pink = Color.rgb(247, 137, 190)
        val pinkLight = Color.rgb(255, 190, 222)
        val pinkShadow = Color.rgb(186, 75, 133)
        val ribbon = Color.rgb(24, 31, 43)
        val ribbonLight = Color.rgb(72, 91, 111)
        val skin = Color.rgb(255, 226, 219)
        val ink = Color.rgb(45, 29, 49)

        // Ground shadow softly follows the body bounce.
        color(Color.argb(45, 20, 10, 30))
        canvas.drawOval(RectF(cx-d(24f), d(116f), cx+d(24f), d(121f)), p)

        // Twin tails behind the head, each with its own slight sway.
        canvas.save()
        canvas.rotate(swaySlow * 5f, cx-d(23f), d(43f))
        color(pinkShadow)
        canvas.drawRoundRect(RectF(cx-d(31f), d(39f), cx-d(18f), d(84f)), d(7f), d(7f), p)
        color(pink)
        canvas.drawRoundRect(RectF(cx-d(29f), d(39f), cx-d(20f), d(81f)), d(5f), d(5f), p)
        color(pinkLight, Paint.Style.STROKE, 1.5f)
        canvas.drawLine(cx-d(25f), d(46f), cx-d(25f), d(73f), p)
        canvas.restore()

        canvas.save()
        canvas.rotate(-swaySlow * 5f, cx+d(23f), d(43f))
        color(pinkShadow)
        canvas.drawRoundRect(RectF(cx+d(18f), d(39f), cx+d(31f), d(84f)), d(7f), d(7f), p)
        color(pink)
        canvas.drawRoundRect(RectF(cx+d(20f), d(39f), cx+d(29f), d(81f)), d(5f), d(5f), p)
        color(pinkLight, Paint.Style.STROKE, 1.5f)
        canvas.drawLine(cx+d(25f), d(46f), cx+d(25f), d(73f), p)
        canvas.restore()

        // Body and dark school-uniform inspired outfit.
        canvas.save()
        canvas.rotate(sway * 1.7f, cx, d(83f))
        color(skin)
        canvas.drawRoundRect(RectF(cx-d(12f), d(70f), cx+d(12f), d(91f)), d(7f), d(7f), p)
        color(Color.rgb(37, 43, 58))
        val dress = Path().apply {
            moveTo(cx-d(12f), d(77f))
            lineTo(cx+d(12f), d(77f))
            lineTo(cx+d(22f), d(105f))
            quadTo(cx, d(112f), cx-d(22f), d(105f))
            close()
        }
        canvas.drawPath(dress, p)
        color(Color.rgb(255, 211, 231))
        canvas.drawPath(Path().apply {
            moveTo(cx-d(9f), d(78f)); lineTo(cx, d(86f)); lineTo(cx+d(9f), d(78f))
            lineTo(cx+d(5f), d(91f)); lineTo(cx-d(5f), d(91f)); close()
        }, p)
        color(ribbon)
        canvas.drawCircle(cx, d(88f), d(3.5f), p)
        color(pink)
        canvas.drawCircle(cx, d(88f), d(1.7f), p)
        // Legs and shoes
        color(skin)
        canvas.drawRoundRect(RectF(cx-d(10f), d(104f), cx-d(3f), d(114f)), d(3f), d(3f), p)
        canvas.drawRoundRect(RectF(cx+d(3f), d(104f), cx+d(10f), d(114f)), d(3f), d(3f), p)
        color(ink)
        canvas.drawRoundRect(RectF(cx-d(14f), d(111f), cx-d(1f), d(118f)), d(3f), d(3f), p)
        canvas.drawRoundRect(RectF(cx+d(1f), d(111f), cx+d(14f), d(118f)), d(3f), d(3f), p)
        canvas.restore()

        // Head silhouette and cheek/ear details.
        color(pinkShadow)
        canvas.drawOval(RectF(cx-d(28f), d(14f), cx+d(28f), d(73f)), p)
        color(skin)
        canvas.drawOval(RectF(cx-d(23f), d(22f), cx+d(23f), d(69f)), p)
        canvas.drawOval(RectF(cx-d(25f), d(43f), cx-d(20f), d(53f)), p)
        canvas.drawOval(RectF(cx+d(20f), d(43f), cx+d(25f), d(53f)), p)

        // Pink fringe with asymmetric locks, swaying very subtly.
        canvas.save()
        canvas.rotate(sway * 1.2f, cx, d(34f))
        color(pink)
        val hair = Path().apply {
            moveTo(cx-d(24f), d(45f))
            quadTo(cx-d(31f), d(13f), cx, d(15f))
            quadTo(cx+d(30f), d(13f), cx+d(24f), d(45f))
            lineTo(cx+d(13f), d(34f))
            lineTo(cx+d(5f), d(43f))
            lineTo(cx-d(2f), d(32f))
            lineTo(cx-d(12f), d(44f))
            close()
        }
        canvas.drawPath(hair, p)
        color(pinkLight, Paint.Style.STROKE, 1.5f)
        canvas.drawArc(RectF(cx-d(20f), d(20f), cx+d(18f), d(45f)), 205f, 115f, false, p)
        canvas.restore()

        // Expressive eyes; blink is a short natural close, not a large facial change.
        if (blink) {
            color(ink, Paint.Style.STROKE, 2.2f)
            canvas.drawArc(RectF(cx-d(17f), d(41f), cx-d(4f), d(54f)), 15f, 150f, false, p)
            canvas.drawArc(RectF(cx+d(4f), d(41f), cx+d(17f), d(54f)), 15f, 150f, false, p)
        } else {
            color(Color.WHITE)
            canvas.drawOval(RectF(cx-d(17f), d(40f), cx-d(4f), d(56f)), p)
            canvas.drawOval(RectF(cx+d(4f), d(40f), cx+d(17f), d(56f)), p)
            color(Color.rgb(90, 112, 153))
            canvas.drawOval(RectF(cx-d(15f), d(43f), cx-d(6f), d(56f)), p)
            canvas.drawOval(RectF(cx+d(6f), d(43f), cx+d(15f), d(56f)), p)
            color(ink)
            canvas.drawOval(RectF(cx-d(13f), d(45f), cx-d(7f), d(55f)), p)
            canvas.drawOval(RectF(cx+d(7f), d(45f), cx+d(13f), d(55f)), p)
            color(Color.WHITE)
            canvas.drawCircle(cx-d(11f), d(46f), d(2f), p)
            canvas.drawCircle(cx+d(9f), d(46f), d(2f), p)
        }

        // Eyebrows, blush and small smile.
        color(pinkShadow, Paint.Style.STROKE, 1.6f)
        canvas.drawLine(cx-d(16f), d(38f), cx-d(6f), d(37f), p)
        canvas.drawLine(cx+d(6f), d(37f), cx+d(16f), d(38f), p)
        color(Color.argb(145, 255, 126, 165))
        canvas.drawOval(RectF(cx-d(20f), d(55f), cx-d(13f), d(59f)), p)
        canvas.drawOval(RectF(cx+d(13f), d(55f), cx+d(20f), d(59f)), p)
        color(Color.rgb(150, 61, 92), Paint.Style.STROKE, 1.4f)
        canvas.drawArc(RectF(cx-d(4f), d(55f), cx+d(4f), d(61f)), 15f, 150f, false, p)

        // Black/teal butterfly ribbons, with independently moving loops and tails.
        canvas.save()
        canvas.rotate(swaySlow * 7f, cx-d(23f), d(27f))
        drawButterfly(canvas, cx-d(23f), d(27f), -1f, ribbon, ribbonLight)
        canvas.restore()
        canvas.save()
        canvas.rotate(-swaySlow * 7f, cx+d(23f), d(27f))
        drawButterfly(canvas, cx+d(23f), d(27f), 1f, ribbon, ribbonLight)
        canvas.restore()

        // Tiny highlight marks; no large outline that makes the overlay look like a box.
        color(Color.argb(180, 255, 210, 238))
        canvas.drawCircle(cx-d(34f), d(61f), d(1.2f), p)
        canvas.drawCircle(cx+d(34f), d(67f), d(1.2f), p)

        canvas.restore()
        postInvalidateDelayed(50L)
    }

    private fun drawButterfly(
        canvas: Canvas,
        x: Float,
        y: Float,
        side: Float,
        base: Int,
        highlight: Int
    ) {
        color(base)
        canvas.drawOval(RectF(x-d(8f), y-d(7f), x+d(1f), y+d(1f)), p)
        canvas.drawOval(RectF(x-d(1f), y-d(7f), x+d(8f), y+d(1f)), p)
        canvas.drawOval(RectF(x-d(6f), y, x+d(1f), y+d(7f)), p)
        canvas.drawOval(RectF(x-d(1f), y, x+d(6f), y+d(7f)), p)
        color(highlight, Paint.Style.STROKE, 1f)
        canvas.drawLine(x-d(5f), y-d(4f), x-d(2f), y-d(2f), p)
        canvas.drawLine(x+d(5f), y-d(4f), x+d(2f), y-d(2f), p)
        color(Color.rgb(244, 198, 224))
        canvas.drawCircle(x, y, d(1.8f), p)
        color(base)
        val tail = Path().apply {
            moveTo(x, y+d(5f))
            lineTo(x+side*d(4f), y+d(13f))
            lineTo(x+side*d(1f), y+d(10f))
            lineTo(x-side*d(1f), y+d(13f))
            close()
        }
        canvas.drawPath(tail, p)
    }
}
