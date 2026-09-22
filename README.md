# Forma (Forma) — Native Android

> **Structured daily timeline meets mindful habit tracking. Pure Japanese Paper & Tea aesthetic.**

Forma is a modern, responsive native Android application built with Kotlin, Jetpack Compose, Material 3, Room, and Hilt. It combines structured, time-blocked daily intention scheduling with recurring rituals, streaks, focus soundscapes, and mindfulness reflections.

---

## ✨ Features & Capabilities

- **🍵 Zen Paper & Tea Design System**:
  - Warm oat canvas (`#F7F8F4`), matcha green (`#4E6542`), slate, and terracotta accents.
  - Zero OLED neon spectrum; soothing paper-like textures and mindful typography.
- **🎨 4K Zen Ensō Adaptive App Icon**:
  - Android adaptive safe-zone compliant vector icon with concentric jade plateau discs and organic calligraphy arcs.
- **⚡ Ultra-Responsive & Sub-Millisecond Engine**:
  - $O(1)$ indexed hash lookup timeline computations ($<1\text{ms}$).
  - Early-halting streak calculations and zero-allocation `.entries` loops.
  - Snappy 180ms screen transition curves.
- **✍️ Premium Add Habit & Intention Composer**:
  - Live interactive focus glow animations (`animateColorAsState` & `animateDpAsState`).
  - Animated 1-tap clear button and real-time mindful character counter.
  - Inline keyboard presentation without modal takeovers.
  - Habit lifecycle horizon with 1-tap **"Ongoing / Forever (∞)"** switch.
- **⏱️ Organic Focus Pomodoro**:
  - Circular zen progress ring with `-5m` and `+5m` adjustment pills.
  - Built-in soothing soundscapes (*Rain on Tatami*, *Silent Flow*, *Zen Drone*, *Stream*, *Brown Flow*, *White Air*).
- **🧘 Daily Alignment & Reflection**:
  - Morning alignment and evening gratitude reflection sheets.
  - 4-7-8 and box breathing exercise visualizer.
  - Monthly Zen Summary reports and full JSON / Markdown journal export.
- **📱 Android System Integrations**:
  - Home screen Zen Ensō Ring & Ritual List widgets (`AppWidgetProvider`).
  - Quick Settings Zen Focus and Zen Breathe tiles (`TileService`).
  - Android 13+ App Shortcuts.

---

## 🏗️ Architecture

Forma strictly adheres to **Clean Architecture** with unidirectional data flow (UDF):

```
┌─────────────────────────────────────────────────────────────┐
│                          UI Layer                           │
│  - Jetpack Compose + Navigation Compose                     │
│  - Emil Kowalski fluid spring physics (formaPressEffect)    │
│  - ViewModels (Today, Habits, Timeline, Stats, Pomodoro)    │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                        Domain Layer                         │
│  - Models (Habit, HabitCompletion, TimelineItem, Reflection)│
│  - Repositories (Habit, Timeline, DailyReflection, Billing) │
│  - UseCases (GetTodayTimeline, CalculateStreak, Stats)      │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                         Data Layer                          │
│  - Room Database v4 (Indexed Habit & Timeline Entities)     │
│  - DataStore Preferences & WorkManager                      │
│  - Foreground AmbientSoundService & Notification Channels   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Building & Running

```bash
# Clone the repository
git clone https://github.com/MITTNAYT/forma.git

# Navigate to directory
cd "Forma"

# Compile and assemble debug APK
./gradlew assembleDebug

# Install on connected device via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
