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

class HabitMatrixWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HabitMatrixEntryPoint {
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
            val views = RemoteViews(context.packageName, R.layout.widget_habit_matrix)

            val today = DateUtils.today()
            val todayDate = DateUtils.formatDateIso(today)

            var streakText = "🔥 1d streak"
            var completionSummary = "Tracking your daily flow"
            var weeklyProgress = 50

            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    HabitMatrixEntryPoint::class.java
                )
                val habitDao = entryPoint.habitDao()
                val habitCompletionDao = entryPoint.habitCompletionDao()

                runBlocking(Dispatchers.IO) {
                    val activeHabits = habitDao.getActiveHabits().first()
                    val totalHabits = activeHabits.size.coerceAtLeast(1)

                    var weekCompletionsCount = 0
                    for (i in 0..6) {
                        val d = today.minusDays(i.toLong())
                        val dStr = DateUtils.formatDateIso(d)
                        val comps = habitCompletionDao.getCompletionsForDate(dStr).first()
                        weekCompletionsCount += comps.size
                    }

                    weeklyProgress = ((weekCompletionsCount.toDouble() / (totalHabits * 7).toDouble()) * 100).toInt().coerceIn(0, 100)
                    completionSummary = "$weekCompletionsCount rituals completed in the past 7 days"

                    // Quick streak estimation
                    val todayComps = habitCompletionDao.getCompletionsForDate(todayDate).first()
                    val streakDays = if (todayComps.isNotEmpty()) (weekCompletionsCount / totalHabits).coerceAtLeast(1) else 0
                    streakText = "🔥 ${streakDays}d streak"
                }
            } catch (_: Exception) {}

            views.setTextViewText(R.id.matrix_widget_streak, streakText)
            views.setTextViewText(R.id.matrix_widget_history_summary, completionSummary)
            views.setProgressBar(R.id.matrix_widget_progress, 100, weeklyProgress, false)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "stats")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                201,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.matrix_widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
