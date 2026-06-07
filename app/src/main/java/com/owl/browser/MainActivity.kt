package com.owl.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.owl.browser.data.SettingsRepository
import com.owl.browser.presentation.MainViewModel
import com.owl.browser.presentation.MainViewModelFactory
import com.owl.browser.presentation.browser.BrowserScreen
import com.owl.browser.presentation.onboarding.OnboardingScreen
import com.owl.browser.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val repository = SettingsRepository(applicationContext)
        val viewModel: MainViewModel by viewModels { MainViewModelFactory(repository) }
        
        setContent {
            MyApplicationTheme {
                val isFirstRun by viewModel.isFirstRun.collectAsState()
                
                if (isFirstRun == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val navController = rememberNavController()
                    val startDestination = if (isFirstRun == true) "onboarding" else "browser"
                    
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onFinish = {
                                    viewModel.completeOnboarding()
                                    navController.navigate("browser") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("browser") {
                            val glassOpacity by viewModel.glassOpacity.collectAsState()
                            val blurRadius by viewModel.blurRadius.collectAsState()
                            BrowserScreen(
                                glassOpacity = glassOpacity,
                                blurRadius = blurRadius
                            )
                        }
                    }
                }
            }
        }
    }
}
