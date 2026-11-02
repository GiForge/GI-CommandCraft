package com.example.totalcommander.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.totalcommander.model.FileItem
import com.example.totalcommander.model.SortMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FileListView(
    items: List<FileItem>,
    selectedPaths: Set<String>,
    currentSort: SortMode,
    sortAscending: Boolean,
    isLoading: Boolean,
    displayMode: String,
    isCompact: Boolean = false,
    onItemSelect: (FileItem) -> Unit,
    onItemAction: (FileItem) -> Unit,
    onItemContextMenu: (FileItem) -> Unit,
    onSortChange: (SortMode) -> Unit,
    onGoToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Empty",
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Cartella vuota o permessi limitati",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onGoToHome,
                        modifier = Modifier.testTag("empty_go_home_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accedi a Memoria Smartphone")
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table header for columns
                FileListHeader(
                    currentSort = currentSort,
                    sortAscending = sortAscending,
                    isCompact = isCompact,
                    onSortChange = onSortChange
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.path }) { item ->
                        val isSelected = item.path in selectedPaths

                        FileRowItem(
                            item = item,
                            isSelected = isSelected,
                            isCompact = isCompact,
                            displayMode = displayMode,
                            onSelect = { onItemSelect(item) },
                            onAction = { onItemAction(item) },
                            onContextMenu = { onItemContextMenu(item) }
                        )
                        Divider(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileListHeader(
    currentSort: SortMode,
    sortAscending: Boolean,
    isCompact: Boolean,
    onSortChange: (SortMode) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isCompact) 6.dp else 12.dp,
                    vertical = if (isCompact) 3.dp else 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Name
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onSortChange(SortMode.NAME) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Name",
                    fontSize = if (isCompact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentSort == SortMode.NAME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (currentSort == SortMode.NAME) {
                    Text(
                        text = if (sortAscending) " ▲" else " ▼",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (!isCompact) {
                // Ext
                Row(
                    modifier = Modifier
                        .width(42.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSortChange(SortMode.EXT) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ext",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentSort == SortMode.EXT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (currentSort == SortMode.EXT) {
                        Text(
                            text = if (sortAscending) " ▲" else " ▼",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Size
            Row(
                modifier = Modifier
                    .width(if (isCompact) 52.dp else 68.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onSortChange(SortMode.SIZE) },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Size",
                    fontSize = if (isCompact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentSort == SortMode.SIZE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (currentSort == SortMode.SIZE) {
                    Text(
                        text = if (sortAscending) " ▲" else " ▼",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (!isCompact) {
                // Date
                Row(
                    modifier = Modifier
                        .width(74.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSortChange(SortMode.DATE) },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentSort == SortMode.DATE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (currentSort == SortMode.DATE) {
                        Text(
                            text = if (sortAscending) " ▲" else " ▼",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileRowItem(
    item: FileItem,
    isSelected: Boolean,
    isCompact: Boolean,
    displayMode: String,
    onSelect: () -> Unit,
    onAction: () -> Unit,
    onContextMenu: () -> Unit
) {
    val dateStr = remember(item.lastModified) {
        if (item.lastModified <= 0) "" else {
            SimpleDateFormat("dd.MM.yy", Locale.US).format(Date(item.lastModified))
        }
    }

    val icon = getFileIcon(item)
    val iconTint = getFileIconColor(item)

    val rowBackground = when {
        isSelected -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f)
        item.isParentDir -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .combinedClickable(
                onClick = {
                    if (item.isParentDir) onAction() else onSelect()
                },
                onDoubleClick = {
                    onAction()
                },
                onLongClick = {
                    if (!item.isParentDir) onContextMenu()
                }
            )
            .padding(
                horizontal = if (isCompact) 6.dp else 10.dp,
                vertical = if (isCompact) 2.5.dp else 4.dp
            )
            .testTag("file_item_${item.name}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier.size(if (isCompact) 18.dp else 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(if (isCompact) 16.dp else 19.dp)
            )
            if (item.isZipArchive) {
                // Little zipper indicator
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 6.dp else 7.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(if (isCompact) 6.dp else 8.dp))

        // Name (strictly ALWAYS single-line across ALL modes - splitted and non-splitted)
        Text(
            text = if (item.isParentDir) "[..]" else item.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = if (isCompact) 12.sp else 13.5.sp
            ),
            fontWeight = if (item.isDirectory || item.isZipArchive) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        )

        if (!isCompact) {
            // Ext (single-line column)
            Text(
                text = if (item.isDirectory || item.isParentDir) "" else item.extension.uppercase(),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(38.dp),
                maxLines = 1
            )
        }

        // Size (strictly single line)
        Text(
            text = item.formattedSize,
            fontSize = if (isCompact) 10.sp else 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (item.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
            color = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(if (isCompact) 52.dp else 64.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            maxLines = 1
        )

        if (!isCompact) {
            // Date (single-line column)
            Text(
                text = dateStr,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.width(68.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                maxLines = 1
            )
        }
    }
}

@Composable
fun getFileIcon(item: FileItem): ImageVector {
    return when {
        item.isParentDir -> Icons.Default.FolderOpen
        item.isDirectory -> Icons.Default.Folder
        item.isZipArchive -> Icons.Default.Archive
        item.isImage -> Icons.Default.Image
        item.extension.lowercase() in listOf("mp4", "mkv", "avi", "mov", "webm") -> Icons.Default.Videocam
        item.extension.lowercase() in listOf("mp3", "flac", "ogg", "wav", "m4a") -> Icons.Default.Audiotrack
        item.extension.lowercase() in listOf("kt", "java", "py", "c", "cpp", "js", "html", "css", "json", "xml") -> Icons.Default.Code
        item.extension.lowercase() in listOf("sh", "bash", "bat") -> Icons.Default.Terminal
        item.isTextOrCode -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }
}

@Composable
fun getFileIconColor(item: FileItem): Color {
    val cs = MaterialTheme.colorScheme
    return when {
        item.isParentDir -> cs.primary
        item.isDirectory -> Color(0xFFFFA000) // Amber Folder
        item.isZipArchive -> cs.tertiary
        item.isImage -> Color(0xFF43A047) // Green
        item.extension.lowercase() in listOf("mp4", "mkv", "avi") -> Color(0xFFE53935) // Red
        item.extension.lowercase() in listOf("mp3", "flac") -> Color(0xFF8E24AA) // Purple
        item.isTextOrCode -> cs.primary
        else -> cs.outline
    }
}
