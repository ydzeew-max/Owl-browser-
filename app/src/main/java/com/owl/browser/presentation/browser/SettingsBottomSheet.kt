package com.owl.browser.presentation.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    glassOpacity: Float
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Browser Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Section 1: Basic Tools
            SettingsSection(title = "BASIC TOOLS") {
                SettingsItem("History & Downloads")
                SettingsItem("Bookmarks & Reading List")
                SettingsToggle("Incognito Mode", false)
                SettingsToggle("Desktop Site Toggle", false)
                SettingsItem("Share Page & Find in Page")
            }

            // Section 2: Smart Navigation & Tab Management
            SettingsSection(title = "SMART NAVIGATION & TAB MANAGEMENT") {
                SettingsItem("Tab Switcher Style (Cards)")
                SettingsItem("Tab Groups")
                SettingsToggle("Auto-Close Tabs (Inactive)", true)
            }

            // Section 3: Media & Content
            SettingsSection(title = "MEDIA & CONTENT") {
                SettingsToggle("Force Dark Mode for Web Contents", true)
                SettingsToggle("System Picture-in-Picture (PiP)", true)
                SettingsToggle("Background Audio Playback", false)
                SettingsToggle("Smart Reader Mode", false)
            }

            // Section 4: Advanced UI Customization
            SettingsSection(title = "ADVANCED UI CUSTOMIZATION") {
                SettingsItem("Glass Transparency Slider: ${(glassOpacity * 100).toInt()}%")
                SettingsItem("Real-Time Background Blur Engine")
                SettingsItem("Corner Radius Grid Slider")
                SettingsItem("Typography: Tech-Futuristic")
                SettingsItem("Color Themes: Midnight Blue")
            }

            // Section 5: Advanced Security & Privacy
            SettingsSection(title = "ADVANCED SECURITY & PRIVACY") {
                SettingsToggle("Ad & Tracker Blocker", true)
                SettingsToggle("Anti-Fingerprinting Engine", true)
                SettingsToggle("Biometric App Lock", false)
                SettingsToggle("Auto-Clear Data on Exit", false)
                SettingsToggle("HTTPS Only Mode", true)
                SettingsItem("Per-Site Permissions Manager")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(
            title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(title: String) {
    Text(
        title,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}

@Composable
fun SettingsToggle(title: String, initialValue: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(title, color = Color.White)
        Switch(checked = initialValue, onCheckedChange = {})
    }
}
