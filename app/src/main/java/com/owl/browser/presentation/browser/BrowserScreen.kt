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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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
    onNavigateToSettings: (String) -> Unit = {},
    viewModel: BrowserViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var inputText by remember { mutableStateOf(state.activeTab?.url ?: "") }
    val isFocused = remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val bottomBarHeightPx = remember { with(density) { 140.dp.toPx() } }
    var bottomBarOffset by remember { mutableStateOf(0f) }

    // Map to hold WebView instances
    val webViews = remember { mutableStateMapOf<String, WebView>() }

    // Sync input text with actual URL when not typing
    LaunchedEffect(state.activeTab?.url) {
        if (!isFocused.value) {
            inputText = state.activeTab?.url ?: ""
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
                    android.widget.FrameLayout(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { container ->
                    val activeId = state.activeTabId ?: return@AndroidView
                    
                    // Create WebView if it doesn't exist
                    if (!webViews.containsKey(activeId)) {
                        val webView = WebView(container.context).apply {
                            layoutParams = android.widget.FrameLayout.LayoutParams(
                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
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
                            addJavascriptInterface(object {
                                @android.webkit.JavascriptInterface
                                fun focusOmnibox() {
                                    post { isFocused.value = true }
                                }
                            }, "NativeBridge")
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    url?.let { viewModel.updateUrl(activeId, it) }
                                    isFocused.value = false
                                    bottomBarOffset = 0f
                                }
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    view?.title?.let { viewModel.updateTitle(activeId, it) }
                                    view?.evaluateJavascript("document.body.style.paddingBottom = '140px';", null)
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    viewModel.updateProgress(activeId, newProgress)
                                }
                                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                                    super.onReceivedIcon(view, icon)
                                    viewModel.updateFavicon(activeId, icon)
                                }
                                override fun onPermissionRequest(request: PermissionRequest) {
                                    request.grant(request.resources)
                                }
                                override fun onJsAlert(
                                    view: WebView?,
                                    url: String?,
                                    message: String?,
                                    result: android.webkit.JsResult?
                                ): Boolean {
                                    if (message != null) {
                                        viewModel.showJsAlert(message)
                                        result?.confirm() // Auto-confirm the underlying JS blocking call to prevent hanging
                                        return true
                                    }
                                    return super.onJsAlert(view, url, message, result)
                                }
                            }
                            setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                                val diff = scrollY - oldScrollY
                                if (diff > 0) {
                                    bottomBarOffset = (bottomBarOffset + diff).coerceAtMost(bottomBarHeightPx)
                                } else if (diff < -10) {
                                    bottomBarOffset = 0f
                                }
                            }
                        }
                        webViews[activeId] = webView
                        webView.loadUrl(state.activeTab?.url ?: "file:///android_asset/start.html")
                    }

                    // Attach the active WebView and detach others safely
                    if (container.childCount == 0 || container.getChildAt(0) != webViews[activeId]) {
                        container.removeAllViews()
                        val activeWebView = webViews[activeId]
                        if (activeWebView?.parent != null) {
                            (activeWebView.parent as android.view.ViewGroup).removeView(activeWebView)
                        }
                        container.addView(activeWebView)
                    }
                    
                    // Cleanup removed tabs
                    val currentTabIds = state.tabs.map { it.id }.toSet()
                    webViews.keys.toList().forEach { id ->
                        if (!currentTabIds.contains(id)) {
                            val wv = webViews.remove(id)
                            wv?.destroy()
                        }
                    }
                }
            )

            // Progress Bar
            if (state.activeTab?.isLoading == true) {
                LinearProgressIndicator(
                    progress = { (state.activeTab?.progress ?: 0) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.statusBars),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Bottom UI Container
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .offset { androidx.compose.ui.unit.IntOffset(0, bottomBarOffset.toInt()) }
            ) {
                    // Omnibox
                    GlassBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(bottom = 8.dp),
                        opacity = glassOpacity,
                        blurRadius = blurRadius,
                        shape = RoundedCornerShape(14.dp)
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
                                    if (!fixUrl.startsWith("http") && !fixUrl.startsWith("file://")) {
                                        fixUrl = "https://www.google.com/search?q=\$fixUrl"
                                    }
                                    state.activeTabId?.let { id ->
                                        webViews[id]?.loadUrl(fixUrl)
                                    }
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
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { state.activeTabId?.let { webViews[it]?.goBack() } }) {
                                Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White)
                            }
                            IconButton(onClick = { state.activeTabId?.let { webViews[it]?.reload() } }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                            }
                            IconButton(onClick = {
                                // Capture screenshot before showing tab switcher
                                state.activeTabId?.let { id ->
                                    val wv = webViews[id]
                                    if (wv != null) {
                                        try {
                                            val bitmap = android.graphics.Bitmap.createBitmap(wv.width, wv.height, android.graphics.Bitmap.Config.ARGB_8888)
                                            val canvas = android.graphics.Canvas(bitmap)
                                            wv.draw(canvas)
                                            // Scale down for memory
                                            val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, wv.width / 4, wv.height / 4, true)
                                            viewModel.updateThumbnail(id, scaled)
                                            if (bitmap != scaled) bitmap.recycle()
                                        } catch (e: Exception) {
                                            // ignore layout exceptions
                                        }
                                    }
                                }
                                viewModel.toggleTabSwitcher(true) 
                            }) {
                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Tabs", tint = Color.White)
                            }
                            IconButton(onClick = { viewModel.toggleSettingsSheet(true) }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                            }
                        }
                    }
            }
        }

        // Overlays
        if (state.isTabSwitcherOpen) {
            TabSwitcherScreen(
                state = state,
                onTabSelected = { viewModel.switchTab(it) },
                onTabClosed = { viewModel.closeTab(it) },
                onNewTab = { viewModel.addNewTab() },
                onCloseSwitcher = { viewModel.toggleTabSwitcher(false) }
            )
        }

        if (state.isSettingsSheetOpen) {
            SettingsBottomSheet(
                onDismiss = { viewModel.toggleSettingsSheet(false) },
                onNavigateToSettings = { route -> 
                    onNavigateToSettings(route)
                }
            )
        }

        if (state.jsAlertMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissJsAlert() },
                containerColor = com.owl.browser.ui.theme.DarkSlateGraphite,
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("Owl Browser", fontWeight = FontWeight.Bold) },
                text = { Text(state.jsAlertMessage!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissJsAlert() }) {
                        Text("DIMISS", color = com.owl.browser.ui.theme.SilverWhite)
                    }
                }
            )
        }
    }
}
