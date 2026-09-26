package com.forma.app.core.auth.clerk

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.forma.app.BuildConfig
import com.forma.app.domain.model.AuthUser
import com.forma.app.domain.model.SubscriptionTier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

private val Context.clerkDataStore: DataStore<Preferences> by preferencesDataStore(name = "forma_clerk_auth_prefs")

@Singleton
class ClerkClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ClerkClient"
        private val KEY_SESSION_ID = stringPreferencesKey("clerk_session_id")
        private val KEY_CLIENT_ID = stringPreferencesKey("clerk_client_id")
        private val KEY_SESSION_TOKEN = stringPreferencesKey("clerk_session_token")
        private val KEY_CACHED_USER_JSON = stringPreferencesKey("clerk_cached_user_json")
    }

    val publishableKey: String = BuildConfig.CLERK_PUBLISHABLE_KEY.ifBlank {
        "pk_test_dW5pcXVlLW1vbml0b3ItNDg0Ny5jbGVyay5hY2NvdW50cy5kZXYk"
    }

    val frontendApiHost: String by lazy {
        try {
            val parts = publishableKey.split("_")
            if (parts.size >= 3) {
                val encodedDomain = parts[2].trimEnd('$')
                val decoded = String(Base64.decode(encodedDomain, Base64.DEFAULT), StandardCharsets.UTF_8).trimEnd('$')
                if (decoded.isNotBlank()) decoded else "unique-monitor-4847.clerk.accounts.dev"
            } else {
                "unique-monitor-4847.clerk.accounts.dev"
            }
        } catch (e: Exception) {
            "unique-monitor-4847.clerk.accounts.dev"
        }
    }

    private val baseUrl: String get() = "https://$frontendApiHost/v1"

    private val _currentUser = MutableStateFlow<AuthUser?>(null)

    val cachedUser: Flow<AuthUser?> = context.clerkDataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_CACHED_USER_JSON]
        if (!jsonStr.isNullOrBlank()) {
            try {
                parseUserFromJson(JSONObject(jsonStr), prefs[KEY_SESSION_TOKEN])
            } catch (e: Exception) {
                null
            }
        } else {
            _currentUser.value
        }
    }

    suspend fun getCurrentSessionToken(): String? {
        return context.clerkDataStore.data.map { it[KEY_SESSION_TOKEN] }.first()
    }

    suspend fun getCurrentUser(): AuthUser? = withContext(Dispatchers.IO) {
        val cached = cachedUser.first()
        if (cached != null) return@withContext cached

        // Attempt refreshing client from Clerk Frontend API
        try {
            val response = executeRequest("GET", "/client", null, getSavedClientHeaders())
            if (response.statusCode in 200..299) {
                val json = JSONObject(response.body)
                val responseClient = json.optJSONObject("response") ?: json.optJSONObject("client") ?: json
                val user = extractUserFromClientJson(responseClient)
                if (user != null) {
                    saveSessionState(
                        clientId = responseClient.optString("id"),
                        sessionId = responseClient.optJSONArray("sessions")?.optJSONObject(0)?.optString("id"),
                        sessionToken = responseClient.optJSONArray("sessions")?.optJSONObject(0)?.optJSONObject("last_active_token")?.optString("jwt"),
                        userJson = responseClient.optJSONArray("sessions")?.optJSONObject(0)?.optJSONObject("user")?.toString()
                    )
                    _currentUser.value = user
                    return@withContext user
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed fetching current Clerk client: ${e.message}")
        }
        null
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("identifier", email.trim())
                put("password", pass)
                put("strategy", "password")
            }

            val response = executeRequest("POST", "/client/sign_ins", body.toString(), getSavedClientHeaders())
            if (response.statusCode in 200..299) {
                val json = JSONObject(response.body)
                val responseData = json.optJSONObject("response") ?: json
                val status = responseData.optString("status")

                if (status == "complete") {
                    val sessionId = responseData.optString("created_session_id")
                    val user = fetchUserForSession(sessionId, email, null)
                    return@withContext Result.success(user)
                } else {
                    // Try attempting first factor if needed
                    val signInId = responseData.optString("id")
                    val attemptBody = JSONObject().apply {
                        put("strategy", "password")
                        put("password", pass)
                    }
                    val attemptResp = executeRequest("POST", "/client/sign_ins/$signInId/attempt_first_factor", attemptBody.toString(), getSavedClientHeaders())
                    if (attemptResp.statusCode in 200..299) {
                        val attemptJson = JSONObject(attemptResp.body)
                        val attemptData = attemptJson.optJSONObject("response") ?: attemptJson
                        val sessionId = attemptData.optString("created_session_id")
                        val user = fetchUserForSession(sessionId, email, null)
                        return@withContext Result.success(user)
                    } else {
                        val errMsg = parseClerkError(attemptResp.body)
                        return@withContext Result.failure(Exception(errMsg))
                    }
                }
            } else {
                val errMsg = parseClerkError(response.body)
                return@withContext Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Clerk signIn error", e)
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String?): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val names = (displayName ?: "").trim().split(" ", limit = 2)
            val firstName = names.getOrNull(0) ?: ""
            val lastName = names.getOrNull(1) ?: ""

            val body = JSONObject().apply {
                put("email_address", email.trim())
                put("password", pass)
                if (firstName.isNotBlank()) put("first_name", firstName)
                if (lastName.isNotBlank()) put("last_name", lastName)
            }

            val response = executeRequest("POST", "/client/sign_ups", body.toString(), getSavedClientHeaders())
            if (response.statusCode in 200..299) {
                val json = JSONObject(response.body)
                val responseData = json.optJSONObject("response") ?: json
                val status = responseData.optString("status")

                if (status == "complete") {
                    val sessionId = responseData.optString("created_session_id")
                    val user = fetchUserForSession(sessionId, email, displayName)
                    return@withContext Result.success(user)
                } else {
                    val signUpId = responseData.optString("id")
                    val user = AuthUser(
                        uid = signUpId.ifBlank { "clerk_usr_${email.hashCode()}" },
                        email = email.trim(),
                        displayName = displayName?.trim(),
                        isAnonymous = false,
                        tier = SubscriptionTier.FREE
                    )
                    saveSessionState("client_$signUpId", "session_$signUpId", "token_$signUpId", JSONObject().apply {
                        put("id", user.uid)
                        put("email", user.email)
                        put("name", user.displayName)
                    }.toString())
                    _currentUser.value = user
                    return@withContext Result.success(user)
                }
            } else {
                val errMsg = parseClerkError(response.body)
                return@withContext Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Clerk signUp error", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleToken(idToken: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("strategy", "oauth_google")
                put("token", idToken)
            }
            val response = executeRequest("POST", "/client/sign_ins", body.toString(), getSavedClientHeaders())
            if (response.statusCode in 200..299) {
                val json = JSONObject(response.body)
                val responseData = json.optJSONObject("response") ?: json
                val sessionId = responseData.optString("created_session_id")
                val user = fetchUserForSession(sessionId, null, null)
                Result.success(user)
            } else {
                val errMsg = parseClerkError(response.body)
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Clerk Google sign-in error", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("identifier", email.trim())
                put("strategy", "reset_password_email_code")
            }
            val response = executeRequest("POST", "/client/sign_ins", body.toString(), getSavedClientHeaders())
            if (response.statusCode in 200..299) {
                Result.success(Unit)
            } else {
                val errMsg = parseClerkError(response.body)
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sessionId = context.clerkDataStore.data.map { it[KEY_SESSION_ID] }.first()
            if (!sessionId.isNullOrBlank()) {
                executeRequest("DELETE", "/client/sessions/$sessionId/remove", null, getSavedClientHeaders())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Sign out network call error: ${e.message}")
        } finally {
            clearSessionState()
            _currentUser.value = null
        }
        Result.success(Unit)
    }

    private suspend fun fetchUserForSession(sessionId: String?, emailFallback: String?, nameFallback: String?): AuthUser {
        val clientHeaders = getSavedClientHeaders()
        var user: AuthUser? = null

        if (!sessionId.isNullOrBlank()) {
            try {
                val touchResp = executeRequest("POST", "/client/sessions/$sessionId/touch", null, clientHeaders)
                if (touchResp.statusCode in 200..299) {
                    val json = JSONObject(touchResp.body)
                    val responseClient = json.optJSONObject("response") ?: json.optJSONObject("client") ?: json
                    user = extractUserFromClientJson(responseClient)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Touch session failed: ${e.message}")
            }
        }

        val finalUser = user ?: AuthUser(
            uid = sessionId?.ifBlank { "clerk_usr_anon" } ?: "clerk_usr_anon",
            email = emailFallback,
            displayName = nameFallback,
            isAnonymous = false,
            tier = SubscriptionTier.FREE
        )

        saveSessionState(
            clientId = null,
            sessionId = sessionId,
            sessionToken = null,
            userJson = JSONObject().apply {
                put("id", finalUser.uid)
                put("email", finalUser.email)
                put("name", finalUser.displayName)
                put("tier", finalUser.tier.id)
            }.toString()
        )
        _currentUser.value = finalUser
        return finalUser
    }

    private suspend fun getSavedClientHeaders(): Map<String, String> {
        val prefs = context.clerkDataStore.data.first()
        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $publishableKey"
        headers["Clerk-Publishable-Key"] = publishableKey
        headers["Content-Type"] = "application/json"
        headers["User-Agent"] = "Forma-Android-Client/2.0"

        val token = prefs[KEY_SESSION_TOKEN]
        if (!token.isNullOrBlank()) {
            headers["Clerk-Session-Token"] = token
        }
        val clientId = prefs[KEY_CLIENT_ID]
        if (!clientId.isNullOrBlank()) {
            headers["Clerk-Client-Id"] = clientId
        }
        return headers
    }

    private suspend fun saveSessionState(
        clientId: String?,
        sessionId: String?,
        sessionToken: String?,
        userJson: String?
    ) {
        context.clerkDataStore.edit { prefs ->
            if (clientId != null) prefs[KEY_CLIENT_ID] = clientId
            if (sessionId != null) prefs[KEY_SESSION_ID] = sessionId
            if (sessionToken != null) prefs[KEY_SESSION_TOKEN] = sessionToken
            if (userJson != null) prefs[KEY_CACHED_USER_JSON] = userJson
        }
    }

    private suspend fun clearSessionState() {
        context.clerkDataStore.edit { prefs ->
            prefs.remove(KEY_CLIENT_ID)
            prefs.remove(KEY_SESSION_ID)
            prefs.remove(KEY_SESSION_TOKEN)
            prefs.remove(KEY_CACHED_USER_JSON)
        }
    }

    private fun extractUserFromClientJson(clientJson: JSONObject): AuthUser? {
        val sessions = clientJson.optJSONArray("sessions") ?: JSONArray()
        if (sessions.length() == 0) return null
        val activeSession = sessions.optJSONObject(0) ?: return null
        val userObj = activeSession.optJSONObject("user") ?: return null
        val token = activeSession.optJSONObject("last_active_token")?.optString("jwt")
        return parseUserFromJson(userObj, token)
    }

    private fun parseUserFromJson(userJson: JSONObject, sessionToken: String?): AuthUser {
        val uid = userJson.optString("id", "clerk_usr_default")
        val firstName = userJson.optString("first_name", "")
        val lastName = userJson.optString("last_name", "")
        val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank {
            userJson.optString("name", "").takeIf { it.isNotBlank() }
        }

        var email: String? = userJson.optString("email", "").takeIf { it.isNotBlank() }
        val emailsArray = userJson.optJSONArray("email_addresses")
        if (emailsArray != null && emailsArray.length() > 0) {
            email = emailsArray.optJSONObject(0)?.optString("email_address") ?: email
        }

        val photoUrl = userJson.optString("image_url", "").takeIf { it.isNotBlank() }
        val publicMeta = userJson.optJSONObject("public_metadata")
        val tierId = publicMeta?.optString("tier") ?: userJson.optString("tier", "free")
        val tier = SubscriptionTier.fromId(tierId)

        return AuthUser(
            uid = uid,
            email = email,
            displayName = fullName,
            photoUrl = photoUrl,
            isAnonymous = false,
            tier = tier,
            sessionToken = sessionToken
        )
    }

    private fun parseClerkError(responseBody: String?): String {
        if (responseBody.isNullOrBlank()) return "An authentication error occurred."
        return try {
            val json = JSONObject(responseBody)
            val errors = json.optJSONArray("errors")
            if (errors != null && errors.length() > 0) {
                val firstErr = errors.getJSONObject(0)
                firstErr.optString("long_message").ifBlank {
                    firstErr.optString("message", "Authentication error.")
                }
            } else {
                json.optString("message", "Authentication error.")
            }
        } catch (e: Exception) {
            "Authentication error: $responseBody"
        }
    }

    private data class HttpResponse(val statusCode: Int, val body: String)

    private fun executeRequest(
        method: String,
        path: String,
        body: String?,
        headers: Map<String, String>
    ): HttpResponse {
        val url = URL(if (path.startsWith("http")) path else "$baseUrl$path")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.useCaches = false

            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }

            if (body != null && (method == "POST" || method == "PUT" || method == "PATCH")) {
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { it.write(body) }
            }

            val statusCode = conn.responseCode
            val inputStream = if (statusCode in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
            val responseText = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { it.readText() }

            return HttpResponse(statusCode, responseText)
        } finally {
            conn.disconnect()
        }
    }
}
