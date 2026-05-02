package com.rotato

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ROTATE_LEFT -> RotationService.rotate(context, 0)
            ACTION_ROTATE_RIGHT -> RotationService.rotate(context, 1)
            ACTION_CLOSE -> context.stopService(Intent(context, RotationService::class.java))
        }
    }

    companion object {
        const val ACTION_ROTATE_LEFT = "com.rotato.ACTION_ROTATE_LEFT"
        const val ACTION_ROTATE_RIGHT = "com.rotato.ACTION_ROTATE_RIGHT"
        const val ACTION_CLOSE = "com.rotato.ACTION_CLOSE"
    }
}
