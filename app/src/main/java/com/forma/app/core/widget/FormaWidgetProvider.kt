package com.forma.app.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.forma.app.MainActivity
import com.forma.app.R
import com.forma.app.core.chronotype.CircadianEnergyEngine
import com.forma.app.core.util.DateUtils
import com.forma.app.data.local.dao.HabitCompletionDao
import com.forma.app.data.local.dao.HabitDao
import com.forma.app.domain.model.Chronotype
import com.forma.app.domain.repository.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.Locale

class FormaWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FormaWidgetEntryPoint {
        fun habitDao(): HabitDao
        fun habitCompletionDao(): HabitCompletionDao
        fun userPreferencesRepository(): UserPreferencesRepository
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_forma)

            // Date format
            val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            views.setTextViewText(R.id.widget_date, dateFormat.format(Date()))

            var statusTitle = "Daily Flow Architecture"
            var statusSubtitle = "Focus on your highest-leverage ritual today."

            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    FormaWidgetEntryPoint::class.java
                )
                val userPrefs = entryPoint.userPreferencesRepository()
                val habitDao = entryPoint.habitDao()
                val habitCompletionDao = entryPoint.habitCompletionDao()

                runBlocking(Dispatchers.IO) {
                    val chronotype = userPrefs.chronotype.firstOrNull() ?: Chronotype.BIMODAL_NOCTURNAL
                    val currentHour = LocalTime.now().hour
                    val curve = CircadianEnergyEngine().calculateDailyEnergyCurve(chronotype)
                    val activePoint = curve.find { it.hour == currentHour }
                    val zone = activePoint?.zone

                    val todayIso = DateUtils.formatDateIso(DateUtils.today())
                    val dayOfWeek = LocalDate.now().dayOfWeek.value
                    val allHabits = habitDao.getActiveHabits().first()
                    val completions = habitCompletionDao.getCompletionsForDate(todayIso).first()
                    val completedIds = completions.map { it.habitId }.toSet()

                    val scheduledHabits = allHabits.filter { habit ->
                        val repeatDays = if (habit.repeatDays.isBlank()) {
                            emptySet()
                        } else {
                            habit.repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                        }
                        (repeatDays.isEmpty() || repeatDays.contains(dayOfWeek)) && !habit.isWintering
                    }

                    val total = scheduledHabits.size
                    val done = scheduledHabits.count { completedIds.contains(it.id) }

                    if (zone != null) {
                        val zoneTag = zone.displayName.uppercase()
                        statusTitle = "$zoneTag • ${chronotype.displayName.substringBefore(" (")}"
                        statusSubtitle = if (total > 0) {
                            "$done/$total rituals completed • ${zone.subtitle}"
                        } else {
                            zone.subtitle
                        }
                    } else if (total > 0) {
                        statusTitle = "Daily Flow ($done/$total)"
                        statusSubtitle = "$done of $total rituals finished today."
                    }
                }
            } catch (_: Exception) {}

            views.setTextViewText(R.id.widget_status_title, statusTitle)
            views.setTextViewText(R.id.widget_status_subtitle, statusSubtitle)

            // Intent to launch MainActivity on Today
            val todayIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "today")
            }
            val todayPendingIntent = PendingIntent.getActivity(
                context,
                0,
                todayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_action_today, todayPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_root, todayPendingIntent)

            // Intent to launch MainActivity on Pomodoro
            val pomodoroIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "pomodoro")
            }
            val pomodoroPendingIntent = PendingIntent.getActivity(
                context,
                1,
                pomodoroIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_action_pomodoro, pomodoroPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
