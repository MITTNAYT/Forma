package com.forma.app.data.repository

import android.content.Context
import com.forma.app.core.auth.clerk.ClerkClient
import com.forma.app.domain.model.AuthState
import com.forma.app.domain.model.AuthUser
import com.forma.app.domain.repository.AuthRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clerkClient: ClerkClient
) : AuthRepository {

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    override val authState: Flow<AuthState> = clerkClient.cachedUser.map { user ->
        if (user != null) {
            AuthState.Authenticated(user)
        } else {
            val fbUser = firebaseAuth?.currentUser
            if (fbUser != null) {
                AuthState.Authenticated(
                    AuthUser(
                        uid = fbUser.uid,
                        email = fbUser.email,
                        displayName = fbUser.displayName,
                        photoUrl = fbUser.photoUrl?.toString(),
                        isAnonymous = fbUser.isAnonymous
                    )
                )
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    override suspend fun getCurrentUser(): AuthUser? {
        val clerkUser = clerkClient.getCurrentUser()
        if (clerkUser != null) return clerkUser

        val fbUser = firebaseAuth?.currentUser ?: return null
        return AuthUser(
            uid = fbUser.uid,
            email = fbUser.email,
            displayName = fbUser.displayName,
            photoUrl = fbUser.photoUrl?.toString(),
            isAnonymous = fbUser.isAnonymous
        )
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        val clerkResult = clerkClient.signInWithGoogleToken(idToken)
        if (clerkResult.isSuccess) return clerkResult

        val auth = firebaseAuth ?: return clerkResult
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw IllegalStateException("Google sign-in user is null")
            Result.success(
                AuthUser(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = user.isAnonymous
                )
            )
        } catch (e: Exception) {
            clerkResult
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        val clerkResult = clerkClient.signInWithEmail(email, password)
        if (clerkResult.isSuccess) return clerkResult

        // Secondary fallback to Firebase if enabled
        val auth = firebaseAuth ?: return clerkResult
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw IllegalStateException("User is null after sign in")
            Result.success(
                AuthUser(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = user.isAnonymous
                )
            )
        } catch (e: Exception) {
            clerkResult
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): Result<AuthUser> {
        val clerkResult = clerkClient.signUpWithEmail(email, password, displayName)
        if (clerkResult.isSuccess) return clerkResult

        val auth = firebaseAuth ?: return clerkResult
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw IllegalStateException("User creation returned null")

            if (!displayName.isNullOrBlank()) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            Result.success(
                AuthUser(
                    uid = user.uid,
                    email = user.email,
                    displayName = displayName?.trim() ?: user.displayName,
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = user.isAnonymous
                )
            )
        } catch (e: Exception) {
            clerkResult
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        val clerkResult = clerkClient.sendPasswordReset(email)
        if (clerkResult.isSuccess) return clerkResult

        val auth = firebaseAuth ?: return clerkResult
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            clerkResult
        }
    }

    override suspend fun signOut(): Result<Unit> {
        clerkClient.signOut()
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) { }
        return Result.success(Unit)
    }
}
