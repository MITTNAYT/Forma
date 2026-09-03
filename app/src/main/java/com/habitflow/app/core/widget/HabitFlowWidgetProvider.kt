package com.habitflow.app.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.habitflow.app.MainActivity
import com.habitflow.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitFlowWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.widget_habitflow)

            // Date format
            val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            views.setTextViewText(R.id.widget_date, dateFormat.format(Date()))

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
