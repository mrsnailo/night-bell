# NightBell — Implementation Tracker

**Project:** NightBell (Personal Night Alarm for Fiverr & Upwork)  
**Target:** Android (Kotlin, Jetpack Compose, Material 3, Min SDK 26, Target SDK 35)  
**Spec Document:** `NightBell — Personal Alarm App Spec.md`  
**Architecture & Implementation Plan:** `IMPLEMENTATION_PLAN.md`  
**Prototype Reference:** `NightBell — App Prototype.html`  
**Last Updated:** October 2026

---

## 1. Master Progress Dashboard

| Phase | Description | Tasks | Completed | Status |
|---|---|:---:|:---:|:---:|
| **Phase 0** | Project Setup, Tooling & Theme Foundation | 5 | 5 | `[x] Completed` |
| **Phase 1** | Storage Layer & Data Models | 5 | 0 | `[-] In Progress` |
| **Phase 2** | Notification Interceptor & Rule Engine | 6 | 0 | `[ ] Pending` |
| **Phase 3** | Alarm Foreground Service, Audio & Haptics | 6 | 0 | `[ ] Pending` |
| **Phase 4** | Lock Screen Alarm Activity & Visuals | 5 | 0 | `[ ] Pending` |
| **Phase 5** | Primary App Screens (Home, Rules, History) | 6 | 0 | `[ ] Pending` |
| **Phase 6** | Setup Checklist, Battery & OEM Hardening | 5 | 0 | `[ ] Pending` |
| **Phase 7** | End-to-End Acceptance Testing & Device Run | 6 | 0 | `[ ] Pending` |
| **Total** | **All Phases** | **44** | **0** | **0%** |

---

## 2. Granular Task Breakdown

### Phase 0: Project Setup, Tooling & Theme Foundation
- [x] **TASK-001**: Initialize Android project with Gradle Kotlin DSL (`build.gradle.kts`), configuring `minSdk = 26`, `targetSdk = 35`, `compileSdk = 35`.
  - *Files:* `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`
- [x] **TASK-002**: Exclude internet permissions completely from `AndroidManifest.xml` to ensure zero network capability.
  - *Files:* `app/src/main/AndroidManifest.xml`
- [x] **TASK-003**: Configure dependencies: Compose Material 3, Navigation Compose, Room KSP, DataStore Preferences, Coroutines.
  - *Files:* `app/build.gradle.kts`
- [x] **TASK-004**: Implement design system color palette from prototype (`#0E1220`, `#171C30`, `#FFB020`, `#3DDC97`, `#9AA3BA`, `#2A2410`).
  - *Files:* `app/src/main/java/com/nightbell/app/ui/theme/Color.kt`, `Theme.kt`
- [x] **TASK-005**: Configure Inter typography scales and Material 3 theme wrapper (`NightBellTheme`).
  - *Files:* `app/src/main/java/com/nightbell/app/ui/theme/Type.kt`

---

### Phase 1: Storage Layer & Data Models
- [ ] **TASK-101**: Define domain models: `AppRule`, `Schedule`, `AlarmSettings`, `AlarmRecord`.
  - *Files:* `app/src/main/java/com/nightbell/app/domain/model/*.kt`
- [ ] **TASK-102**: Implement `SettingsDataStore` with serializers for App Rules (Fiverr/Upwork enabled, message-only, keyword chips).
  - *Files:* `app/src/main/java/com/nightbell/app/data/datastore/SettingsDataStore.kt`
- [ ] **TASK-103**: Implement `Schedule` persistence in DataStore (start/end minutes, days, 8-hour override timestamp).
  - *Files:* `app/src/main/java/com/nightbell/app/data/datastore/SettingsDataStore.kt`
- [ ] **TASK-104**: Implement Room Database (`NightBellDatabase`) with `AlarmEventEntity` and `AlarmEventDao`.
  - *Files:* `app/src/main/java/com/nightbell/app/core/database/entity/AlarmEventEntity.kt`, `dao/AlarmEventDao.kt`
- [ ] **TASK-105**: Implement `AlarmHistoryRepository` and unit tests for database insertion, query filtering, and duration tracking.
  - *Files:* `app/src/main/java/com/nightbell/app/data/repository/AlarmHistoryRepository.kt`

---

### Phase 2: Notification Interceptor & Rule Engine
- [ ] **TASK-201**: Implement `NightBellNotificationListener` extending `NotificationListenerService`.
  - *Files:* `app/src/main/java/com/nightbell/app/service/NightBellNotificationListener.kt`
