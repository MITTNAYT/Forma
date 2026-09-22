package com.habitflow.app.core.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.habitflow.app.R
import com.habitflow.app.core.audio.ZenFeedbackManager
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.usecase.ToggleHabitCompletionUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetActionReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetActionEntryPoint {
        fun toggleHabitCompletionUseCase(): ToggleHabitCompletionUseCase
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == ACTION_TOGGLE_HABIT) {
            val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
            val date = intent.getStringExtra(EXTRA_DATE) ?: DateUtils.formatDateIso(DateUtils.today())

            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetActionEntryPoint::class.java
            )
            val toggleUseCase = entryPoint.toggleHabitCompletionUseCase()

            ZenFeedbackManager.triggerTactileTick(context)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    toggleUseCase(habitId, date)
                    ZenFeedbackManager.playTibetanBowl(context)

                    // Refresh all widgets
                    val appWidgetManager = AppWidgetManager.getInstance(context)

                    // 1. Habit List Widget
                    val listComponent = ComponentName(context, HabitListWidgetProvider::class.java)
                    val listIds = appWidgetManager.getAppWidgetIds(listComponent)
                    appWidgetManager.notifyAppWidgetViewDataChanged(listIds, R.id.widget_habit_list_view)

                    // 2. Ensō Ring Widget
                    val ensoComponent = ComponentName(context, EnsoRingWidgetProvider::class.java)
                    val ensoIds = appWidgetManager.getAppWidgetIds(ensoComponent)
                    for (id in ensoIds) {
                        EnsoRingWidgetProvider.updateAppWidget(context, appWidgetManager, id)
                    }

                    // 3. Habit Matrix Widget
                    val matrixComponent = ComponentName(context, HabitMatrixWidgetProvider::class.java)
                    val matrixIds = appWidgetManager.getAppWidgetIds(matrixComponent)
                    for (id in matrixIds) {
                        HabitMatrixWidgetProvider.updateAppWidget(context, appWidgetManager, id)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_HABIT = "com.habitflow.app.action.TOGGLE_HABIT_WIDGET"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_DATE = "extra_date"

        fun createToggleIntent(context: Context, habitId: String, date: String): Intent {
            return Intent(context, WidgetActionReceiver::class.java).apply {
                action = ACTION_TOGGLE_HABIT
                putExtra(EXTRA_HABIT_ID, habitId)
                putExtra(EXTRA_DATE, date)
            }
        }
    }
}
