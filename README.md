# Rotato

A minimal Android 14+ app that lives in the notification tray and lets you rotate the screen with a tap.

## What it does

Rotato runs as a foreground service and adds a persistent notification with three buttons:
- **◀ Left** — rotates the screen 90° counter-clockwise
- **Right ▶** — rotates the screen 90° clockwise
- **✕ Close** — stops the service and removes the notification

## Installation

1. Install the APK (enable *Install from unknown sources* if prompted)
2. Tap the **Rotato** icon — the app will guide you through two permission prompts:
   - **Modify system settings** — redirects you to the system screen; find Rotato and toggle it on, then press back
   - **Notifications** — required to show the tray controls; tap Allow
3. The notification appears in your tray and the app is ready to use

> **Note:** Rotation control works on stock/near-stock Android 14+ (e.g. Pixel). Some OEM skins (Samsung One UI, MIUI, etc.) may restrict writing rotation settings, in which case the rotate buttons will have no effect.

## Permissions explained

| Permission | Why it's needed |
|---|---|
| Modify system settings | Writes `USER_ROTATION` to change screen orientation |
| Post notifications | Shows the persistent tray notification with the control buttons |

## Building locally

```bash
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## CI / build numbers

Every push to GitHub triggers a build via GitHub Actions. The APK is uploaded as a workflow artifact named `rotato-debug-<run_number>.apk`. The Android `versionCode` is set to the GitHub Actions run number, so it increments automatically with each push.
