package com.owl.browser.presentation.browser

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class TabData(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "file:///android_asset/start.html",
    val title: String = "Owl Browser",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val thumbnail: Bitmap? = null,
    val favicon: Bitmap? = null
)

data class BrowserState(
    val tabs: List<TabData> = listOf(TabData()),
    val activeTabId: String? = null,
    val isSettingsSheetOpen: Boolean = false,
    val isIncognito: Boolean = false,
    val showBottomBar: Boolean = true,
    val isTabSwitcherOpen: Boolean = false,
    val jsAlertMessage: String? = null
) {
    val activeTab: TabData? get() = tabs.find { it.id == activeTabId }
}

class BrowserViewModel : ViewModel() {
    private val _state = MutableStateFlow(BrowserState())
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    init {
        _state.update { it.copy(activeTabId = it.tabs.first().id) }
    }

    fun showJsAlert(message: String) {
        _state.update { it.copy(jsAlertMessage = message) }
    }

    fun dismissJsAlert() {
        _state.update { it.copy(jsAlertMessage = null) }
    }

    fun updateUrl(id: String, url: String) {
        _state.update { state -> 
            state.copy(tabs = state.tabs.map { if (it.id == id) it.copy(url = url) else it }) 
        }
    }

    fun updateTitle(id: String, title: String) {
        _state.update { state -> 
            state.copy(tabs = state.tabs.map { if (it.id == id) it.copy(title = title) else it }) 
        }
    }

    fun updateProgress(id: String, progress: Int) {
        _state.update { state -> 
            state.copy(tabs = state.tabs.map { if (it.id == id) it.copy(progress = progress, isLoading = progress < 100) else it }) 
        }
    }

    fun updateFavicon(id: String, favicon: Bitmap?) {
        _state.update { state -> 
            state.copy(tabs = state.tabs.map { if (it.id == id) it.copy(favicon = favicon) else it }) 
        }
    }

    fun updateThumbnail(id: String, thumbnail: Bitmap?) {
        _state.update { state -> 
            state.copy(tabs = state.tabs.map { if (it.id == id) it.copy(thumbnail = thumbnail) else it }) 
        }
    }

    fun addNewTab() {
        val newTab = TabData()
        _state.update { state -> 
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newTab.id,
                isTabSwitcherOpen = false
            )
        }
    }

    fun closeTab(id: String) {
        _state.update { state ->
            val newTabs = state.tabs.filter { it.id != id }
            if (newTabs.isEmpty()) {
                val newTab = TabData()
                state.copy(tabs = listOf(newTab), activeTabId = newTab.id)
            } else {
                val nextActive = if (state.activeTabId == id) newTabs.last().id else state.activeTabId
                state.copy(tabs = newTabs, activeTabId = nextActive)
            }
        }
    }

    fun switchTab(id: String) {
        _state.update { it.copy(activeTabId = id, isTabSwitcherOpen = false) }
    }

    fun toggleSettingsSheet(open: Boolean) {
        _state.update { it.copy(isSettingsSheetOpen = open) }
    }

    fun toggleTabSwitcher(open: Boolean) {
        _state.update { it.copy(isTabSwitcherOpen = open) }
    }

    fun toggleIncognito() {
        _state.update { it.copy(isIncognito = !it.isIncognito) }
    }

    fun setBottomBarVisible(visible: Boolean) {
        _state.update { it.copy(showBottomBar = visible) }
    }
}
