# NightBell — Implementation Plan & Technical Architecture

**Document Version:** 1.0.0  
**Target Platform:** Android (Min SDK 26 / Android 8.0, Target SDK 35 / Android 15)  
**Language & Framework:** Kotlin 2.0+, Jetpack Compose, Material 3, AndroidX  
**Reference Documents:** `NightBell — Personal Alarm App Spec.md` and `NightBell — App Prototype.html`  
**Tracker Reference:** `TRACKER.md`

---

## 1. Executive Summary & Problem Space

Freelancers and agency owners in Bangladesh (GMT+6) face a significant time zone mismatch with clients in North America and Europe. Crucial project inquiries, order revisions, and direct messages arrive between 11:00 PM and 8:00 AM local time. Standard Android notification sounds are transient (1–2 seconds) and easily slept through, resulting in:
- Damaged Fiverr response rate metrics.
- Missed first-to-respond advantage on Upwork direct invites.
- Lost project orders and revenue.

**NightBell** intercepts incoming notifications strictly from target apps (`com.fiverr.fiverr` and `com.upwork.android.apps.main`), evaluates rule criteria (time schedule, keywords, message vs. marketing distinction), and triggers an unyielding, high-priority alarm stream playback with full-screen lock screen bypass until dismissed or stopped.

### Key Architectural Pillars
1. **Zero External Dependencies / True Air-Gapped Privacy:** No `android.permission.INTERNET` requested. Zero analytics, zero cloud SDKs, zero API key requirements.
2. **Deterministic Wake-Up Engine:** Utilizes `AudioAttributes.USAGE_ALARM` with `AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE` to bypass Silent and Do Not Disturb (DND) modes.
3. **Passive Auto-Silence:** Listens to `onNotificationRemoved()` on `NotificationListenerService`. Opening the message in Fiverr or Upwork naturally silences the alarm without manual NightBell intervention.
4. **Resilient Background Operation:** Handles aggressive OEM battery managers (MIUI/HyperOS, ColorOS, OneUI, FuntouchOS) through an active Setup Checklist and listener auto-rebind mechanisms.

---

## 2. System Architecture & Component Diagram

```
                        +-----------------------------------------+
                        |         Android OS Notification         |
                        |      (Fiverr / Upwork Message)          |
                        +-----------------------------------------+
                                             |
                                             v
                        +-----------------------------------------+
                        |      NightBellNotificationListener      |
                        |       (NotificationListenerService)     |
                        +-----------------------------------------+
                                             |
                        Extract: Package, Title, Text, Extras, Key
                                             v
                        +-----------------------------------------+
                        |               RuleEngine                |
                        +-----------------------------------------+
                                    /                 \
                                   v                   v
                    +--------------------+    +--------------------+
                    |  ScheduleChecker   |    |  Filter Heuristics |
                    | (Quiet Window/8h)  |    | (Keywords/Category)|
                    +--------------------+    +--------------------+
                                   \                   /
                                    v                 v
                               +---------------------------+
                               |     Evaluation: MATCH?    |
                               +---------------------------+
                                             | Yes
                                             v
                               +---------------------------+
                               |       AlarmService        |
                               |    (Foreground Service)   |
                               +---------------------------+
                                  /            |         \
                                 /             |          \
                                v              v           v
                     +---------------+  +------------+  +------------------+
                     |  AudioPlayer  |  |  Vibrator  |  | AlarmActivity    |
                     | (Alarm Stream |  | Controller |  | (Full Screen On  |
                     |  Volume Ramp) |  | (Rhythmic) |  |  Lock Screen)    |
                     +---------------+  +------------+  +------------------+
                                             |
                     +---------------------------------------------+
                     | Dismissal Triggers:                         |
                     |  1. In-App "Stop" Button                    |
                     |  2. In-App "Snooze 5 min"                   |
                     |  3. In-App "Open Fiverr/Upwork"             |
                     |  4. Notification dismissed remotely / read  |
                     |  5. 2-min ring timeout (repeat in 5 min)    |
                     +---------------------------------------------+
                                             |
                                             v
                     +---------------------------------------------+
                     |             AlarmHistoryDao                 |
                     |       (Room DB: Event + Duration)           |
                     +---------------------------------------------+
```

