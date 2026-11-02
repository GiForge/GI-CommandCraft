package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.totalcommander.ui.TotalCommanderApp
import com.example.totalcommander.ui.theme.CommanderThemes
import com.example.ui.theme.Typography
import com.example.totalcommander.viewmodel.TotalCommanderViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TotalCommanderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val config by viewModel.config.collectAsState()
            val colorScheme = CommanderThemes.getColorScheme(config.themePreset)

            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography
            ) {
                TotalCommanderApp(viewModel = viewModel)
            }
        }
    }
}
