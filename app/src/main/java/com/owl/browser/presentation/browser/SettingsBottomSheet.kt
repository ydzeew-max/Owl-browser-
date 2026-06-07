package com.owl.browser.presentation.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.owl.browser.ui.theme.HeavySmokedGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToSettings: (String) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = HeavySmokedGlass,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section: Quick Tools Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickToolItem(Icons.Default.History, "History") { 
                    onDismiss()
                    onNavigateToSettings("history")
                }
                QuickToolItem(Icons.Default.Download, "Downloads") { 
                    onDismiss()
                    onNavigateToSettings("downloads")
                }
                QuickToolItem(Icons.Default.Star, "Bookmarks") { 
                    onDismiss()
                    onNavigateToSettings("bookmarks")
                }
                QuickToolItem(Icons.Default.Face, "Incognito") { 
                    onDismiss()
                    onNavigateToSettings("incognito")
                }
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            // Navigation Subsections
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RoutingRow(Icons.Default.Security, "Security & Privacy") {
                    onDismiss()
                    onNavigateToSettings("security")
                }
                RoutingRow(Icons.Default.Build, "Advanced Configuration") {
                    onDismiss()
                    onNavigateToSettings("advanced")
                }
                RoutingRow(Icons.Default.Brush, "Theme & Appearance") {
                    onDismiss()
                    onNavigateToSettings("appearance")
                }
                RoutingRow(Icons.Default.Info, "About Owl Browser") {
                    onDismiss()
                    onNavigateToSettings("about")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun QuickToolItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun RoutingRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = Color.LightGray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = Color.Gray)
    }
}
