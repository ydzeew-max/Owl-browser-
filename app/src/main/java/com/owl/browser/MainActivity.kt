package com.owl.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
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
                        val alpha by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1500)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(com.owl.browser.ui.theme.MidnightCharcoal),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_owl_logo),
                                contentDescription = "Owl Splash",
                                modifier = Modifier
                                    .size(150.dp)
                                    .alpha(alpha),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
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
                                blurRadius = blurRadius,
                                onNavigateToSettings = { section ->
                                    navController.navigate("settings/$section")
                                }
                            )
                        }
                        composable("settings/{section}") { backStackEntry ->
                            val section = backStackEntry.arguments?.getString("section") ?: ""
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Text("Settings: $section")
                            }
                        }
                    }
                }
            }
        }
    }
}
