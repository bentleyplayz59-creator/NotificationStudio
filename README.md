# Notification Studio

An Android notification creator prototype. The first build tests installed launcher-app discovery, icon selection, and clearly labeled simulated notifications.

Notifications created by this app remain attributed to Notification Studio; selected apps are used for icon artwork and optional launch actions, not impersonation.

## Build

GitHub Actions builds the debug APK. The workflow generates the launcher icon with Python Pillow before running Gradle.
