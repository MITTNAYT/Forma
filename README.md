# HabitFlow (Android)

> **Structured's clean timeline, but habits are never paywalled.**

HabitFlow is a native Android application written in Kotlin using Jetpack Compose, Material 3, Room, and Hilt. It combines a visual, time-blocked daily timeline (in the style of the Structured app) with habit-tracking features: free recurring habits, streaks, and a 5-week grayscale completion heatmap.

---

## Architecture Overview

HabitFlow adheres to clean **MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)**:

```
┌─────────────────────────────────────────────────────────────┐
│                          UI Layer                           │
│  - Jetpack Compose + Navigation Compose                     │
│  - Notion-inspired Minimalist Monochrome Design System      │
│  - ViewModels (Today, Habits, AddEditTimeline, Stats, etc.) │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                        Domain Layer                         │
│  - Models (Habit, HabitCompletion, TimelineItem, Subtask)   │
│  - Repository Interfaces (Habit, Timeline, Billing, AI)     │
│  - Use Cases (GetTodayTimeline, CalculateStreak, PlanDayAi) │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                         Data Layer                          │
│  - Room Database (Entities with UUID, updatedAt, SyncStatus)│
│  - Database Pre-seeding (3 starter habits + 2 timeline tasks)│
│  - DataStore Preferences (ThemeMode, Notification prefs)    │
│  - WorkManager / AlarmManager (Reminders & Alerts)          │
└─────────────────────────────────────────────────────────────┘
```

---

## Design System: "Notion-Inspired Black & White"

- **Color Palette**:
  - Light Theme: Background `#FAFAFA`, Surface `#FFFFFF`, Text Primary `#191919`, Secondary `#757575`, Hairline Border `#E5E5E5`.
  - Dark Theme: Background `#191919`, Surface `#222222`, Text Primary `#FFFFFF`, Secondary `#9E9E9E`, Hairline Border `#333333`.
  - Accent Color: Single warm terracotta / red `#EB5757` used exclusively for progress indicators, streaks, and primary CTAs.
- **Structure**: 1px dividers, flat bordered cards (no heavy drop shadows), clean checkbox & ring completion toggles with smooth check animations.
- **Heatmap**: 5-week (35-day) monochrome consistency grid with intensity levels mapped from subtle gray to deep ink / silver white.

---

## Core Screens

1. **Today**: Vertical Structured-style daily timeline merging time-blocked tasks and scheduled habits for any selected day. Live streak indicators, completion toggles, daily progress bar, and AI day planner action.
2. **Habits**: Complete habit management (creation, editing, archiving, repeat schedule Mon–Sun, energy levels, time of day, and custom reminder alarms).
3. **Add/Edit Task or Event**: Minimalist task composer with start/end time blocking, checklist subtasks, color tags, notes, and recurrence.
4. **Stats**: Overall completion rate percentage, active streak, best all-time streak, 5-week grayscale completion heatmap, and per-habit breakdowns.
5. **Settings & Pro Paywall**: Dynamic theme switcher (System / Light / Dark), notification permission management, and Freemium Pro paywall modal.

---

## Freemium Model & Feature Split

| Feature | Free Tier | Pro Tier |
| :--- | :---: | :---: |
| **Unlimited Habits & Recurrence** | ✅ Always Free | ✅ |
| **Unlimited Timeline Tasks & Events** | ✅ Always Free | ✅ |
| **Streaks & 5-Week Grayscale Heatmap** | ✅ Always Free | ✅ |
| **Light & Dark Mode** | ✅ Always Free | ✅ |
| **Scheduled Alarms & Notifications** | ✅ Always Free | ✅ |
| **AI Day Planning** | — | ⭐️ Pro |
| **Advanced Productivity & Energy Trends** | — | ⭐️ Pro |
| **Curated Custom Themes (Nord, Sepia, etc.)** | — | ⭐️ Pro |
| **Home Screen Widgets** | — | ⭐️ Pro |

---

## Extension Points & Plugging in Real Services

### 1. Future Cloud Sync
Every entity (`HabitEntity`, `HabitCompletionEntity`, `TimelineItemEntity`) has:
- `id`: Stable UUID string.
- `updatedAt`: Millisecond epoch timestamp.
- `syncStatus`: `PENDING`, `SYNCED`, or `FAILED`.

To connect Firebase, Supabase, or a custom backend:
1. Implement a `SyncWorker` or `RemoteDataSource` that queries records where `syncStatus == PENDING`.
2. Push changed entities to the backend, update `syncStatus = SYNCED`, and pull new records from remote using `updatedAt > lastSyncTimestamp`.

### 2. Google Play Billing
The subscription gate is governed by `BillingRepository`.
- Located at: `app/src/main/java/com/habitflow/app/data/repository/BillingRepositoryImpl.kt`
- Replace the mock flow with the official Google Play `BillingClient`:
  - Query in-app / subscription product details for `habitflow_pro_monthly` and `habitflow_pro_lifetime`.
  - Handle purchase tokens in `PurchasesUpdatedListener`.

### 3. AI Day Planning (LLM Integration)
The AI scheduler is abstracted behind `AiPlannerRepository`.
- Located at: `app/src/main/java/com/habitflow/app/data/repository/AiPlannerRepositoryImpl.kt`
- Replace the mock response with a call to Google Gemini / OpenAI / Anthropic API passing the user's habits, energy levels, and commitments to receive optimized time blocks.

---

## How to Build and Run

### Prerequisites
- **Android Studio Ladybug (2024.2+)** or newer.
- **JDK 17 or 21** (bundled with Android Studio JBR).
- **Android SDK Platform 35 / 36**.

### Build via Command Line
```powershell
# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## License
MIT License. Structured timeline & habit tracking — never paywalled.
