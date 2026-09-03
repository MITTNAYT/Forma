package com.habitflow.app.domain.usecase

import com.habitflow.app.data.repository.AiPlannerRepositoryImpl
import com.habitflow.app.domain.model.TimelineItem
import com.habitflow.app.domain.repository.AiPlanPreset
import com.habitflow.app.domain.repository.BillingRepository
import com.habitflow.app.domain.repository.HabitRepository
import com.habitflow.app.domain.repository.TimelineRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlanDayWithAiUseCaseTest {

    private val billingRepository: BillingRepository = mockk()
    private val habitRepository: HabitRepository = mockk()
    private val timelineRepository: TimelineRepository = mockk(relaxed = true)
    private val aiPlannerRepository = AiPlannerRepositoryImpl()

    private lateinit var useCase: PlanDayWithAiUseCase

    @Before
    fun setup() {
        useCase = PlanDayWithAiUseCase(
            billingRepository = billingRepository,
            habitRepository = habitRepository,
            timelineRepository = timelineRepository,
            aiPlannerRepository = aiPlannerRepository
        )
    }

    @Test
    fun `when user is not pro, returns RequiresPro`() = runTest {
        coEvery { billingRepository.isPro } returns flowOf(false)

        val result = useCase("2026-09-03")

        assertTrue(result is PlanDayWithAiUseCase.Result.RequiresPro)
        coVerify(exactly = 0) { timelineRepository.insertTimelineItem(any()) }
    }

    @Test
    fun `when user is pro, generates deep work items and saves to repository`() = runTest {
        coEvery { billingRepository.isPro } returns flowOf(true)
        coEvery { habitRepository.getAllHabits(includeArchived = false) } returns flowOf(emptyList())
        coEvery { timelineRepository.getTimelineItemsForDate(any()) } returns flowOf(emptyList())

        val result = useCase("2026-09-03", AiPlanPreset.DEEP_WORK)

        assertTrue(result is PlanDayWithAiUseCase.Result.Success)
        val success = result as PlanDayWithAiUseCase.Result.Success
        assertEquals(3, success.generatedItems.size)
        assertTrue(success.generatedItems.any { it.title.contains("Deep Focus") })
        coVerify(exactly = 3) { timelineRepository.insertTimelineItem(any()) }
    }

    @Test
    fun `when health balance preset selected, generates health oriented items`() = runTest {
        coEvery { billingRepository.isPro } returns flowOf(true)
        coEvery { habitRepository.getAllHabits(includeArchived = false) } returns flowOf(emptyList())
        coEvery { timelineRepository.getTimelineItemsForDate(any()) } returns flowOf(emptyList())

        val result = useCase("2026-09-03", AiPlanPreset.HEALTH_BALANCE)

        assertTrue(result is PlanDayWithAiUseCase.Result.Success)
        val success = result as PlanDayWithAiUseCase.Result.Success
        assertTrue(success.generatedItems.any { it.title.contains("Sunlight") })
    }

    @Test
    fun `when exam study preset selected, generates study sprint items`() = runTest {
        coEvery { billingRepository.isPro } returns flowOf(true)
        coEvery { habitRepository.getAllHabits(includeArchived = false) } returns flowOf(emptyList())
        coEvery { timelineRepository.getTimelineItemsForDate(any()) } returns flowOf(emptyList())

        val result = useCase("2026-09-03", AiPlanPreset.EXAM_STUDY)

        assertTrue(result is PlanDayWithAiUseCase.Result.Success)
        val success = result as PlanDayWithAiUseCase.Result.Success
        assertTrue(success.generatedItems.any { it.title.contains("Active Recall") })
    }
}
