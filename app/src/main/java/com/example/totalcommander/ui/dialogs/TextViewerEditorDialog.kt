package com.example.totalcommander.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.totalcommander.fs.FileManager
import com.example.totalcommander.model.FileItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextViewerEditorDialog(
    item: FileItem,
    isEditMode: Boolean,
    fileManager: FileManager,
    onSave: (String) -> Unit,
    onSwitchMode: () -> Unit,
    onDismiss: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var initialLoaded by remember { mutableStateOf(false) }
    var isHexMode by remember { mutableStateOf(false) }
    var isWordWrap by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchRow by remember { mutableStateOf(false) }
    var isModified by remember { mutableStateOf(false) }

    LaunchedEffect(item.path) {
        if (item.isZipEntry && item.zipArchiveSourcePath != null && item.zipEntryInternalPath != null) {
            content = fileManager.readZipEntryText(File(item.zipArchiveSourcePath), item.zipEntryInternalPath)
        } else {
            val f = File(item.path)
            content = if (f.exists()) {
                try {
                    f.readText()
                } catch (e: Exception) {
                    "Error loading text: ${e.message}"
                }
            } else ""
        }
        initialLoaded = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (isEditMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isEditMode) "F4 EDIT" else "F3 LISTER",
                                        color = if (isEditMode) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.name + if (isModified) " *" else "",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = if (item.isZipEntry) "[Archive entry] Read-only" else item.path,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("text_dialog_close_btn")) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        if (!isEditMode) {
                            // Hex mode toggle
                            IconButton(onClick = { isHexMode = !isHexMode }) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "Hex View",
                                    tint = if (isHexMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // Word wrap toggle
                            IconButton(onClick = { isWordWrap = !isWordWrap }) {
                                Icon(
                                    imageVector = Icons.Default.WrapText,
                                    contentDescription = "Word Wrap",
                                    tint = if (isWordWrap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // Search in file
                            IconButton(onClick = { showSearchRow = !showSearchRow }) {
                                Icon(Icons.Default.FindInPage, contentDescription = "Find in file")
                            }
                            // Switch to Edit
                            if (!item.isZipEntry) {
                                IconButton(onClick = onSwitchMode) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Mode")
                                }
                            }
                        } else {
                            // Save button
                            IconButton(
                                onClick = {
                                    onSave(content)
                                    isModified = false
                                },
                                modifier = Modifier.testTag("text_save_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Optional Search Bar
                if (showSearchRow && !isEditMode) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Find text in file...", fontSize = 12.sp) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 12.sp),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            IconButton(onClick = {
                                searchQuery = ""
                                showSearchRow = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(if (isHexMode) Color(0xFF0F111A) else MaterialTheme.colorScheme.surface)
                ) {
                    if (isHexMode && !isEditMode) {
                        HexViewer(content = content)
                    } else if (!isEditMode) {
                        ListerViewer(
                            content = content,
                            searchQuery = searchQuery,
                            isWordWrap = isWordWrap
                        )
                    } else {
                        // Editable text editor
                        Row(modifier = Modifier.fillMaxSize()) {
                            val lines = content.lines()
                            // Line numbers column
                            LazyColumn(
                                modifier = Modifier
                                    .width(42.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                items(lines.size) { idx ->
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                }
                            }

                            // Text Editor Field
                            BasicTextField(
                                value = content,
                                onValueChange = {
                                    content = it
                                    isModified = true
                                },
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .testTag("text_editor_input")
                            )
                        }
                    }
                }

                // Bottom Status Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lines: ${content.lines().size} | Chars: ${content.length}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isEditMode) (if (isModified) "Modified" else "Saved") else (if (isHexMode) "HEX MODE" else "TEXT MODE"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isModified) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListerViewer(
    content: String,
    searchQuery: String,
    isWordWrap: Boolean
) {
    val lines = content.lines()
    val scrollState = rememberScrollState()

    val containerModifier = if (isWordWrap) Modifier.fillMaxSize() else Modifier
        .fillMaxSize()
        .horizontalScroll(scrollState)

    LazyColumn(modifier = containerModifier.padding(vertical = 6.dp)) {
        itemsIndexed(lines) { idx, line ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 1.dp)
            ) {
                Text(
                    text = String.format("%4d ", idx + 1),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.width(36.dp)
                )

                val isMatch = searchQuery.isNotEmpty() && line.contains(searchQuery, ignoreCase = true)
                Text(
                    text = line.ifEmpty { " " },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = if (isMatch) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isMatch) FontWeight.Bold else FontWeight.Normal,
                    modifier = if (isMatch) Modifier.background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)) else Modifier
                )
            }
        }
    }
}

@Composable
fun HexViewer(content: String) {
    val bytes = content.toByteArray()
    val chunks = bytes.toList().chunked(16)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        itemsIndexed(chunks) { index, chunk ->
            val offsetStr = String.format("%08X", index * 16)
            val hexStr = chunk.joinToString(" ") { String.format("%02X", it) }.padEnd(47, ' ')
            val asciiStr = chunk.map { b ->
                val c = b.toInt().toChar()
                if (c in ' '..'~') c else '.'
            }.joinToString("")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
            ) {
                Text(
                    text = "$offsetStr: ",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF64B5F6),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = hexStr,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF81C784)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "|$asciiStr|",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFFFFD54F)
                )
            }
        }
    }
}
