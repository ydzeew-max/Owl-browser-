package com.owl.browser.presentation.browser

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BrowserState(
    val url: String = "file:///android_asset/start.html",
    val title: String = "Owl Browser",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val isSettingsSheetOpen: Boolean = false,
    val isIncognito: Boolean = false,
    val showBottomBar: Boolean = true
)

class BrowserViewModel : ViewModel() {
    private val _state = MutableStateFlow(BrowserState())
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    fun updateUrl(url: String) {
        _state.update { it.copy(url = url) }
    }

    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun updateProgress(progress: Int) {
        _state.update { it.copy(progress = progress, isLoading = progress < 100) }
    }

    fun toggleSettingsSheet(open: Boolean) {
        _state.update { it.copy(isSettingsSheetOpen = open) }
    }

    fun toggleIncognito() {
        _state.update { it.copy(isIncognito = !it.isIncognito) }
    }

    fun setBottomBarVisible(visible: Boolean) {
        _state.update { it.copy(showBottomBar = visible) }
    }
}