---

## 3. Package & Project Directory Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/nightbell/app/
│   ├── NightBellApp.kt                         # Application class & DI container
│   ├── core/
│   │   ├── common/
│   │   │   ├── Constants.kt                    # Target packages, default hours, tone IDs
│   │   │   ├── Resource.kt                     # State wrapper
│   │   │   └── Extensions.kt                   # Context, Time, String helpers
│   │   ├── audio/
│   │   │   ├── AlarmAudioPlayer.kt             # MediaPlayer, USAGE_ALARM, volume ramp
│   │   │   └── VibratorController.kt           # Pulsing haptic patterns
│   │   ├── engine/
│   │   │   ├── RuleEngine.kt                   # Evaluates app rules against SBN
│   │   │   ├── ScheduleChecker.kt              # Quiet window & 8-hour override logic
│   │   │   └── NotificationParser.kt           # Extracts sender and message body
│   │   └── database/
│   │       ├── NightBellDatabase.kt            # Room database definition
│   │       ├── dao/
│   │       │   └── AlarmEventDao.kt            # Alarm history queries
│   │       └── entity/
│   │           └── AlarmEventEntity.kt         # Alarm history schema
│   ├── data/
│   │   ├── datastore/
│   │   │   ├── SettingsDataStore.kt            # Protobuf / Preferences DataStore
│   │   │   └── ModelSerializers.kt
│   │   └── repository/
│   │       ├── AlarmHistoryRepository.kt       # Room repository implementation
│   │       ├── RuleRepository.kt               # App rules repository
│   │       └── HealthRepository.kt             # Tracks last seen notification time
│   ├── domain/
│   │   └── model/
│   │       ├── AppRule.kt                      # Rule configuration model
│   │       ├── Schedule.kt                     # Schedule window & quick override
│   │       ├── AlarmSettings.kt                # Audio tone, volume, ramp, vibration
│   │       └── AlarmRecord.kt                  # Domain representation of history
│   ├── service/
│   │   ├── NightBellNotificationListener.kt    # NotificationListenerService implementation
│   │   ├── AlarmService.kt                     # Foreground service managing ring & wake-lock
│   │   ├── BootReceiver.kt                     # ACTION_BOOT_COMPLETED listener rebind
│   │   └── AlarmRepeatReceiver.kt              # AlarmManager wake receiver for 5-min snooze/repeat
│   └── ui/
│       ├── theme/
│       │   ├── Color.kt                        # Theme colors (#0E1220, #FFB020, #171C30, etc.)
│       │   ├── Type.kt                         # Typography (Inter font family)
│       │   └── Theme.kt                        # NightBellTheme Material3 configuration
│       ├── navigation/
│       │   ├── Screen.kt                       # Sealed class navigation routes
│       │   └── AppNavHost.kt                   # BottomBar & Navigation host
│       ├── screens/
│       │   ├── home/
│       │   │   ├── HomeScreen.kt               # Status card, toggles, last alarm, test button
│       │   │   └── HomeViewModel.kt
│       │   ├── rules/
│       │   │   ├── RulesScreen.kt              # App tabs, switches, chips, sound settings
│       │   │   ├── Components.kt               # KeywordChip, TonePickerDialog
│       │   │   └── RulesViewModel.kt
│       │   ├── history/
│       │   │   ├── HistoryScreen.kt            # Filter chips, alarm event list, duration
│       │   │   └── HistoryViewModel.kt
│       │   ├── setup/
│       │   │   ├── SetupChecklistScreen.kt     # Permissions audit, OEM battery fix guide
│       │   │   └── SetupViewModel.kt
│       │   └── alarm/
│       │       ├── AlarmActivity.kt            # Lock screen bypass activity
│       │       ├── AlarmScreen.kt              # Compose UI: Rings, large clock, CTA buttons
│       │       └── AlarmViewModel.kt
│       └── components/
│           ├── NightBellBottomBar.kt           # Custom 3-tab navigation bar
│           └── StatusPulse.kt                  # Concentric ring pulse animation
└── res/
    ├── raw/
    │   ├── rising_siren.ogg                    # Bundled alarm sound 1 (default)
    │   ├── digital_urgency.ogg                 # Bundled alarm sound 2
    │   └── emergency_beacon.ogg                # Bundled alarm sound 3
    ├── drawable/
    └── mipmap/