- [ ] **TASK-202**: Implement `HealthRepository` to record `lastSeenNotificationTimestamp` for any notification passing through listener.
  - *Files:* `app/src/main/java/com/nightbell/app/data/repository/HealthRepository.kt`
- [ ] **TASK-203**: Implement `RuleEngine` to match target packages (`com.fiverr.fiverr`, `com.upwork.android.apps.main`), parse extras (`EXTRA_TITLE`, `EXTRA_TEXT`, `EXTRA_BIG_TEXT`), and evaluate block/allow keywords.
  - *Files:* `app/src/main/java/com/nightbell/app/core/engine/RuleEngine.kt`
- [ ] **TASK-204**: Implement `ScheduleChecker` to calculate active status including midnight-spanning intervals (e.g. 11:00 PM to 8:00 AM) and active days.
  - *Files:* `app/src/main/java/com/nightbell/app/core/engine/ScheduleChecker.kt`
- [ ] **TASK-205**: Wire listener auto-rebind via `requestRebind(ComponentName)` on disconnect and on boot completion.
  - *Files:* `app/src/main/java/com/nightbell/app/service/BootReceiver.kt`
- [ ] **TASK-206**: Implement remote dismiss detection in `onNotificationRemoved`: cancel active alarm when notification is cleared by original app.
  - *Files:* `app/src/main/java/com/nightbell/app/service/NightBellNotificationListener.kt`

---

### Phase 3: Alarm Foreground Service, Audio & Haptics
- [ ] **TASK-301**: Implement `AlarmService` as a Foreground Service with sticky notification, `WakeLock`, and direct notification "Stop" action.
  - *Files:* `app/src/main/java/com/nightbell/app/service/AlarmService.kt`
- [ ] **TASK-302**: Bundle raw alarm sound assets (`rising_siren.ogg`, `digital_urgency.ogg`, `emergency_beacon.ogg`).
  - *Files:* `app/src/main/res/raw/*.ogg`
- [ ] **TASK-303**: Implement `AlarmAudioPlayer` configured with `AudioAttributes.USAGE_ALARM` and `AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE`.
  - *Files:* `app/src/main/java/com/nightbell/app/core/audio/AlarmAudioPlayer.kt`
- [ ] **TASK-304**: Implement audio volume ramp (starts at 40%, reaches 100% of user setting over 20 seconds).
  - *Files:* `app/src/main/java/com/nightbell/app/core/audio/AlarmAudioPlayer.kt`
- [ ] **TASK-305**: Implement `VibratorController` with repeating heavy pulse pattern (`longArrayOf(0, 800, 400, 800, 400)`).
  - *Files:* `app/src/main/java/com/nightbell/app/core/audio/VibratorController.kt`
- [ ] **TASK-306**: Implement 2-minute ring auto-timeout and 5-minute repeat scheduler using `AlarmManager` (up to 5 repeats).
  - *Files:* `app/src/main/java/com/nightbell/app/service/AlarmRepeatReceiver.kt`

---

### Phase 4: Lock Screen Alarm Activity & Visuals
- [ ] **TASK-401**: Create `AlarmActivity` with window flags `setShowWhenLocked(true)`, `setTurnScreenOn(true)`, `FLAG_KEEP_SCREEN_ON`.
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/alarm/AlarmActivity.kt`
- [ ] **TASK-402**: Implement `AlarmScreen` Compose UI: pulsing concentric amber rings, app badge (`NEW FIVERR MESSAGE`), large digital time (88px).
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/alarm/AlarmScreen.kt`
- [ ] **TASK-403**: Display client sender name and message preview card inside `#171C30` container.
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/alarm/AlarmScreen.kt`
- [ ] **TASK-404**: Implement primary "Open Fiverr" / "Open Upwork" action button: stops alarm and dispatches app launch intent.
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/alarm/AlarmScreen.kt`
- [ ] **TASK-405**: Implement secondary "Snooze 5 min" and "Stop" action buttons; record stop duration in `AlarmEventDao`.
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/alarm/AlarmScreen.kt`

---

### Phase 5: Primary App Screens (Home, Rules, History)
- [ ] **TASK-501**: Build `NightBellBottomBar` navigation component (Home, Rules, History) with active indicator `#FFB020`.
  - *Files:* `app/src/main/java/com/nightbell/app/ui/components/NightBellBottomBar.kt`
