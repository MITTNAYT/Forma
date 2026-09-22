package com.forma.app.core.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.forma.app.R
import com.forma.app.core.util.DateUtils
import com.forma.app.data.local.dao.HabitCompletionDao
import com.forma.app.data.local.dao.HabitDao
import com.forma.app.data.local.entity.HabitEntity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class HabitListWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return HabitListRemoteViewsFactory(applicationContext)
    }
}

class HabitListRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HabitListWidgetEntryPoint {
        fun habitDao(): HabitDao
        fun habitCompletionDao(): HabitCompletionDao
    }

    private var habitItems = listOf<WidgetItemData>()

    data class WidgetItemData(
        val habit: HabitEntity,
        val isCompleted: Boolean,
        val subtitle: String
    )

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            HabitListWidgetEntryPoint::class.java
        )
        val habitDao = entryPoint.habitDao()
        val habitCompletionDao = entryPoint.habitCompletionDao()

        val todayDate = DateUtils.formatDateIso(DateUtils.today())
        val dayOfWeek = LocalDate.now().dayOfWeek.value // 1..7 (Monday..Sunday)

        runBlocking(Dispatchers.IO) {
            try {
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

                habitItems = scheduledHabits.map { habit ->
                    val isDone = completedHabitIds.contains(habit.id)
                    val timing = when (habit.timeOfDay) {
                        "MORNING" -> "Morning"
                        "AFTERNOON" -> "Afternoon"
                        "EVENING" -> "Evening"
                        else -> "Anytime"
                    }
                    val cue = habit.stackedCueText?.let { " • $it" } ?: ""
                    WidgetItemData(
                        habit = habit,
                        isCompleted = isDone,
                        subtitle = "$timing$cue"
                    )
                }
            } catch (e: Exception) {
                habitItems = emptyList()
            }
        }
    }

    override fun onDestroy() {
        habitItems = emptyList()
    }

    override fun getCount(): Int = habitItems.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= habitItems.size) return null
        val item = habitItems[position]

        val views = RemoteViews(context.packageName, R.layout.widget_habit_list_item)
        views.setTextViewText(R.id.widget_item_title, item.habit.name)
        views.setTextViewText(R.id.widget_item_subtitle, item.subtitle)
        views.setTextViewText(R.id.widget_item_icon, if (item.isCompleted) "✓" else "○")

        // Fill-in Intent for clicking the row (Action to toggle completion)
        val fillInIntent = Intent().apply {
            putExtra(WidgetActionReceiver.EXTRA_HABIT_ID, item.habit.id)
            putExtra(WidgetActionReceiver.EXTRA_DATE, DateUtils.formatDateIso(DateUtils.today()))
        }
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
