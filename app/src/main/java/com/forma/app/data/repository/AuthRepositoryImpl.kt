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
        val trimmedEmail = email.trim()
        var lastError: Exception? = null

        // 1. Try Firebase Auth
        val auth = firebaseAuth
        if (auth != null) {
            try {
                val result = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
                val user = result.user
                if (user != null) {
                    val authUser = AuthUser(
                        uid = user.uid,
                        email = user.email,
                        displayName = user.displayName ?: trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        photoUrl = user.photoUrl?.toString(),
                        isAnonymous = user.isAnonymous
                    )
                    return Result.success(authUser)
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        // 2. Try Clerk Client
        val clerkResult = clerkClient.signInWithEmail(trimmedEmail, password)
        if (clerkResult.isSuccess) return clerkResult

        // 3. Return the actual error so the user sees actionable feedback
        val errorMessage = friendlyAuthError(lastError ?: clerkResult.exceptionOrNull())
        return Result.failure(Exception(errorMessage))
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): Result<AuthUser> {
        val trimmedEmail = email.trim()
        val finalName = displayName?.trim()?.ifBlank { null } ?: trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        var lastError: Exception? = null

        // 1. Try Firebase Auth
        val auth = firebaseAuth
        if (auth != null) {
            try {
                val result = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
                val user = result.user
                if (user != null) {
                    try {
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(finalName)
                            .build()
                        user.updateProfile(profileUpdates).await()
                    } catch (_: Exception) { }

                    val authUser = AuthUser(
                        uid = user.uid,
                        email = user.email,
                        displayName = finalName,
                        photoUrl = user.photoUrl?.toString(),
                        isAnonymous = user.isAnonymous
                    )
                    return Result.success(authUser)
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        // 2. Try Clerk Client
        val clerkResult = clerkClient.signUpWithEmail(trimmedEmail, password, finalName)
        if (clerkResult.isSuccess) return clerkResult

        // 3. Return the actual error so the user sees actionable feedback
        val errorMessage = friendlyAuthError(lastError ?: clerkResult.exceptionOrNull())
        return Result.failure(Exception(errorMessage))
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

    /** Map Firebase/Clerk exceptions to user-friendly messages. */
    private fun friendlyAuthError(e: Throwable?): String {
        val msg = e?.message?.lowercase() ?: return "Authentication failed. Check your connection and try again."
        return when {
            "no user record" in msg || "user-not-found" in msg ->
                "No account found with this email. Try creating a new one."
            "password is invalid" in msg || "wrong-password" in msg ->
                "Incorrect password. Please try again or reset it."
            "email address is badly formatted" in msg || "invalid-email" in msg ->
                "Please enter a valid email address."
            "email address is already in use" in msg || "email-already-in-use" in msg ->
                "An account with this email already exists. Try signing in."
            "password should be at least" in msg || "weak-password" in msg ->
                "Password is too weak. Use at least 6 characters."
            "network" in msg || "timeout" in msg ->
                "Network error. Please check your connection."
            "too-many-requests" in msg || "unusual activity" in msg ->
                "Too many attempts. Please wait a moment and try again."
            else -> e.localizedMessage ?: "Authentication failed. Please try again."
        }
    }
}
