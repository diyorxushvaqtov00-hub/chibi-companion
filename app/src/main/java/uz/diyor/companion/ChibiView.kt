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
 * Lightweight, locally drawn chibi mascot. No network or image asset is required.
 */
class ChibiView(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density
    private fun d(v: Float) = v * density

    init {
        contentDescription = "Malika, suzuvchi chibi yordamchi"
        layoutParams = android.view.ViewGroup.LayoutParams(d(104f).toInt(), d(126f).toInt())
        isClickable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val t = System.currentTimeMillis() / 360.0
        val bob = sin(t).toFloat() * d(2.5f)
        canvas.save()
        canvas.translate(0f, bob)
        val cx = width / 2f
        fun paint(color: Int, style: Paint.Style = Paint.Style.FILL, stroke: Float = 1f) {
            p.color = color; p.style = style; p.strokeWidth = d(stroke); p.strokeCap = Paint.Cap.ROUND
        }

        // Soft purple shadow
        paint(Color.argb(70, 210, 70, 255))
        canvas.drawOval(RectF(cx-d(28f), d(112f), cx+d(28f), d(120f)), p)

        // Hair behind the body
        paint(Color.rgb(43, 20, 49))
        canvas.drawOval(RectF(cx-d(28f), d(23f), cx+d(29f), d(84f)), p)
        canvas.drawRoundRect(RectF(cx-d(25f), d(54f), cx-d(15f), d(91f)), d(8f), d(8f), p)
        canvas.drawRoundRect(RectF(cx+d(15f), d(52f), cx+d(25f), d(90f)), d(8f), d(8f), p)

        // Tiny gothic dress and legs
        paint(Color.rgb(82, 35, 112))
        val dress = Path().apply {
            moveTo(cx-d(13f), d(76f)); lineTo(cx+d(13f), d(76f))
            lineTo(cx+d(21f), d(101f)); quadraticTo(cx, d(109f), cx-d(21f), d(101f)); close()
        }
        canvas.drawPath(dress, p)
        paint(Color.rgb(244, 193, 220))
        canvas.drawRoundRect(RectF(cx-d(10f), d(101f), cx-d(3f), d(112f)), d(3f), d(3f), p)
        canvas.drawRoundRect(RectF(cx+d(3f), d(101f), cx+d(10f), d(112f)), d(3f), d(3f), p)
        paint(Color.rgb(30, 22, 45))
        canvas.drawRoundRect(RectF(cx-d(13f), d(109f), cx-d(1f), d(115f)), d(3f), d(3f), p)
        canvas.drawRoundRect(RectF(cx+d(1f), d(109f), cx+d(13f), d(115f)), d(3f), d(3f), p)

        // Face
        paint(Color.rgb(255, 224, 224))
        canvas.drawOval(RectF(cx-d(23f), d(21f), cx+d(23f), d(70f)), p)

        // Fringe and side locks
        paint(Color.rgb(37, 19, 43))
        val fringe = Path().apply {
            moveTo(cx-d(23f), d(44f)); quadraticTo(cx-d(29f), d(15f), cx, d(17f))
            quadraticTo(cx+d(29f), d(15f), cx+d(23f), d(44f))
            lineTo(cx+d(9f), d(34f)); lineTo(cx+d(1f), d(43f))
            lineTo(cx-d(7f), d(33f)); lineTo(cx-d(16f), d(44f)); close()
        }
        canvas.drawPath(fringe, p)
        paint(Color.rgb(108, 48, 132), Paint.Style.STROKE, 2f)
        canvas.drawArc(RectF(cx-d(20f), d(22f), cx+d(20f), d(56f)), 205f, 130f, false, p)

        // Big anime eyes
        paint(Color.WHITE)
        canvas.drawOval(RectF(cx-d(16f), d(42f), cx-d(4f), d(57f)), p)
        canvas.drawOval(RectF(cx+d(4f), d(42f), cx+d(16f), d(57f)), p)
        paint(Color.rgb(139, 55, 207))
        canvas.drawOval(RectF(cx-d(14f), d(44f), cx-d(6f), d(56f)), p)
        canvas.drawOval(RectF(cx+d(6f), d(44f), cx+d(14f), d(56f)), p)
        paint(Color.rgb(35, 17, 48))
        canvas.drawCircle(cx-d(10f), d(49f), d(3.2f), p)
        canvas.drawCircle(cx+d(10f), d(49f), d(3.2f), p)
        paint(Color.WHITE)
        canvas.drawCircle(cx-d(11f), d(46.5f), d(1.8f), p)
        canvas.drawCircle(cx+d(9f), d(46.5f), d(1.8f), p)

        // Blush and smile
        paint(Color.rgb(255, 139, 177))
        canvas.drawOval(RectF(cx-d(20f), d(56f), cx-d(13f), d(60f)), p)
        canvas.drawOval(RectF(cx+d(13f), d(56f), cx+d(20f), d(60f)), p)
        paint(Color.rgb(133, 46, 80), Paint.Style.STROKE, 1.5f)
        canvas.drawArc(RectF(cx-d(4f), d(55f), cx+d(4f), d(62f)), 15f, 150f, false, p)

        // Crown
        paint(Color.rgb(255, 203, 75))
        val crown = Path().apply {
            moveTo(cx-d(15f), d(23f)); lineTo(cx-d(18f), d(11f))
            lineTo(cx-d(8f), d(17f)); lineTo(cx, d(5f))
            lineTo(cx+d(8f), d(17f)); lineTo(cx+d(18f), d(11f))
            lineTo(cx+d(15f), d(23f)); close()
        }
        canvas.drawPath(crown, p)
        paint(Color.rgb(255, 245, 190))
        canvas.drawCircle(cx, d(12f), d(2f), p)
        canvas.drawCircle(cx-d(12f), d(17f), d(1.5f), p)
        canvas.drawCircle(cx+d(12f), d(17f), d(1.5f), p)

        // Little sparkles
        paint(Color.rgb(255, 150, 225))
        canvas.drawCircle(cx-d(35f), d(42f), d(2f), p)
        canvas.drawCircle(cx+d(35f), d(63f), d(2f), p)
        canvas.restore()
        postInvalidateDelayed(80)
    }
}
