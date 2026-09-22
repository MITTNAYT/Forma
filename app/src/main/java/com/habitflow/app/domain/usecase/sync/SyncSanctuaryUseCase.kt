package com.habitflow.app.domain.usecase.sync

import com.habitflow.app.domain.repository.SyncRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SyncSanctuaryUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    val isSyncing: StateFlow<Boolean> = syncRepository.isSyncing
    val lastSyncedAt: StateFlow<Long?> = syncRepository.lastSyncedAt

    suspend operator fun invoke(): Result<Unit> = syncRepository.syncAll()
}
