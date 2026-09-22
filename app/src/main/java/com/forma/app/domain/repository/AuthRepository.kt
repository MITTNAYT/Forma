package com.forma.app.domain.repository

import com.forma.app.domain.model.AuthState
import com.forma.app.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    suspend fun getCurrentUser(): AuthUser?
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String?): Result<AuthUser>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
