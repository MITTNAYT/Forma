package com.forma.app.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.forma.app.MainActivity
import com.forma.app.R
import com.forma.app.core.util.DateUtils
import com.forma.app.data.local.dao.HabitCompletionDao
import com.forma.app.data.local.dao.HabitDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class EnsoRingWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EnsoWidgetEntryPoint {
        fun habitDao(): HabitDao
        fun habitCompletionDao(): HabitCompletionDao
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
            val views = RemoteViews(context.packageName, R.layout.widget_enso_ring)

            val todayDate = DateUtils.formatDateIso(DateUtils.today())
            val dayOfWeek = LocalDate.now().dayOfWeek.value

            var titleText = "FORMA"
            var subtitleText = "Mindful Flow"

            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    EnsoWidgetEntryPoint::class.java
                )
                val habitDao = entryPoint.habitDao()
                val habitCompletionDao = entryPoint.habitCompletionDao()

                runBlocking(Dispatchers.IO) {
                    val allHabits = habitDao.getActiveHabits().first()
                    val completions = habitCompletionDao.getCompletionsForDate(todayDate).first()
                    val completedHabitIds = completions.map { it.habitId }.toSet()

                    val scheduledHabits = allHabits.filter { habit ->
                        val repeatDays = if (habit.repeatDays.isBlank()) {
                            emptySet()
                        } else {
                            habit.repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                        }
                        (repeatDays.isEmpty() || repeatDays.contains(dayOfWeek)) && !habit.isWintering
                    }

                    val total = scheduledHabits.size
                    val done = scheduledHabits.count { completedHabitIds.contains(it.id) }

                    if (total > 0) {
                        val pct = ((done.toDouble() / total.toDouble()) * 100).toInt()
                        titleText = "$pct% Done"
                        subtitleText = "$done of $total rituals"
                    }
                }
            } catch (_: Exception) {}

            views.setTextViewText(R.id.enso_widget_title, titleText)
            views.setTextViewText(R.id.enso_widget_subtitle, subtitleText)

            // Intent to launch MainActivity
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "today")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                200,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.enso_widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
