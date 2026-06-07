package com.owl.browser.presentation.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.owl.browser.presentation.components.GlassBox
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    glassOpacity: Float,
    blurRadius: Float,
    viewModel: BrowserViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var inputText by remember { mutableStateOf(state.url) }

    val isFocused = remember { mutableStateOf(false) }

    // Sync input text with actual URL when not typing
    LaunchedEffect(state.url) {
        if (!isFocused.value) {
            inputText = state.url
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // We handle insets manually
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            setSupportMultipleWindows(true)
                            javaScriptCanOpenWindowsAutomatically = true
                            allowFileAccess = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                safeBrowsingEnabled = true
                            }
                        }

                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let { viewModel.updateUrl(it) }
                                isFocused.value = false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                view?.title?.let { viewModel.updateTitle(it) }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                viewModel.updateProgress(newProgress)
                            }
                            
                            override fun onPermissionRequest(request: PermissionRequest) {
                                // Real implementation would prompt user, auto-granting for demo per instructions
                                request.grant(request.resources)
                            }
                        }

                        // Override scrolling to auto-hide bottom bar
                        setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                            val diff = scrollY - oldScrollY
                            if (diff > 20) {
                                viewModel.setBottomBarVisible(false)
                            } else if (diff < -20) {
                                viewModel.setBottomBarVisible(true)
                            }
                        }

                        loadUrl(state.url)
                        webViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // Just caching the ref
                    webViewRef = view
                }
            )

            // Progress Bar
            if (state.isLoading) {
                LinearProgressIndicator(
                    progress = { state.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.statusBars),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Bottom UI Container
            AnimatedVisibility(
                visible = state.showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring()
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Omnibox
                    GlassBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(bottom = 8.dp),
                        opacity = glassOpacity,
                        blurRadius = blurRadius,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.fillMaxSize().onFocusChanged { isFocused.value = it.isFocused },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    var fixUrl = inputText
                                    if (!fixUrl.startsWith("http")) {
                                        fixUrl = "https://www.google.com/search?q=\$fixUrl"
                                    }
                                    webViewRef?.loadUrl(fixUrl)
                                }
                            ),
                            placeholder = { Text("Search or type URL", color = Color.Gray) }
                        )
                    }

                    // Bottom Navigation Bar
                    GlassBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        opacity = glassOpacity,
                        blurRadius = blurRadius,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { webViewRef?.goBack() }) {
                                Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White)
                            }
                            IconButton(onClick = { webViewRef?.reload() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                            }
                            IconButton(onClick = { /* Tab Switcher */ }) {
                                Icon(Icons.Default.List, contentDescription = "Tabs", tint = Color.White)
                            }
                            IconButton(onClick = { viewModel.toggleSettingsSheet(true) }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Settings Bottom Sheet
        if (state.isSettingsSheetOpen) {
            SettingsBottomSheet(
                onDismiss = { viewModel.toggleSettingsSheet(false) },
                glassOpacity = glassOpacity
            )
        }
    }
}
