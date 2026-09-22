package com.forma.app.domain.usecase.auth

import com.forma.app.domain.model.AuthState
import com.forma.app.domain.model.AuthUser
import com.forma.app.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthUseCasesTest {

    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        authRepository = mockk(relaxed = true)
    }

    @Test
    fun `GetAuthStateUseCase emits auth state from repository`() = runTest {
        val expectedUser = AuthUser(uid = "user_123", email = "test@forma.app")
        every { authRepository.authState } returns flowOf(AuthState.Authenticated(expectedUser))

        val useCase = GetAuthStateUseCase(authRepository)
        val state = useCase().first()

        assertTrue(state is AuthState.Authenticated)
        assertEquals("user_123", (state as AuthState.Authenticated).user.uid)
    }

    @Test
    fun `SignInWithEmailUseCase delegates to repository`() = runTest {
        val user = AuthUser(uid = "email_user_1", email = "hello@forma.app")
        coEvery { authRepository.signInWithEmail("hello@forma.app", "password123") } returns Result.success(user)

        val useCase = SignInWithEmailUseCase(authRepository)
        val result = useCase("hello@forma.app", "password123")

        assertTrue(result.isSuccess)
        assertEquals("email_user_1", result.getOrNull()?.uid)
        coVerify(exactly = 1) { authRepository.signInWithEmail("hello@forma.app", "password123") }
    }

    @Test
    fun `SignUpWithEmailUseCase creates new user with name`() = runTest {
        val user = AuthUser(uid = "new_user_1", email = "new@forma.app", displayName = "Maya")
        coEvery { authRepository.signUpWithEmail("new@forma.app", "pass1234", "Maya") } returns Result.success(user)

        val useCase = SignUpWithEmailUseCase(authRepository)
        val result = useCase("new@forma.app", "pass1234", "Maya")

        assertTrue(result.isSuccess)
        assertEquals("Maya", result.getOrNull()?.displayName)
        coVerify(exactly = 1) { authRepository.signUpWithEmail("new@forma.app", "pass1234", "Maya") }
    }

    @Test
    fun `SignOutUseCase invokes signOut on repository`() = runTest {
        coEvery { authRepository.signOut() } returns Result.success(Unit)

        val useCase = SignOutUseCase(authRepository)
        val result = useCase()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authRepository.signOut() }
    }
}
