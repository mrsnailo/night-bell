# NightBell — Personal Alarm App Spec

Version 0.1 · Personal use only · Android

## 1. Problem

Clients on Fiverr and Upwork message at times when it is night in Bangladesh. The normal app notification is short and quiet, so it is easy to sleep through. A slow reply hurts response rate and can lose the order. The goal is one thing: **when a real client message arrives, ring like an alarm until I wake up.**

## 2. Goals and non-goals

**Goals**

- Ring a loud alarm when a Fiverr or Upwork message notification arrives.
- Work with silent mode and Do Not Disturb.
- Ring only during my chosen sleeping hours (optional), so daytime stays normal.
- Stop by itself when I open the message in the original app.
- Run fully offline on the phone. No account, no server, no login to Fiverr or Upwork.

**Non-goals (v1)**

- No Play Store release. I install the APK myself.
- No reading or replying to messages inside the app.
- No scraping, no API calls, no use of my Fiverr or Upwork credentials.
- No iOS.

## 3. How it works

1. The app uses Android's **NotificationListenerService**. After I grant Notification Access, it sees every notification that is posted.
2. It ignores everything except the apps I enable (Fiverr, Upwork).
3. Each matching notification goes through my **rules** (see section 5). If it passes, the app starts an **alarm**.
4. The alarm plays on the **alarm audio stream** at a fixed volume, so the ringer or notification volume does not matter.
5. When the original notification is dismissed (because I opened the app and read the message), the alarm stops.

Package names to confirm on my phone before coding: Fiverr `com.fiverr.fiverr`, Upwork `com.upwork.android.apps.main`.

## 4. Alarm behaviour

- Sound: chosen from a few bundled loud tones, or any sound from the phone.
- Volume: set in the app, with an optional **ramp** (starts at 40%, reaches full in 20 seconds).
- Vibration: on or off.
- Full-screen alarm screen over the lock screen, with three buttons: **Stop**, **Snooze 5 min**, **Open app**.
- Max ring time: 2 minutes per ring, then it pauses.
- **Repeat until read:** if the notification is still unread after the pause, ring again every 5 minutes, up to 5 times.
- Alarm uses a foreground service so Android does not kill it while ringing.

## 5. Rules

Each app (Fiverr, Upwork) has its own rule set:

| Rule | Default |
| --- | --- |
| Enabled | On |
| Only ring for messages | On (ignore offers, promos, tips) |
| Keyword allow-list | empty (any message) |
| Keyword block-list | "sale", "discount", "tips", "webinar" |
| Ring only during schedule | On |

Message detection: the notification's title is usually the sender and the text is the message. Marketing notifications have different titles and categories, so the block-list and a "sender present" check remove most of them. Defaults need testing against real notifications during week one.

## 6. Schedule

- Quiet window, default 11:00 PM to 8:00 AM, with a day picker.
- Outside the window, the app does nothing and the normal notification stays as is.
- A "Ring now for the next 8 hours" quick switch for when I am expecting an urgent client.

## 7. Screens

1. **Home** — big status ("Listening" or "Paused"), what happens at the next ring, last alert, a Test alarm button.
2. **Apps and rules** — Fiverr and Upwork cards with toggles and keyword chips.
3. **Schedule and sound** — quiet window, tone picker, volume, ramp, repeat settings.
4. **History** — list of alarms with app, sender, time, and how long until I stopped it.
5. **Setup checklist** — every permission with a green tick or a red fix button.
6. **Alarm screen** — full-screen ringing view.

## 8. Permissions and setup checklist

- Notification Access (required).
- Post notifications (Android 13+).
- Full-screen intent (Android 14+; grant manually in app settings).
- Battery: set to **Unrestricted** for NightBell, Fiverr, and Upwork. Fiverr's own help page says delayed notifications are often caused by battery optimization.
- Autostart / background permission on Xiaomi, Oppo, Vivo, Samsung style phones.
- Do Not Disturb access (optional; alarm audio already passes by default).

The home screen shows a **health check**: time of the last notification seen. If no notification has been seen in 24 hours, show a warning.

## 9. Technical design

- Language: Kotlin. UI: Jetpack Compose with Material 3. Min SDK 26.
- Storage: DataStore for settings, Room for alarm history (stays on the phone).
- Components: `NotifListenerService`, `AlarmService` (foreground), `AlarmActivity` (full-screen), `RuleEngine`, `ScheduleChecker`.
- Listener rebind: call `requestRebind` on boot and when the service disconnects.
- Audio: `AudioAttributes.USAGE_ALARM`, `MediaPlayer` or `ExoPlayer`, audio focus requested, volume set on the alarm stream.
- Stop logic: `onNotificationRemoved` for the same key cancels the alarm and the repeat timer.
- No internet permission at all, which keeps it private and simple.

## 10. Data model

- `AppRule(package, enabled, messagesOnly, allowKeywords, blockKeywords, useSchedule)`
- `Schedule(startMinute, endMinute, days, quickOverrideUntil)`
- `AlarmSettings(toneUri, volume, ramp, vibrate, maxRingSec, repeatEveryMin, repeatCount)`
- `AlarmEvent(id, package, sender, preview, startedAt, stoppedAt, stoppedBy)`

## 11. Acceptance tests

1. Phone on silent and Do Not Disturb, screen off: a Fiverr message rings the alarm within 5 seconds of the notification.
2. A Fiverr promo notification does not ring.
3. Outside the quiet window, nothing rings.
4. Opening the message in Fiverr stops the alarm.
5. Ignoring the alarm causes a repeat after 5 minutes.
6. After a phone reboot, the listener works again without opening NightBell.
7. Airplane mode and no internet: Test alarm still works.

## 12. Milestones

- **Day 1:** listener, rule engine, log notifications to screen, confirm package names and message format.
- **Day 2:** alarm service, full-screen alarm, stop on dismiss.
- **Day 3:** schedule, repeat, settings, setup checklist.
- **Day 4:** history, UI polish, one week of real-life testing and rule tuning.

## 13. Risks

- If Fiverr or Upwork does not deliver the push notification (phone offline, battery killing the app), nothing can ring. The setup checklist and the health check reduce this but cannot remove it.
- Phone makers kill background apps aggressively. Test on my own phone and follow its battery settings.
- Notification text format can change after an app update. Rules must be editable without rebuilding.
- Backup plan: keep Fiverr and Upwork email notifications on, and use Android's own Override Do Not Disturb for each app channel.
