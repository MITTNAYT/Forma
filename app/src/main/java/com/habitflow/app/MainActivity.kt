package com.habitflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.habitflow.app.core.designsystem.HabitFlowTheme
import com.habitflow.app.domain.repository.DarkModeOption
import com.habitflow.app.domain.repository.PaletteFamily
import com.habitflow.app.domain.repository.UserPreferencesRepository
import com.habitflow.app.ui.HabitFlowApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val paletteFamily by preferencesRepository.paletteFamily.collectAsState(initial = PaletteFamily.MATCHA_OAT)
            val darkModeOption by preferencesRepository.darkModeOption.collectAsState(initial = DarkModeOption.LIGHT)

            HabitFlowTheme(
                paletteFamily = paletteFamily,
                darkModeOption = darkModeOption
            ) {
                HabitFlowApp()
            }
        }
    }
}
