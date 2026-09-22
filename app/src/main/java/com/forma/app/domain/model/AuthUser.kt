package com.forma.app.domain.model

data class AuthUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val sessionToken: String? = null
)
