# NightBell 🔔

> **Personal Night Alarm for Fiverr & Upwork Freelancers**  
> Never sleep through urgent client messages, project inquiries, or order revisions during night hours.

---

## 💡 The Problem

Freelancers and agency owners in Bangladesh (GMT+6) and Asian time zones experience an 8–11 hour shift from clients in the US, Canada, and Europe. High-value client inquiries, contract invitations, and urgent order revisions arrive between 11:00 PM and 8:00 AM local time.

Standard Android notifications play a 1–2 second subtle chime that is easy to sleep through. A slow reply degrades your Fiverr response rate score and loses the project to another freelancer.

**NightBell solves this with a single focused purpose:**  
When a real client message arrives on Fiverr or Upwork, ring like an alarm until you wake up.

---

## ✨ Features

- **Loud Alarm Stream Playback**: Uses Android's `AudioAttributes.USAGE_ALARM` with exclusive transient audio focus. Bypasses physical ringer silent mode and Do Not Disturb (DND).
- **Full-Screen Lock Screen Bypass**: Wakes your phone with an animated pulsing amber alert, large digital clock, sender name, message preview, and instant "Open Fiverr" / "Open Upwork" actions.
- **Passive Auto-Silence**: Dismissing or reading the message on your phone (or on a desktop web browser) automatically cancels and silences the alarm via `onNotificationRemoved`.
- **Intelligent Heuristic Filtering**: Distinguishes between direct client messages and marketing push promotions (e.g., promos, webinars, tips).
- **Keyword Blocklist & Allowlist**: Easily manage ignore-words (`sale`, `discount`, `tips`, `webinar`) directly in the app without recompiling.
- **Quiet Window Schedule**: Configurable active hours (default: 11:00 PM to 8:00 AM) with day-of-week picker, plus a quick **"Ring now for next 8 hours"** override.
- **100% Offline & Private**: Zero cloud servers, zero accounts, zero analytics, and **no `android.permission.INTERNET` requested in the manifest**.
- **OEM Battery Hardening**: Built-in Setup Checklist with specific instructions to prevent task killing on Xiaomi/HyperOS, Samsung One UI, Oppo, and Vivo devices.

---

## 📱 Screenshots

The interface was designed from the ground up using Jetpack Compose and Material 3:

- **Home Dashboard**: Status bell ("Listening" / "Paused"), schedule status, Fiverr/Upwork toggles, last alert duration, and "Test alarm" trigger.
- **Rules & Sound**: Per-app toggles, interactive ignore-word chip group, alarm sound selector, volume slider with real-time feedback, and repeat configurations.
- **Alarm History**: Complete record of past alarm events with exact stop durations in mint green (`"Stopped in 38 s"`).
- **Setup Checklist**: Real-time permission verification with one-tap system settings shortcuts.

---

## 🛠️ Architecture & Tech Stack

```
NightBellNotificationListener (NotificationListenerService)
                      │
                      ▼
         RuleEngine & ScheduleChecker
         (Keywords, Quiet Hours, 8h Override)
                      │ (Match)
                      ▼
          AlarmService (Foreground Service)
           ├── AlarmAudioPlayer (USAGE_ALARM + 20s Volume Ramp)
           ├── VibratorController (Heavy Rhythmic Pattern)
           └── AlarmActivity (Lock Screen Activity Overlay)
                      │
                      ▼
      Passive Dismissal / In-App Action
                      │
                      ▼
        NightBellDatabase (SQLite Event Log)
```

- **Platform**: Android (Min SDK 26 / Android 8.0, Target SDK 35 / Android 15)
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material 3 Dark Palette (`#0E1220`, `#171C30`, `#FFB020`)
- **Persistence**: Jetpack DataStore Preferences & SQLite Database
- **Audio**: Android `MediaPlayer` on `AudioAttributes.USAGE_ALARM` with 20-second smoothed volume ramping

---

## 🚀 Building & Installing

### Prerequisites
- JDK 21+ (e.g., Android Studio bundled JetBrains Runtime)
- Android SDK Platforms 35

### Clone the Repository
```bash
git clone https://github.com/mrsnailo/night-bell.git
cd night-bell
```

### Build Debug APK
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Install Directly on Connected Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Run Unit & Acceptance Tests
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest
```

---

## 📋 Documentation

- [`NightBell — Personal Alarm App Spec.md`](NightBell%20—%20Personal%20Alarm%20App%20Spec.md): The original product specification.
- [`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md): Complete architecture document and data models.
- [`TRACKER.md`](TRACKER.md): Phase-by-phase implementation tracker with all 44 verified milestone tasks.

---

## 📄 License

Personal use. Open-sourced under the [MIT License](LICENSE).
