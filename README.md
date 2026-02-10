# Wan2GP Android Remote (Starter)

This repository now contains a simple Android app project (Jetpack Compose) that can be used as a remote control UI for Wan2GP over LAN.

## What it includes

- A text field to enter the main PC LAN IP/port (example: `192.168.1.25:7860`).
- Save-on-device persistence for that IP (kept until changed).
- Model picker with dedicated option panels for:
  - **LTX 2**
  - **Flux Klein 9b**
  - **Ace Step 1.5**
- Option values are persisted locally using DataStore.

## Notes

- This is a starter UI/control surface so you can mirror Wan2GP options on Android.
- The "Test connection payload" button currently only confirms target URL in-app; connect your actual HTTP request/JSON payload to your Wan2GP endpoint in `MainActivity.kt`.

## Build

Open in Android Studio (Hedgehog+ / Iguana+) and run the `app` configuration.
