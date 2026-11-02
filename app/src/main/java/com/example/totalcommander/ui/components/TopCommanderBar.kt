package com.example.totalcommander.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.totalcommander.model.PanelId
import com.example.totalcommander.model.PanelState
import com.example.totalcommander.model.SortMode
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopCommanderBar(
    activePanelId: PanelId,
    activePanelState: PanelState,
    isSplitView: Boolean,
    onToggleSplitView: () -> Unit,
    onSwapPanels: () -> Unit,
    onEqualizePanels: () -> Unit,
    onNavigateSegment: (String) -> Unit,
    onReload: () -> Unit,
    onSelectDrive: () -> Unit,
    onSelectAll: () -> Unit,
    onUnselectAll: () -> Unit,
    onInvertSelection: () -> Unit,
    onSortChange: (SortMode) -> Unit,
    onOpenConfig: () -> Unit,
    onOpenPlugins: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenTerminal: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSelectMenu by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "GI",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "GI-CommandCraft",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (activePanelId == PanelId.LEFT) "Panel A [Left]" else "Panel B [Right]",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onSelectDrive,
                        modifier = Modifier.testTag("drive_selector_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Storage Drives",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Split/Tabs toggle
                    IconButton(
                        onClick = onToggleSplitView,
                        modifier = Modifier.testTag("toggle_split_btn")
                    ) {
                        Icon(
                            imageVector = if (isSplitView) Icons.Default.ViewStream else Icons.Default.ViewColumn,
                            contentDescription = if (isSplitView) "Switch to Tabs" else "Switch to Split",
                            tint = if (isSplitView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Swap Panels
                    IconButton(
                        onClick = onSwapPanels,
                        modifier = Modifier.testTag("swap_panels_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Panels",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Sort button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort Files",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${mode.label} ${if (activePanelState.sortMode == mode) if (activePanelState.sortAscending) "▲" else "▼" else ""}",
                                            fontWeight = if (activePanelState.sortMode == mode) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSortChange(mode)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Overflow Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Synchronize Panels (=)") },
                                onClick = {
                                    onEqualizePanels()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Select All (*)") },
                                onClick = {
                                    onSelectAll()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Unselect All") },
                                onClick = {
                                    onUnselectAll()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Invert Selection") },
                                onClick = {
                                    onInvertSelection()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Advanced Search (Alt+F7)") },
                                onClick = {
                                    onOpenSearch()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Terminal Console (F9)") },
                                onClick = {
                                    onOpenTerminal()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Installed Plugins") },
                                onClick = {
                                    onOpenPlugins()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuration (totalcmd.ini)") },
                                onClick = {
                                    onOpenConfig()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Reload Current Directory") },
                                onClick = {
                                    onReload()
                                    showMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Breadcrumb path navigation bar
            BreadcrumbBar(
                path = activePanelState.currentPath,
                isZipMode = activePanelState.isZipMode,
                onNavigate = onNavigateSegment
            )
        }
    }
}

@Composable
fun BreadcrumbBar(
    path: String,
    isZipMode: Boolean,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Home / Smartphone internal shared storage
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onNavigate(android.os.Environment.getExternalStorageDirectory().absolutePath) }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home Storage",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " Home",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Device Root / icon
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onNavigate("/") }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Device Root",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " /",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (isZipMode) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "[ZIP] ${path.removePrefix("zip://")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                val segments = path.split("/").filter { it.isNotEmpty() }
                var accumulated = ""
                segments.forEachIndexed { index, segment ->
                    accumulated += "/$segment"
                    val segmentPath = accumulated

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )

                    val isLast = index == segments.size - 1
                    Text(
                        text = segment,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onNavigate(segmentPath) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
