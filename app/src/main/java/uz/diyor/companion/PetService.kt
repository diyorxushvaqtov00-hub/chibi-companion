package uz.diyor.companion

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast

class PetService : Service() {
    private var windowManager: WindowManager? = null
    private var petView: ChibiView? = null
    private var startX = 0f
    private var startY = 0f
    private var originalX = 0
    private var originalY = 0
    private var moved = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel("chibi", "Chibi Companion", NotificationManager.IMPORTANCE_LOW)
        )
        val open = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(this, "chibi")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Malika yordamchi ishlayapti")
            .setContentText("Ekran ustidagi chibi yordamchi faol")
            .setContentIntent(open)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(10, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(10, notification)
        }
        if (petView == null) showPet()
        return START_STICKY
    }

    private fun showPet() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val pet = ChibiView(this)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 180
        }
        pet.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    originalX = params.x
                    originalY = params.y
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - startX
                    val dy = event.rawY - startY
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) moved = true
                    if (moved) {
                        // Keep the character fully inside the visible screen bounds.
                        val metrics = resources.displayMetrics
                        val maxX = (metrics.widthPixels - pet.width).coerceAtLeast(0)
                        val maxY = (metrics.heightPixels - pet.height).coerceAtLeast(0)
                        params.x = (originalX + dx).toInt().coerceIn(0, maxX)
                        params.y = (originalY + dy).toInt().coerceIn(0, maxY)
                        try { windowManager?.updateViewLayout(pet, params) } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        val chat = Intent(this, ChatActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(chat)
                    }
                    true
                }
                else -> true
            }
        }
        petView = pet
        windowManager?.addView(pet, params)
    }

    override fun onDestroy() {
        try {
            petView?.let { windowManager?.removeView(it) }
        } catch (_: Exception) {}
        petView = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