- [ ] **TASK-502**: Build `HomeScreen`:
  - Big status card with 96x96 bell badge, "Listening" / "Paused".
  - Schedule status caption ("Alarm is on until 8:00 AM").
  - Health status indicator ("Last notification seen 12 min ago" `#3DDC97`).
  - "WATCHING" section with Fiverr and Upwork toggles.
  - "LAST ALARM" card with sender, time, and stop duration.
  - Full-width "Test alarm" button with countdown trigger.
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/home/HomeScreen.kt`, `HomeViewModel.kt`
- [ ] **TASK-503**: Build `RulesScreen` header and app selector tabs (Fiverr | Upwork).
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/rules/RulesScreen.kt`
- [ ] **TASK-504**: Build Rules toggle card ("Enabled", "Messages only", "Only in quiet hours").
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/rules/RulesScreen.kt`
- [ ] **TASK-505**: Build "IGNORE WORDS" interactive chip group with delete action and dashed "Add word" dialog.
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/rules/RulesScreen.kt`, `Components.kt`
- [ ] **TASK-506**: Build "ALARM SOUND" settings card (tone picker, 85% volume slider, repeat configuration) and `HistoryScreen` with filter chips (All, Fiverr, Upwork).
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/history/HistoryScreen.kt`, `HistoryViewModel.kt`

---

### Phase 6: Setup Checklist, Battery & OEM Hardening
- [ ] **TASK-601**: Build `SetupChecklistScreen` displaying real-time permission status with direct system intent buttons.
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/setup/SetupChecklistScreen.kt`
- [ ] **TASK-602**: Implement Notification Access checker & launcher (`Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`).
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/setup/SetupViewModel.kt`
- [ ] **TASK-603**: Implement Battery Optimization prompt (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) for NightBell, Fiverr, and Upwork.
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/setup/SetupViewModel.kt`
- [ ] **TASK-604**: Implement Android 13+ `POST_NOTIFICATIONS` and Android 14+ `USE_FULL_SCREEN_INTENT` permission handlers.
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/setup/SetupViewModel.kt`
- [ ] **TASK-605**: Add OEM-specific guide cards for Xiaomi/HyperOS, Oppo/ColorOS, Vivo, Samsung (Autostart, lock in recents, sleep mode exceptions).
  - *Files:* `app/src/main/java/com/nightbell/app/ui/screens/setup/SetupChecklistScreen.kt`

---

### Phase 7: End-to-End Acceptance Testing & Device Run
- [ ] **TASK-701**: Test Acceptance Case 1: Phone on silent & DND, screen locked -> Fiverr message triggers alarm within 3 seconds.
- [ ] **TASK-702**: Test Acceptance Case 2: Promotional/marketing notification from Fiverr/Upwork is ignored.
- [ ] **TASK-703**: Test Acceptance Case 3: Outside quiet hours window -> no alarm triggered.
- [ ] **TASK-704**: Test Acceptance Case 4: Dismissing notification in Fiverr app terminates alarm immediately.
- [ ] **TASK-705**: Test Acceptance Case 5: Ignored alarm times out at 2 minutes and repeats after 5 minutes.
- [ ] **TASK-706**: Test Acceptance Case 6: Device reboot verification (listener survives and functions without opening app).

---

## 3. Risk Log & Edge Case Mitigations

| Risk # | Description | Severity | Mitigation Strategy | Status |
|---|---|:---:|---|:---:|
| **R-01** | OEM OS (Xiaomi/Oppo/Samsung) kills `NotificationListenerService` in deep sleep. | High | Implement `requestRebind()`, add Unrestricted Battery intent in Setup checklist, guide user to lock NightBell in Recent Apps. | Planned |
| **R-02** | Fiverr or Upwork changes notification extra keys in a future app update. | Medium | Parser searches multiple standard keys (`EXTRA_TITLE`, `EXTRA_TEXT`, `EXTRA_BIG_TEXT`, `EXTRA_SUMMARY_TEXT`) and falls back to scanning full text span. | Planned |
| **R-03** | Freelancer opens message on PC web browser while phone is locked and ringing. | Medium | `onNotificationRemoved` catches remote notification retraction across sync and silences alarm automatically. | Planned |
| **R-04** | Android 14 full-screen intent restriction blocks lock screen overlay. | Medium | Add Android 14 `USE_FULL_SCREEN_INTENT` permission check in Setup Checklist and fallback to high-priority heads-up notification with full sound. | Planned |
| **R-05** | Marketing notifications disguised with misleading titles. | Low | User-configurable keyword blocklist allows immediate blocking without needing an app update or recompile. | Planned |

---

## 4. Work Log & Session Notes

- **2026-10-24**: Initialized `IMPLEMENTATION_PLAN.md` and `TRACKER.md` based on `NightBell — Personal Alarm App Spec.md` and interactive prototype `NightBell — App Prototype.html`. Extracted exact UI tokens, component hierarchies, and architectural modules.
