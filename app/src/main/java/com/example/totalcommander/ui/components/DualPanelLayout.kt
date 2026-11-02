package com.example.totalcommander.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.totalcommander.model.FileItem
import com.example.totalcommander.model.PanelId
import com.example.totalcommander.model.PanelState
import com.example.totalcommander.model.SortMode
import java.io.File

@Composable
fun DualPanelLayout(
    activePanelId: PanelId,
    leftPanel: PanelState,
    rightPanel: PanelState,
    isSplitView: Boolean,
    displayMode: String,
    onSelectPanel: (PanelId) -> Unit,
    onItemSelect: (PanelId, FileItem) -> Unit,
    onItemAction: (PanelId, FileItem) -> Unit,
    onItemContextMenu: (PanelId, FileItem) -> Unit,
    onSortChange: (PanelId, SortMode) -> Unit,
    onGoToHome: (PanelId) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isSplitView) {
            // Split mode: Side by side
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(
                            width = if (activePanelId == PanelId.LEFT) 1.5.dp else 0.dp,
                            color = if (activePanelId == PanelId.LEFT) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                ) {
                    SinglePanelContainer(
                        panelState = leftPanel,
                        isActive = activePanelId == PanelId.LEFT,
                        displayMode = displayMode,
                        isCompact = true,
                        onActivate = { onSelectPanel(PanelId.LEFT) },
                        onItemSelect = { onItemSelect(PanelId.LEFT, it) },
                        onItemAction = { onItemAction(PanelId.LEFT, it) },
                        onItemContextMenu = { onItemContextMenu(PanelId.LEFT, it) },
                        onSortChange = { onSortChange(PanelId.LEFT, it) },
                        onGoToHome = { onGoToHome(PanelId.LEFT) }
                    )
                }

                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Right Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(
                            width = if (activePanelId == PanelId.RIGHT) 1.5.dp else 0.dp,
                            color = if (activePanelId == PanelId.RIGHT) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                ) {
                    SinglePanelContainer(
                        panelState = rightPanel,
                        isActive = activePanelId == PanelId.RIGHT,
                        displayMode = displayMode,
                        isCompact = true,
                        onActivate = { onSelectPanel(PanelId.RIGHT) },
                        onItemSelect = { onItemSelect(PanelId.RIGHT, it) },
                        onItemAction = { onItemAction(PanelId.RIGHT, it) },
                        onItemContextMenu = { onItemContextMenu(PanelId.RIGHT, it) },
                        onSortChange = { onSortChange(PanelId.RIGHT, it) },
                        onGoToHome = { onGoToHome(PanelId.RIGHT) }
                    )
                }
            }
        } else {
            // Tabbed mode
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = if (activePanelId == PanelId.LEFT) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = activePanelId == PanelId.LEFT,
                        onClick = { onSelectPanel(PanelId.LEFT) },
                        modifier = Modifier.testTag("tab_panel_left"),
                        text = {
                            PanelTabTitle(
                                title = "Panel A",
                                path = leftPanel.currentPath,
                                isZip = leftPanel.isZipMode,
                                count = leftPanel.items.count { !it.isParentDir },
                                selectedCount = leftPanel.selectedCount
                            )
                        }
                    )
                    Tab(
                        selected = activePanelId == PanelId.RIGHT,
                        onClick = { onSelectPanel(PanelId.RIGHT) },
                        modifier = Modifier.testTag("tab_panel_right"),
                        text = {
                            PanelTabTitle(
                                title = "Panel B",
                                path = rightPanel.currentPath,
                                isZip = rightPanel.isZipMode,
                                count = rightPanel.items.count { !it.isParentDir },
                                selectedCount = rightPanel.selectedCount
                            )
                        }
                    )
                }

                val currentPanel = if (activePanelId == PanelId.LEFT) leftPanel else rightPanel
                SinglePanelContainer(
                    panelState = currentPanel,
                    isActive = true,
                    displayMode = displayMode,
                    isCompact = displayMode == "compact",
                    onActivate = { },
                    onItemSelect = { onItemSelect(activePanelId, it) },
                    onItemAction = { onItemAction(activePanelId, it) },
                    onItemContextMenu = { onItemContextMenu(activePanelId, it) },
                    onSortChange = { onSortChange(activePanelId, it) },
                    onGoToHome = { onGoToHome(activePanelId) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PanelTabTitle(
    title: String,
    path: String,
    isZip: Boolean,
    count: Int,
    selectedCount: Int
) {
    val dirName = if (isZip) "[ZIP] ${File(path).name}" else File(path).name.ifEmpty { "/" }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isZip) Icons.Default.Archive else Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$title: $dirName",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold
        )
        if (selectedCount > 0) {
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "$selectedCount/$count",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        } else {
            Text(
                text = "($count)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SinglePanelContainer(
    panelState: PanelState,
    isActive: Boolean,
    displayMode: String,
    onActivate: () -> Unit,
    onItemSelect: (FileItem) -> Unit,
    onItemAction: (FileItem) -> Unit,
    onItemContextMenu: (FileItem) -> Unit,
    onSortChange: (SortMode) -> Unit,
    onGoToHome: () -> Unit,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onActivate)
    ) {
        FileListView(
            items = panelState.items,
            selectedPaths = panelState.selectedPaths,
            currentSort = panelState.sortMode,
            sortAscending = panelState.sortAscending,
            isLoading = panelState.isLoading,
            displayMode = displayMode,
            isCompact = isCompact || displayMode == "compact",
            onItemSelect = onItemSelect,
            onItemAction = onItemAction,
            onItemContextMenu = onItemContextMenu,
            onSortChange = onSortChange,
            onGoToHome = onGoToHome
        )
    }
}