```

---

## 4. UI Specification & Design Tokens

Derived from the verified prototype markup (`NightBell — App Prototype.html`):

### 4.1 Color Palette
| Token Name | Hex Code | Purpose |
|---|---|---|
| `DarkBackground` | `#0E1220` | Root background of all screens |
| `DarkSurfaceCard` | `#171C30` | Cards, list items, toggles, text containers |
| `DarkSurfaceNav` | `#121729` | Bottom navigation bar background |
| `DarkBorder` | `#232A44` | Card dividers, navigation top border |
| `PrimaryAmber` | `#FFB020` | Accent color, active tabs, buttons, bell ring, switches |
| `AmberMutedBg` | `#2A2410` | Home bell icon circular background |
| `TextPrimary` | `#E8EAF2` | High-contrast headings and primary labels |
| `TextMuted` | `#9AA3BA` | Subtitles, inactive tabs, captions, uppercase headers |
| `TextBody` | `#C9CFE0` | Message preview text in Alarm screen |
| `SuccessMint` | `#3DDC97` | Health check text ("Last seen..."), stop duration ("38 s") |
| `SliderTrack` | `#3A4260` | Inactive volume track |
| `DashedBorder` | `#5A6385` | "Add word" chip border & alarm secondary buttons |
| `PulseRingOuter`| `rgba(255, 176, 32, 0.2)` | 260px pulsing ring in Alarm screen |
| `PulseRingInner`| `rgba(255, 176, 32, 0.4)` | 180px pulsing ring in Alarm screen |

### 4.2 Typography & Spacing
- **Font Family:** Inter (Regular 400, Medium 500, Semi-Bold 600, Bold 700).
- **Corner Radii:** Cards: 20dp; Action Buttons: 16dp–18dp; Segmented Controls: 14dp; Chips: 20dp.
- **Section Headers:** 12px, font-weight 600, letter-spacing 0.08em, uppercase, `#9AA3BA`.

---

## 5. Core Engine & Technical Specifications

### 5.1 Notification Parsing & Filtering Heuristics (`RuleEngine.kt`)
The target packages are:
- Fiverr: `com.fiverr.fiverr`
- Upwork: `com.upwork.android.apps.main`

When a `StatusBarNotification` (SBN) arrives:
```kotlin
fun evaluate(sbn: StatusBarNotification, rule: AppRule, schedule: Schedule): EvaluationResult {
    if (!rule.enabled) return EvaluationResult.Ignored("Rule disabled for app")
    
    // 1. App Package Matching
    if (sbn.packageName != rule.packageName) return EvaluationResult.Ignored("Package mismatch")

    // 2. Schedule Check
    if (rule.useSchedule && !ScheduleChecker.isInsideActiveWindow(schedule, System.currentTimeMillis())) {
        return EvaluationResult.Ignored("Outside active schedule window")
    }

    // 3. Extract Notification Content
    val extras = sbn.notification.extras
    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
    val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
    val fullContent = "$title $text $bigText".lowercase()

    // 4. Messages-Only Filter Heuristics
    if (rule.messagesOnly) {
        val category = sbn.notification.category
        // Ignore system navigation, promo notifications, low priority summaries
        if (category == Notification.CATEGORY_PROMO || category == Notification.CATEGORY_RECOMMENDATION) {
            return EvaluationResult.Ignored("Notification is marketing/promo category")
        }
        // In Fiverr/Upwork, client messages always include a non-empty sender title
        if (title.isBlank()) {
            return EvaluationResult.Ignored("Missing sender title; likely a marketing push")
        }
    }

    // 5. Keyword Blocklist Filter
    for (blocked in rule.blockKeywords) {
        if (blocked.isNotBlank() && fullContent.contains(blocked.lowercase())) {
            return EvaluationResult.Ignored("Blocked by keyword: $blocked")
        }
    }

    // 6. Keyword Allowlist Filter (if specified)
    if (rule.allowKeywords.isNotEmpty()) {
        val matchFound = rule.allowKeywords.any { fullContent.contains(it.lowercase()) }
        if (!matchFound) {
            return EvaluationResult.Ignored("Does not contain required allowlist keywords")
        }
    }

    return EvaluationResult.Trigger(
        sender = title,
        messagePreview = if (bigText.isNotBlank()) bigText else text,
        notificationKey = sbn.key
    )
}
```

