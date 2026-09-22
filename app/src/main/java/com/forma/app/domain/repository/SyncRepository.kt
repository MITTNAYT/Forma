package com.forma.app.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SyncRepository {
    val isSyncing: StateFlow<Boolean>
    val lastSyncedAt: StateFlow<Long?>
    suspend fun syncAll(): Result<Unit>
    suspend fun uploadLocalToCloud(): Result<Unit>
    suspend fun downloadCloudToLocal(): Result<Unit>
}
