package com.owl.browser.presentation.browser

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.owl.browser.ui.theme.DarkSlateGraphite
import com.owl.browser.ui.theme.MidnightCharcoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherScreen(
    state: BrowserState,
    onTabSelected: (String) -> Unit,
    onTabClosed: (String) -> Unit,
    onNewTab: () -> Unit,
    onCloseSwitcher: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Tabs (${state.tabs.size})", color = Color.White) },
                actions = {
                    IconButton(onClick = onCloseSwitcher) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightCharcoal)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewTab,
                containerColor = DarkSlateGraphite,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Tab")
            }
        },
        containerColor = MidnightCharcoal
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.tabs, key = { it.id }) { tab ->
                val isSelected = tab.id == state.activeTabId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.6f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab.id) }
                        .then(
                            if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(12.dp))
                            else Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateGraphite)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (tab.favicon != null) {
                                Image(
                                    bitmap = tab.favicon.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = tab.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onTabClosed(tab.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Tab", tint = Color.LightGray)
                            }
                        }

                        // Thumbnail
                        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                            if (tab.thumbnail != null) {
                                Image(
                                    bitmap = tab.thumbnail.asImageBitmap(),
                                    contentDescription = "Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Default background
                                Box(modifier = Modifier.fillMaxSize().background(MidnightCharcoal))
                            }
                        }
                    }
                }
            }
        }
    }
}
