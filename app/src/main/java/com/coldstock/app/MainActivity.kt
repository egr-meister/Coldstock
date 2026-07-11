package com.coldstock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.navigation.ColdstockNavHost
import com.coldstock.app.ui.theme.ColdstockTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ColdstockViewModel by viewModels {
        ColdstockViewModel.factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ColdstockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    ColdstockNavHost(viewModel = viewModel, appData = state)
                }
            }
        }
    }
}
