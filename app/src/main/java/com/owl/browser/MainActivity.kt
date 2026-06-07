package com.owl.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            val theme by viewModel.theme.collectAsState()
            val language by viewModel.language.collectAsState()
            
            val isDarkTheme = when (theme) {
                "Light" -> false
                "Dark (Recommended)" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            LaunchedEffect(language) {
                val locale = if (language == "Русский") java.util.Locale("ru") else java.util.Locale("en")
                java.util.Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)
            }
            
            MyApplicationTheme(
                darkTheme = isDarkTheme
            ) {
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
                            val currentLang by viewModel.language.collectAsState()
                            val currentThemeState by viewModel.theme.collectAsState()
                            val currentEngine by viewModel.searchEngine.collectAsState()
                            
                            OnboardingScreen(
                                currentLanguage = currentLang,
                                onLanguageSelected = { viewModel.setLanguage(it) },
                                currentTheme = currentThemeState,
                                onThemeSelected = { viewModel.setTheme(it) },
                                currentSearchEngine = currentEngine,
                                onSearchEngineSelected = { viewModel.setSearchEngine(it) },
                                onFinish = {
                                    viewModel.completeOnboarding()
                                    navController.navigate("browser") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("browser") {
                            val searchEngine by viewModel.searchEngine.collectAsState()
                            BrowserScreen(
                                searchEngine = searchEngine,
                                onNavigateToSettings = { section ->
                                    navController.navigate("settings/$section")
                                }
                            )
                        }
                        composable("settings/{section}") { backStackEntry ->
                            val section = backStackEntry.arguments?.getString("section") ?: ""
                            Scaffold(
                                topBar = {
                                    @OptIn(ExperimentalMaterial3Api::class)
                                    TopAppBar(
                                        title = { 
                                            Text(
                                                text = section.replaceFirstChar { it.uppercase() }, 
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            ) 
                                        },
                                        navigationIcon = {
                                            IconButton(onClick = { navController.popBackStack() }) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.ArrowBack, 
                                                    contentDescription = "Back", 
                                                    tint = Color.White
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = com.owl.browser.ui.theme.MidnightCharcoal
                                        )
                                    )
                                },
                                containerColor = com.owl.browser.ui.theme.MidnightCharcoal
                            ) { padding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(padding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = null,
                                            tint = com.owl.browser.ui.theme.SilverWhite,
                                            modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                                        )
                                        Text(
                                            "Settings: ${section.replaceFirstChar { it.uppercase() }}",
                                            color = com.owl.browser.ui.theme.SilverWhite,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            "Operational configuration layout.",
                                            color = Color.Gray,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
