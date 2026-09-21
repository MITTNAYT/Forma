package com.habitflow.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.core.backup.DataExportManager
import com.habitflow.app.core.notification.NotificationHelper
import com.habitflow.app.core.util.BackupManager
import com.habitflow.app.domain.repository.BillingRepository
import com.habitflow.app.domain.repository.DailyReflectionRepository
import com.habitflow.app.domain.repository.DarkModeOption
import com.habitflow.app.domain.repository.FocusTrackerRepository
import com.habitflow.app.domain.repository.HabitRepository
import com.habitflow.app.domain.repository.PaletteFamily
import com.habitflow.app.domain.repository.ThemeMode
import com.habitflow.app.domain.repository.UserPreferencesRepository
import com.habitflow.app.domain.usecase.ExportDataUseCase
import com.habitflow.app.domain.usecase.ImportDataUseCase
import com.habitflow.app.ui.mindfulness.ZenSummaryData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val billingRepository: BillingRepository,
    private val notificationHelper: NotificationHelper,
    private val dataExportManager: DataExportManager,
    private val backupManager: BackupManager,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val dailyReflectionRepository: DailyReflectionRepository,
    private val focusTrackerRepository: FocusTrackerRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    val userName: StateFlow<String> = preferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Alex")

    val userHeadline: StateFlow<String> = preferencesRepository.userHeadline
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Architect of Daily Flow")

    val paletteFamily: StateFlow<PaletteFamily> = preferencesRepository.paletteFamily
        .stateIn(viewModelScope, SharingStarted.Eagerly, PaletteFamily.MATCHA_OAT)

    val darkModeOption: StateFlow<DarkModeOption> = preferencesRepository.darkModeOption
        .stateIn(viewModelScope, SharingStarted.Eagerly, DarkModeOption.LIGHT)

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.MATCHA_OAT)

    val notificationsEnabled: StateFlow<Boolean> = preferencesRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isPro: StateFlow<Boolean> = billingRepository.isPro
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun sendTestNotification() {
        notificationHelper.showHabitNotification(
            habitId = "test_mindful_ritual",
            habitName = "Morning Sunlight & Breath",
            habitIcon = "wb_sunny"
        )
    }

    fun exportJson(activityContext: Context) {
        viewModelScope.launch {
            val json = dataExportManager.generateJsonExport()
            dataExportManager.shareContent(
                activityContext = activityContext,
                title = "Forma-Backup.json",
                content = json,
                mimeType = "application/json"
            )
        }
    }

    fun exportMarkdown(activityContext: Context) {
        viewModelScope.launch {
            val md = dataExportManager.generateMarkdownJournal()
            dataExportManager.shareContent(
                activityContext = activityContext,
                title = "Forma-Journal.md",
                content = md,
                mimeType = "text/markdown"
            )
        }
    }

    fun exportFullBackup(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = exportDataUseCase()
            onExportReady(json)
        }
    }

    fun restoreFullBackup(json: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = importDataUseCase(json)
            result.onSuccess { count ->
                onComplete(true, "Restored $count records into HabitFlow.")
            }.onFailure { err ->
                onComplete(false, err.message ?: "Failed to parse backup JSON.")
            }
        }
    }

    fun exportEncryptedVault(activityContext: Context, passphrase: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val plainJson = exportDataUseCase()
                val encrypted = com.habitflow.app.core.crypto.CryptoVaultManager.encrypt(plainJson, passphrase.toCharArray())
                dataExportManager.shareContent(
                    activityContext = activityContext,
                    title = "HabitFlow-Secure-Backup.habitvault",
                    content = encrypted,
                    mimeType = "text/plain"
                )
                onDone(true, "Encrypted vault exported with AES-256-GCM.")
            } catch (e: Exception) {
                onDone(false, e.message ?: "Failed to encrypt vault.")
            }
        }
    }

    fun restoreEncryptedVault(passphrase: String, encryptedPayload: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val decryptedResult = com.habitflow.app.core.crypto.CryptoVaultManager.decrypt(encryptedPayload, passphrase.toCharArray())
            decryptedResult.onSuccess { plainJson ->
                val importResult = importDataUseCase(plainJson)
                importResult.onSuccess { count ->
                    onDone(true, "Successfully unlocked & restored $count records.")
                }.onFailure { err ->
                    onDone(false, "Decryption succeeded, but data import failed: ${err.message}")
                }
            }.onFailure {
                onDone(false, "Decryption failed. Incorrect passphrase or corrupt vault envelope.")
            }
        }
    }

    suspend fun getMonthlyZenSummary(): ZenSummaryData {
        val reflections = dailyReflectionRepository.getRecentReflections().first()
        val focusStats = focusTrackerRepository.getFocusTimeStats().first()
        val habits = habitRepository.getAllHabits(includeArchived = false).first()

        val avgPeace = if (reflections.isNotEmpty()) {
            reflections.map { it.mindfulnessScore.toDouble() }.average().toFloat()
        } else 5.0f

        return ZenSummaryData(
            monthName = "SEPTEMBER",
            focusHours = focusStats.thisMonthMinutes / 60f,
            ritualsCompleted = habits.size * 4,
            reflectionsLogged = reflections.size,
            averagePeaceRating = avgPeace
        )
    }

    fun setUserName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            preferencesRepository.setUserName(name.trim())
        }
    }

    fun setUserHeadline(headline: String) {
        viewModelScope.launch {
            preferencesRepository.setUserHeadline(headline.trim())
        }
    }

    fun setPaletteFamily(family: PaletteFamily) {
        viewModelScope.launch {
            preferencesRepository.setPaletteFamily(family)
        }
    }

    fun setDarkModeOption(option: DarkModeOption) {
        viewModelScope.launch {
            preferencesRepository.setDarkModeOption(option)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
        }
    }

    fun toggleMorningReminder(context: Context, enabled: Boolean) {
        if (enabled) {
            com.habitflow.app.core.notification.MindfulReminderScheduler.scheduleMorningReminder(context, 8, 0)
        } else {
            com.habitflow.app.core.notification.MindfulReminderScheduler.cancelMorningReminder(context)
        }
    }

    fun toggleEveningReminder(context: Context, enabled: Boolean) {
        if (enabled) {
            com.habitflow.app.core.notification.MindfulReminderScheduler.scheduleEveningReminder(context, 21, 30)
        } else {
            com.habitflow.app.core.notification.MindfulReminderScheduler.cancelEveningReminder(context)
        }
    }

    fun playBackgroundSound(context: Context, sound: com.habitflow.app.core.audio.AmbientSound, timerMinutes: Int) {
        com.habitflow.app.core.audio.AmbientSoundService.start(context, sound, timerMinutes)
    }

    fun stopBackgroundSound(context: Context) {
        com.habitflow.app.core.audio.AmbientSoundService.stop(context)
    }

    fun purchasePro() {
        viewModelScope.launch {
            billingRepository.purchasePro()
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            billingRepository.restorePurchases()
        }
    }

    fun toggleProStatus(isPro: Boolean) {
        viewModelScope.launch {
            billingRepository.setProStatus(isPro)
        }
    }
}

