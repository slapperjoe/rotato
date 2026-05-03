package com.rotato

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class RotationService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rotato",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setSound(null, null)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        fun pendingBroadcast(action: String): PendingIntent {
            // Android 14+ requires an explicit component when targeting
            // an unexported receiver — setPackage() alone is not sufficient.
            val intent = Intent(action, null, this, ActionReceiver::class.java)
            return PendingIntent.getBroadcast(
                this, action.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Custom layout so the buttons are visible in the collapsed notification —
        // addAction() buttons only appear when the notification is expanded.
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val textColor = if (isDark) Color.WHITE else Color.BLACK
        val btnBg = if (isDark) R.drawable.btn_notification_dark else R.drawable.btn_notification

        val views = RemoteViews(packageName, R.layout.notification_controls).apply {
            setOnClickPendingIntent(R.id.btn_rotate_left,  pendingBroadcast(ActionReceiver.ACTION_ROTATE_LEFT))
            setOnClickPendingIntent(R.id.btn_close,        pendingBroadcast(ActionReceiver.ACTION_CLOSE))
            setOnClickPendingIntent(R.id.btn_rotate_right, pendingBroadcast(ActionReceiver.ACTION_ROTATE_RIGHT))
            for (id in listOf(R.id.btn_rotate_left, R.id.btn_close, R.id.btn_rotate_right)) {
                setTextColor(id, textColor)
                setInt(id, "setBackgroundResource", btnBg)
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val TAG = "RotationService"
        private const val CHANNEL_ID = "rotato_channel"
        private const val NOTIFICATION_ID = 1

        @Volatile var isRunning = false

        fun rotate(context: Context, direction: Int) {
            val cr = context.contentResolver
            try {
                val current = Settings.System.getInt(cr, Settings.System.USER_ROTATION, 0)
                val next = if (direction == 0) (current + 3) % 4 else (current + 1) % 4
                Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0)
                Settings.System.putInt(cr, Settings.System.USER_ROTATION, next)
            } catch (e: SecurityException) {
                Log.w(TAG, "WRITE_SETTINGS not granted — cannot rotate: ${e.message}")
            }
        }
    }
}
