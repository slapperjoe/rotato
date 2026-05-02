package com.rotato

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // Set to true while we are waiting for the user to return from the
    // WRITE_SETTINGS system-settings screen.
    private var awaitingWriteSettings = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                this,
                "Notification permission denied — tray controls may not appear",
                Toast.LENGTH_LONG
            ).show()
        }
        startServiceAndFinish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        // Returning from the WRITE_SETTINGS system screen — proceed to next step.
        if (awaitingWriteSettings) {
            awaitingWriteSettings = false
            if (!Settings.System.canWrite(this)) {
                Toast.makeText(
                    this,
                    "\"Modify system settings\" not granted — rotation buttons will be inactive",
                    Toast.LENGTH_LONG
                ).show()
            }
            checkNotificationPermission()
        }
    }

    private fun checkPermissions() {
        // WRITE_SETTINGS cannot use requestPermissions() — must open system settings.
        if (!Settings.System.canWrite(this)) {
            awaitingWriteSettings = true
            Toast.makeText(
                this,
                "Please allow \"Modify system settings\" so Rotato can rotate your screen",
                Toast.LENGTH_LONG
            ).show()
            startActivity(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
            )
            return // wait for onResume to continue
        }
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return // wait for launcher callback to continue
        }
        startServiceAndFinish()
    }

    private fun startServiceAndFinish() {
        if (RotationService.isRunning) {
            Toast.makeText(this, "Rotato is already running", Toast.LENGTH_SHORT).show()
        } else {
            startForegroundService(Intent(this, RotationService::class.java))
        }
        finish()
    }
}