### 5.2 Midnight-Spanning Schedule Logic (`ScheduleChecker.kt`)
Quiet hours typically span midnight (e.g., 23:00 to 08:00):
```kotlin
fun isInsideActiveWindow(schedule: Schedule, currentTimeMillis: Long): Boolean {
    // Check quick 8-hour override first
    if (currentTimeMillis < schedule.quickOverrideUntilMillis) {
        return true
    }

    val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat
    val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

    val start = schedule.startMinute // e.g. 23 * 60 = 1380
    val end = schedule.endMinute     // e.g. 8 * 60 = 480

    val isInTimeWindow = if (start <= end) {
        // Daytime interval (e.g. 09:00 to 17:00)
        currentMinutes in start..end
    } else {
        // Spans midnight (e.g. 23:00 to 08:00)
        currentMinutes >= start || currentMinutes <= end
    }

    if (!isInTimeWindow) return false

    // If current time is after midnight (00:00 to end), the window started on the previous day
    val effectiveDay = if (start > end && currentMinutes <= end) {
        if (currentDayOfWeek == Calendar.SUNDAY) Calendar.SATURDAY else currentDayOfWeek - 1
    } else {
        currentDayOfWeek
    }

    return schedule.activeDays.contains(effectiveDay)
}
```

### 5.3 Audio Engine & Volume Ramp (`AlarmAudioPlayer.kt`)
The alarm must play with maximum reliability on the alarm audio stream:
- `AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()`
- Requests transient exclusive audio focus: `AudioManager.requestAudioFocus()`
- Starts volume at 40% if ramp is enabled and escalates to target volume (e.g., 85%) over 20 seconds using a smoothed coroutine ticker.
- Releases audio focus cleanly when `stop()` is called.

### 5.4 Lock Screen Bypass (`AlarmActivity.kt`)
In `AlarmActivity.onCreate()`:
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
    setShowWhenLocked(true)
    setTurnScreenOn(true)
} else {
    @Suppress("DEPRECATION")
    window.addFlags(
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    )
}
window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
```

---

## 6. Phase-by-Phase Implementation Roadmap

The implementation is broken down into 7 distinct, sequential phases designed to minimize risk and ensure testability at every stage.

### Phase 0: Project Setup, Tooling & Theme Foundation
**Objective:** Establish the clean Gradle build, Manifest, theme tokens, and typography matching the prototype.
- Configure `minSdk = 26`, `targetSdk = 35`, `compileSdk = 35`.
- Add dependencies: Jetpack Compose Material3, Navigation, Room, DataStore, Lifecycle ViewModel.
- Strictly exclude `android.permission.INTERNET` from `AndroidManifest.xml`.
- Implement `NightBellTheme` with exact hex colors: `#0E1220`, `#171C30`, `#FFB020`, `#3DDC97`, `#9AA3BA`.
- Verify dark theme styling and font integration (Inter).

