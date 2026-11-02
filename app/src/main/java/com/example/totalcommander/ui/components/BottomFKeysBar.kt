package com.example.totalcommander.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomFKeysBar(
    selectedCount: Int,
    onF2Rename: () -> Unit,
    onF3View: () -> Unit,
    onF4Edit: () -> Unit,
    onF5Copy: () -> Unit,
    onF6Move: () -> Unit,
    onF7NewFolder: () -> Unit,
    onF8Delete: () -> Unit,
    onF9Terminal: () -> Unit,
    onF10Search: () -> Unit,
    onZipCompress: () -> Unit,
    onChecksum: () -> Unit,
    onPlugins: () -> Unit,
    onConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FKeyButton(
                    key = "F2",
                    label = "Rename",
                    icon = Icons.Default.Edit,
                    testTag = "f2_rename_btn",
                    onClick = onF2Rename
                )

                FKeyButton(
                    key = "F3",
                    label = "View",
                    icon = Icons.Default.Visibility,
                    testTag = "f3_view_btn",
                    onClick = onF3View
                )

                FKeyButton(
                    key = "F4",
                    label = "Edit",
                    icon = Icons.Default.Edit,
                    testTag = "f4_edit_btn",
                    onClick = onF4Edit
                )

                FKeyButton(
                    key = "F5",
                    label = "Copy",
                    icon = Icons.Default.FileCopy,
                    testTag = "f5_copy_btn",
                    badge = if (selectedCount > 0) "$selectedCount" else null,
                    highlight = selectedCount > 0,
                    onClick = onF5Copy
                )

                FKeyButton(
                    key = "F6",
                    label = "Move",
                    icon = Icons.Default.DriveFileMove,
                    testTag = "f6_move_btn",
                    badge = if (selectedCount > 0) "$selectedCount" else null,
                    onClick = onF6Move
                )

                FKeyButton(
                    key = "F7",
                    label = "NewDir",
                    icon = Icons.Default.CreateNewFolder,
                    testTag = "f7_mkdir_btn",
                    onClick = onF7NewFolder
                )

                FKeyButton(
                    key = "F8",
                    label = "Delete",
                    icon = Icons.Default.Delete,
                    testTag = "f8_delete_btn",
                    isDestructive = true,
                    onClick = onF8Delete
                )

                FKeyButton(
                    key = "F9",
                    label = "Term",
                    icon = Icons.Default.Terminal,
                    testTag = "f9_term_btn",
                    onClick = onF9Terminal
                )

                FKeyButton(
                    key = "F10",
                    label = "Search",
                    icon = Icons.Default.Search,
                    testTag = "f10_search_btn",
                    onClick = onF10Search
                )

                // Extra Total Commander Power Actions
                FKeyButton(
                    key = "ZIP",
                    label = "Pack",
                    icon = Icons.Default.Archive,
                    testTag = "zip_compress_btn",
                    onClick = onZipCompress
                )

                FKeyButton(
                    key = "HASH",
                    label = "Check",
                    icon = Icons.Default.Fingerprint,
                    testTag = "checksum_btn",
                    onClick = onChecksum
                )

                FKeyButton(
                    key = "PLUG",
                    label = "Plugins",
                    icon = Icons.Default.Extension,
                    testTag = "plugins_btn",
                    onClick = onPlugins
                )

                FKeyButton(
                    key = "INI",
                    label = "Config",
                    icon = Icons.Default.Settings,
                    testTag = "config_btn",
                    onClick = onConfig
                )
            }
        }
    }
}

@Composable
fun FKeyButton(
    key: String,
    label: String,
    icon: ImageVector,
    testTag: String,
    badge: String? = null,
    highlight: Boolean = false,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        isDestructive && highlight -> MaterialTheme.colorScheme.errorContainer
        highlight -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    val contentColor = when {
        isDestructive && highlight -> MaterialTheme.colorScheme.onErrorContainer
        highlight -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = key,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )

            if (badge != null) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
