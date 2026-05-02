package com.rotato

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.util.Log
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
            val intent = Intent(action).setPackage(packageName)
            return PendingIntent.getBroadcast(
                this, action.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Rotato")
            .setContentText("Screen rotation control active")
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(0, "◀ Left", pendingBroadcast(ActionReceiver.ACTION_ROTATE_LEFT))
            .addAction(0, "Right ▶", pendingBroadcast(ActionReceiver.ACTION_ROTATE_RIGHT))
            .addAction(0, "✕ Close", pendingBroadcast(ActionReceiver.ACTION_CLOSE))
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