### Phase 1: Storage Layer & Data Models
**Objective:** Define persistence for user preferences, rules, schedule, and alarm history.
- Implement DataStore preferences:
  - App rules (`fiverr_enabled`, `upwork_enabled`, `messages_only`, `ignore_words`).
  - Schedule (`start_minute`, `end_minute`, `active_days`, `quick_override_timestamp`).
  - Alarm sound (`tone_uri`, `volume_percent`, `volume_ramp_enabled`, `repeat_count`).
- Implement Room database (`NightBellDatabase`):
  - `AlarmEventEntity` (`id`, `packageName`, `sender`, `preview`, `triggeredAt`, `stoppedAt`, `durationSeconds`, `stopReason`).
  - `AlarmEventDao` with `getAllEvents()`, `getEventsByPackage()`, `insertEvent()`, `updateEvent()`.
- Write unit tests for DataStore reading/writing and Room DAO queries.

### Phase 2: Notification Interceptor & Rule Engine
**Objective:** Capture and evaluate incoming notifications from target apps.
- Implement `NightBellNotificationListener` extending `NotificationListenerService`.
- Implement notification health tracker (`HealthRepository`): updates `lastSeenTimestamp` on every observed notification.
- Implement `RuleEngine` and `NotificationParser`:
  - Extract title, text, category, and subtext.
  - Implement heuristic message-filtering to distinguish client messages from promos/webinars.
  - Case-insensitive substring ignore-word matching.
- Implement `ScheduleChecker` supporting midnight-spanning intervals and day-of-week checks.
- Implement `requestRebind()` on service disconnection and phone boot (`BootReceiver`).
- Provide an in-memory/debug log accessible on the UI for manual validation.

### Phase 3: Alarm Foreground Service, Audio Stream & Haptics
**Objective:** Build the ringing engine on `AudioAttributes.USAGE_ALARM`.
- Implement `AlarmService` as a Foreground Service:
  - Service notification with high priority and direct "Stop" action button.
  - Uses `foregroundServiceType="mediaPlayback"` / `"specialUse"`.
  - Holds partial `WakeLock` to ensure CPU does not sleep during ringing.
- Implement `AlarmAudioPlayer`:
  - Uses `USAGE_ALARM` so physical ringer/notification mute does not silence it.
  - Requests exclusive audio focus.
  - Supports 20-second volume ramp (40% to target volume).
  - Handles looping alarm tones (`rising_siren.ogg`, `digital_urgency.ogg`, or system tones).
- Implement `VibratorController` with repeating heavy pulse pattern `longArrayOf(0, 800, 400, 800, 400)`.
- Implement auto-timeout at 2 minutes: stops audio, schedules 5-minute repeat via `AlarmManager` (up to 5 iterations).
- Connect `onNotificationRemoved` in `NightBellNotificationListener`: when the original message notification is dismissed (because the freelancer opened it in Fiverr/Upwork), send stop intent to `AlarmService`.

### Phase 4: Full-Screen Lock Screen Alarm UI (`AlarmActivity`)
**Objective:** Implement the high-urgency ringing screen matching the visual prototype.
- Configure `AlarmActivity` in `AndroidManifest.xml` with:
  - `android:showWhenLocked="true"`
  - `android:turnScreenOn="true"`
  - `android:launchMode="singleTask"`
  - `android:excludeFromRecents="true"`
- Implement `AlarmScreen` Compose layout:
  - Amber badge: `"NEW FIVERR MESSAGE"` or `"NEW UPWORK MESSAGE"`.
  - Large digital clock: `"2:14 AM"` (88px bold).
  - Sender headline: `"Jordan M."` (24px semi-bold).
  - Message preview card: dark rounded container (`#171C30`).
  - Animated pulsing concentric circles (`#FFB02033` and `#FFB02066`).
  - Three action buttons:
    1. Primary: `"Open Fiverr"` / `"Open Upwork"` (launches the target app directly via `packageManager.getLaunchIntentForPackage()` and stops the alarm).
    2. Secondary Left: `"Snooze 5 min"` (stops audio, schedules `AlarmRepeatReceiver` for +5 minutes).
    3. Secondary Right: `"Stop"` (stops alarm, marks history as `USER_STOPPED`).

