package com.forma.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.domain.repository.DarkModeOption
import com.forma.app.domain.repository.PaletteFamily
import com.forma.app.domain.repository.UserPreferencesRepository
import com.forma.app.ui.FormaApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val paletteFamily by preferencesRepository.paletteFamily.collectAsState(initial = PaletteFamily.MATCHA_OAT)
            val darkModeOption by preferencesRepository.darkModeOption.collectAsState(initial = DarkModeOption.LIGHT)
            val isPrivacyMaskingEnabled by preferencesRepository.isPrivacyMaskingEnabled.collectAsState(initial = false)

            LaunchedEffect(isPrivacyMaskingEnabled) {
                if (isPrivacyMaskingEnabled) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            FormaTheme(
                paletteFamily = paletteFamily,
                darkModeOption = darkModeOption
            ) {
                FormaApp(preferencesRepository = preferencesRepository)
            }
        }
    }
}

