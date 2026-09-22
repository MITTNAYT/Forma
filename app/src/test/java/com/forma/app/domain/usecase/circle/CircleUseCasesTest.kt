package com.forma.app.domain.usecase.circle

import com.forma.app.domain.model.Circle
import com.forma.app.domain.model.CircleReactionType
import com.forma.app.domain.repository.CircleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CircleUseCasesTest {

    private val repository: CircleRepository = mockk()

    private lateinit var createCircleUseCase: CreateCircleUseCase
    private lateinit var joinCircleUseCase: JoinCircleUseCase
    private lateinit var leaveCircleUseCase: LeaveCircleUseCase
    private lateinit var observeUserCirclesUseCase: ObserveUserCirclesUseCase
    private lateinit var postCircleCheckInUseCase: PostCircleCheckInUseCase
    private lateinit var sendCircleReactionUseCase: SendCircleReactionUseCase

    @Before
    fun setup() {
        createCircleUseCase = CreateCircleUseCase(repository)
        joinCircleUseCase = JoinCircleUseCase(repository)
        leaveCircleUseCase = LeaveCircleUseCase(repository)
        observeUserCirclesUseCase = ObserveUserCirclesUseCase(repository)
        postCircleCheckInUseCase = PostCircleCheckInUseCase(repository)
        sendCircleReactionUseCase = SendCircleReactionUseCase(repository)
    }

    @Test
    fun `createCircle with blank name returns failure`() = runTest {
        val result = createCircleUseCase("", "desc", listOf("Morning"))
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.createCircle(any(), any(), any()) }
    }

    @Test
    fun `createCircle with valid name delegates to repository`() = runTest {
        val expectedCircle = Circle(
            id = "c1",
            name = "Dawn Builders",
            description = "5 AM Focus",
            inviteCode = "DAWN5A",
            ownerId = "u1",
            habitThemes = listOf("Morning Routine"),
            memberCount = 1,
            createdAt = 1000L
        )
        coEvery { repository.createCircle("Dawn Builders", "5 AM Focus", any()) } returns Result.success(expectedCircle)

        val result = createCircleUseCase("Dawn Builders", "5 AM Focus", listOf("Morning Routine"))
        assertTrue(result.isSuccess)
        assertEquals(expectedCircle, result.getOrNull())
        coVerify(exactly = 1) { repository.createCircle("Dawn Builders", "5 AM Focus", any()) }
    }

    @Test
    fun `joinCircle with short code returns failure`() = runTest {
        val result = joinCircleUseCase("AB")
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.joinCircle(any()) }
    }

    @Test
    fun `joinCircle with valid code uppercases and calls repository`() = runTest {
        val expectedCircle = Circle(
            id = "c2",
            name = "Zen Writers",
            description = "",
            inviteCode = "WRITER",
            ownerId = "u2",
            habitThemes = emptyList(),
            memberCount = 2,
            createdAt = 2000L
        )
        coEvery { repository.joinCircle("WRITER") } returns Result.success(expectedCircle)

        val result = joinCircleUseCase("writer")
        assertTrue(result.isSuccess)
        assertEquals("WRITER", result.getOrNull()?.inviteCode)
        coVerify(exactly = 1) { repository.joinCircle("WRITER") }
    }

    @Test
    fun `sendReaction delegates to repository`() = runTest {
        coEvery { repository.sendReaction("c1", "u2", CircleReactionType.CALM) } returns Result.success(Unit)

        val result = sendCircleReactionUseCase("c1", "u2", CircleReactionType.CALM)
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.sendReaction("c1", "u2", CircleReactionType.CALM) }
    }
}
