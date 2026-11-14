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
 * AIRA M1 avatar prototype.
 * This is a lightweight custom Canvas illustration, not a true 3D model.
 * M2 will replace/augment it with a rigged 3D character.
 */
class ChibiView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density
    private fun d(value: Float) = value * density

    enum class AvatarState { IDLE, LISTENING, THINKING, SPEAKING }

    var avatarState: AvatarState = AvatarState.IDLE
        set(value) { field = value; invalidate() }

    var compactMode: Boolean = false
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    init {
        contentDescription = "AIRA: uzun kumush sochli, binafsha ko‘zli futuristik anime qahramoni"
        isClickable = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = d(if (compactMode) 42f else 84f).toInt()
        val h = d(if (compactMode) 51f else 102f).toInt()
        setMeasuredDimension(w, h)
    }

    private fun setPaint(color: Int, style: Paint.Style = Paint.Style.FILL, stroke: Float = 1f) {
        paint.color = color
        paint.style = style
        paint.strokeWidth = d(stroke)
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val time = System.currentTimeMillis() / 1000.0
        val idle = sin(time * 1.8).toFloat()
        val hairMove = sin(time * 1.15 + 0.5).toFloat()
        val blink = (time % if (avatarState == AvatarState.LISTENING) 2.8 else 4.7) in 0.0..0.12

        canvas.save()
        canvas.scale(width / d(120f), height / d(146f))
        canvas.translate(0f, d(1.4f + idle * 1.0f))

        val cx = d(60f)
        val silver = Color.rgb(222, 225, 242)
        val silverBright = Color.rgb(250, 250, 255)
        val silverShade = Color.rgb(143, 148, 181)
        val violet = Color.rgb(155, 105, 255)
        val violetDark = Color.rgb(85, 48, 155)
        val skin = Color.rgb(250, 224, 222)
        val black = Color.rgb(24, 23, 36)
        val white = Color.rgb(238, 238, 247)

        // Soft purple aura, clipped to the avatar bounds.
        val auraAlpha = when (avatarState) {
            AvatarState.IDLE -> 35
            AvatarState.LISTENING -> (75 + 25 * sin(time * 5.0)).toInt()
            AvatarState.THINKING -> (55 + 20 * sin(time * 3.0)).toInt()
            AvatarState.SPEAKING -> 85
        }
        setPaint(Color.argb(auraAlpha, 145, 92, 255))
        canvas.drawOval(RectF(cx-d(39f), d(8f), cx+d(39f), d(140f)), paint)

        // Long hair flowing behind the shoulders.
        canvas.save()
        canvas.rotate(hairMove * 2.4f, cx, d(52f))
        setPaint(silverShade)
        val backHair = Path().apply {
            moveTo(cx-d(27f), d(34f))
            quadTo(cx-d(47f), d(47f), cx-d(39f), d(105f))
            quadTo(cx-d(37f), d(129f), cx-d(22f), d(136f))
            quadTo(cx-d(29f), d(105f), cx-d(23f), d(80f))
            lineTo(cx+d(23f), d(80f))
            quadTo(cx+d(29f), d(105f), cx+d(22f), d(136f))
            quadTo(cx+d(39f), d(129f), cx+d(39f), d(105f))
            quadTo(cx+d(47f), d(47f), cx+d(27f), d(34f))
            close()
        }
        canvas.drawPath(backHair, paint)
        setPaint(silver)
        canvas.drawRoundRect(RectF(cx-d(34f), d(48f), cx-d(24f), d(119f)), d(5f), d(5f), paint)
        canvas.drawRoundRect(RectF(cx+d(24f), d(48f), cx+d(34f), d(119f)), d(5f), d(5f), paint)
        setPaint(silverBright, Paint.Style.STROKE, 1.4f)
        canvas.drawLine(cx-d(29f), d(57f), cx-d(28f), d(112f), paint)
        canvas.drawLine(cx+d(29f), d(57f), cx+d(28f), d(112f), paint)
        canvas.restore()

        // Shoulders and futuristic black/white outfit.
        setPaint(black)
        canvas.drawRoundRect(RectF(cx-d(30f), d(83f), cx+d(30f), d(130f)), d(13f), d(13f), paint)
        setPaint(white)
        val jacket = Path().apply {
            moveTo(cx-d(29f), d(87f))
            lineTo(cx-d(18f), d(79f))
            lineTo(cx-d(9f), d(91f))
            lineTo(cx, d(105f))
            lineTo(cx+d(9f), d(91f))
            lineTo(cx+d(18f), d(79f))
            lineTo(cx+d(29f), d(87f))
            lineTo(cx+d(37f), d(126f))
            lineTo(cx-d(37f), d(126f))
            close()
        }
        canvas.drawPath(jacket, paint)
        setPaint(black)
        canvas.drawRoundRect(RectF(cx-d(12f), d(92f), cx+d(12f), d(130f)), d(5f), d(5f), paint)
        setPaint(violet)
        canvas.drawRoundRect(RectF(cx-d(2f), d(96f), cx+d(2f), d(122f)), d(1f), d(1f), paint)
        canvas.drawCircle(cx, d(111f), d(4f), paint)
        setPaint(violetDark)
        canvas.drawCircle(cx, d(111f), d(2f), paint)

        // Neck and face.
        setPaint(skin)
        canvas.drawRoundRect(RectF(cx-d(8f), d(67f), cx+d(8f), d(88f)), d(5f), d(5f), paint)
        canvas.drawOval(RectF(cx-d(25f), d(19f), cx+d(25f), d(79f)), paint)

        // Side locks frame the face.
        setPaint(silverShade)
        canvas.drawRoundRect(RectF(cx-d(26f), d(29f), cx-d(19f), d(82f)), d(4f), d(4f), paint)
        canvas.drawRoundRect(RectF(cx+d(19f), d(29f), cx+d(26f), d(82f)), d(4f), d(4f), paint)

        // Crown and layered fringe.
        setPaint(silver)
        val fringe = Path().apply {
            moveTo(cx-d(25f), d(49f))
            quadTo(cx-d(31f), d(10f), cx, d(12f))
            quadTo(cx+d(31f), d(10f), cx+d(25f), d(49f))
            lineTo(cx+d(13f), d(38f))
            lineTo(cx+d(4f), d(48f))
            lineTo(cx-d(4f), d(35f))
            lineTo(cx-d(15f), d(49f))
            close()
        }
        canvas.drawPath(fringe, paint)
        setPaint(silverBright, Paint.Style.STROKE, 1.6f)
        canvas.drawArc(RectF(cx-d(19f), d(17f), cx+d(18f), d(48f)), 205f, 112f, false, paint)

        // State aura indicator.
        if (avatarState == AvatarState.THINKING) {
            setPaint(violet, Paint.Style.STROKE, 1.8f)
            canvas.drawArc(RectF(cx-d(34f), d(4f), cx+d(34f), d(72f)), -65f, 95f, false, paint)
            setPaint(silverBright)
            canvas.drawCircle(cx+d(27f), d(18f), d(2.2f), paint)
        }

        // Violet eyes with dark lashes.
        if (blink) {
            setPaint(black, Paint.Style.STROKE, 2f)
            canvas.drawArc(RectF(cx-d(17f), d(44f), cx-d(4f), d(57f)), 15f, 150f, false, paint)
            canvas.drawArc(RectF(cx+d(4f), d(44f), cx+d(17f), d(57f)), 15f, 150f, false, paint)
        } else {
            setPaint(Color.WHITE)
            canvas.drawOval(RectF(cx-d(17f), d(42f), cx-d(4f), d(58f)), paint)
            canvas.drawOval(RectF(cx+d(4f), d(42f), cx+d(17f), d(58f)), paint)
            setPaint(violet)
            canvas.drawOval(RectF(cx-d(15f), d(44f), cx-d(6f), d(58f)), paint)
            canvas.drawOval(RectF(cx+d(6f), d(44f), cx+d(15f), d(58f)), paint)
            setPaint(violetDark)
            canvas.drawOval(RectF(cx-d(12f), d(47f), cx-d(7f), d(57f)), paint)
            canvas.drawOval(RectF(cx+d(7f), d(47f), cx+d(12f), d(57f)), paint)
            setPaint(Color.WHITE)
            canvas.drawCircle(cx-d(10f), d(47f), d(2f), paint)
            canvas.drawCircle(cx+d(9f), d(47f), d(2f), paint)
        }

        // Calm brows, restrained expression and cheek shading.
        setPaint(silverShade, Paint.Style.STROKE, 1.7f)
        canvas.drawLine(cx-d(17f), d(39f), cx-d(6f), d(38f), paint)
        canvas.drawLine(cx+d(6f), d(38f), cx+d(17f), d(39f), paint)
        setPaint(Color.argb(105, 215, 110, 155))
        canvas.drawOval(RectF(cx-d(20f), d(58f), cx-d(13f), d(61f)), paint)
        canvas.drawOval(RectF(cx+d(13f), d(58f), cx+d(20f), d(61f)), paint)
        setPaint(violetDark, Paint.Style.STROKE, 1.2f)
        when (avatarState) {
            AvatarState.SPEAKING -> canvas.drawOval(RectF(cx-d(3f), d(58f), cx+d(3f), d(65f)), paint)
            AvatarState.LISTENING -> canvas.drawArc(RectF(cx-d(5f), d(58f), cx+d(5f), d(64f)), 10f, 160f, false, paint)
            AvatarState.THINKING -> canvas.drawLine(cx-d(3f), d(61f), cx+d(3f), d(61f), paint)
            AvatarState.IDLE -> canvas.drawArc(RectF(cx-d(4f), d(58f), cx+d(4f), d(63f)), 15f, 150f, false, paint)
        }

        // Small violet hair ornaments.
        setPaint(violet)
        canvas.drawCircle(cx-d(25f), d(30f), d(4f), paint)
        canvas.drawCircle(cx+d(25f), d(30f), d(4f), paint)
        setPaint(Color.rgb(225, 210, 255))
        canvas.drawCircle(cx-d(25f), d(30f), d(1.6f), paint)
        canvas.drawCircle(cx+d(25f), d(30f), d(1.6f), paint)

        // AIRA's signature chest emblem.
        setPaint(violet)
        val emblem = Path().apply {
            moveTo(cx, d(103f))
            lineTo(cx+d(5f), d(108f))
            lineTo(cx, d(113f))
            lineTo(cx-d(5f), d(108f))
            close()
        }
        canvas.drawPath(emblem, paint)

        canvas.restore()
        postInvalidateDelayed(60L)
    }
}