### Phase 5: Primary App Screens & Navigation
**Objective:** Build the complete 3-tab application UI shown in the prototype.
- Implement `NightBellBottomBar`: 3 tabs (Home, Rules, History).
- **Home Screen (`HomeScreen.kt`):**
  - Bell status badge: Circular icon with pulsing amber outline, "Listening" / "Paused".
  - Status text: `"Alarm is on until 8:00 AM"`.
  - Health indicator: `"Last notification seen X min ago"` (`#3DDC97`) or warning banner if >24 hours.
  - "WATCHING" section: Fiverr & Upwork cards with toggle switches.
  - "LAST ALARM" summary card: Sender, time, duration (`"Stopped in 38 s"`).
  - Primary `"Test alarm"` button: Triggers a 5-second simulated alarm after a 3-second countdown.
- **Rules Screen (`RulesScreen.kt`):**
  - App selector tabs: Fiverr | Upwork.
  - Toggles: "Enabled", "Messages only", "Only in quiet hours".
  - "IGNORE WORDS" section: Interactive chips (`sale`, `discount`, `tips`, `webinar`) with delete action and dashed `"Add word"` chip opening an input dialog.
  - "ALARM SOUND" card: Tone selector dialog, Volume slider with real-time percentage label, Repeat configuration.
- **History Screen (`HistoryScreen.kt`):**
  - Filter chips: `All`, `Fiverr`, `Upwork`.
  - Chronological list of past alarms with sender, timestamp, app badge, and green duration text (`#3DDC97`).
  - Empty state with clear graphic when no alarms have fired.

### Phase 6: Setup Checklist, Battery Optimization & OEM Hardening
**Objective:** Ensure the app never gets killed in background on aggressive Chinese and Samsung OEM ROMs.
- **Setup Checklist Screen (`SetupChecklistScreen.kt`):**
  - Notification Access (`Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`).
  - Notification Posting Permission (`POST_NOTIFICATIONS` on Android 13+).
  - Full Screen Intent permission (`ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT` on Android 14+).
  - Battery Optimization Exemption (`Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).
  - OEM Autostart & Background instructions for Xiaomi/HyperOS, Oppo/ColorOS, Vivo, Samsung.
- Add listener health check: If the OS silently unbinds `NotificationListenerService`, detect it in `MainActivity` and prompt re-bind.

---

## 7. Verification & Acceptance Testing Protocol

| Test # | Condition | Action | Expected Result |
|---|---|---|---|
| **TC-01** | Phone on Silent, DND enabled, screen locked | Push simulated Fiverr message notification | Screen wakes, full-screen AlarmActivity launches, alarm rings loudly on alarm audio stream within 3 seconds. |
| **TC-02** | Screen locked, alarm ringing | Freelancer taps "Open Fiverr" button | Alarm immediately stops, lock screen unlocks (or prompts biometric), Fiverr app opens directly. |
| **TC-03** | Alarm ringing on phone | Message read on web browser (notification dismissed) | `onNotificationRemoved()` fires, alarm immediately silences without phone being touched. |
| **TC-04** | Marketing push ("50% off gigs this week") | Notification arrives from Fiverr | RuleEngine detects promotional text, alarm does NOT ring, notification remains silent. |
| **TC-05** | Daytime (e.g., 2:00 PM outside quiet hours) | Legitimate client message arrives | Ignored by schedule checker, phone behaves normally without triggering alarm. |
| **TC-06** | "Ring now for next 8 hours" override activated | Message arrives at 2:00 PM | Override takes precedence over quiet window, alarm rings immediately. |
| **TC-07** | Phone rebooted | Device restarts, screen remains locked | `BootReceiver` runs, `NotificationListenerService` rebinds, incoming message rings alarm properly. |
| **TC-08** | Airplane mode enabled | User presses "Test alarm" on Home screen | Alarm rings reliably without network connectivity. |
