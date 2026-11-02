package com.example.totalcommander.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.totalcommander.fs.FileManager
import com.example.totalcommander.model.FileItem
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CopyConfirmDialog(
    items: List<FileItem>,
    targetDir: File,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Copy Files (F5)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Copy ${items.size} item(s):",
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = items.take(4).joinToString("\n") { "• ${it.name} (${it.formattedSize})" } +
                                if (items.size > 4) "\n... and ${items.size - 4} more" else "",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Target directory:", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = targetDir.absolutePath,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, modifier = Modifier.testTag("confirm_copy_btn")) {
                Text("Copy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MoveConfirmDialog(
    items: List<FileItem>,
    targetDir: File,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move Files (F6)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Move ${items.size} item(s):",
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = items.take(4).joinToString("\n") { "• ${it.name}" } +
                                if (items.size > 4) "\n... and ${items.size - 4} more" else "",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Target directory:", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = targetDir.absolutePath,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, modifier = Modifier.testTag("confirm_move_btn")) {
                Text("Move")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    items: List<FileItem>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Files (F8)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Are you sure you want to permanently delete ${items.size} item(s)?",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = items.take(5).joinToString("\n") { "• ${it.name}" } +
                                if (items.size > 5) "\n... and ${items.size - 5} more" else "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_delete_btn")
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NewFolderDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Directory (F7)") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Directory Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_folder_input")
            )
        },
        confirmButton = {
            Button(
                onClick = { if (folderName.isNotBlank()) onConfirm(folderName.trim()) },
                enabled = folderName.isNotBlank(),
                modifier = Modifier.testTag("confirm_new_folder_btn")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NewFileDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var fileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New File") },
        text = {
            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("File Name (e.g. note.txt)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (fileName.isNotBlank()) onConfirm(fileName.trim()) },
                enabled = fileName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ZipCompressDialog(
    items: List<FileItem>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var zipName by remember {
        val defaultName = if (items.size == 1) items.first().name.substringBeforeLast('.') + ".zip" else "Archive_${System.currentTimeMillis() % 10000}.zip"
        mutableStateOf(defaultName)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compress to ZIP Archive") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Compressing ${items.size} item(s):")
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = items.take(4).joinToString("\n") { "• ${it.name}" },
                        fontSize = 11.sp,
                        modifier = Modifier.padding(6.dp)
                    )
                }
                OutlinedTextField(
                    value = zipName,
                    onValueChange = { zipName = it },
                    label = { Text("Archive Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("zip_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (zipName.isNotBlank()) onConfirm(zipName.trim()) },
                enabled = zipName.isNotBlank(),
                modifier = Modifier.testTag("confirm_zip_compress_btn")
            ) {
                Text("Compress")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChecksumDialog(
    item: FileItem,
    fileManager: FileManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var md5 by remember { mutableStateOf<String?>(null) }
    var sha256 by remember { mutableStateOf<String?>(null) }
    var crc32 by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(item.path) {
        val file = File(item.path)
        if (file.exists() && file.isFile) {
            md5 = fileManager.calculateChecksum(file, "MD5")
            sha256 = fileManager.calculateChecksum(file, "SHA-256")
            crc32 = fileManager.calculateChecksum(file, "CRC32")
        }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("File Checksum & Hash")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Size: ${item.formattedSize}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Divider()

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    ChecksumRow(label = "CRC32", value = crc32 ?: "N/A", context = context)
                    ChecksumRow(label = "MD5", value = md5 ?: "N/A", context = context)
                    ChecksumRow(label = "SHA-256", value = sha256 ?: "N/A", context = context)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ChecksumRow(label: String, value: String, context: Context) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy $label", modifier = Modifier.size(14.dp))
            }
        }
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun DriveSelectorDialog(
    fileManager: FileManager,
    onSelectDrive: (path: String) -> Unit,
    onOpenPlugins: () -> Unit,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val hasPermission = fileManager.hasAllFilesPermission()
    val locations = remember { fileManager.getStorageLocations() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Posizioni & Dischi Smartphone")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!hasPermission) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Permesso di accesso ai file richiesto",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Su Android per visualizzare e modificare le cartelle reali dello smartphone (Download, DCIM, Documenti) è necessario abilitare 'Accesso a tutti i file'.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    onRequestPermission()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Abilita Accesso a Tutti i File", fontSize = 11.sp)
                            }
                        }
                    }
                }

                locations.forEach { loc ->
                    val icon = when (loc.iconKey) {
                        "phone" -> Icons.Default.Home
                        "system_root" -> Icons.Default.Storage
                        "download" -> Icons.Default.Download
                        "camera" -> Icons.Default.Image
                        "picture" -> Icons.Default.Image
                        "document" -> Icons.Default.Description
                        "sd_card" -> Icons.Default.Archive
                        else -> Icons.Default.Folder
                    }

                    DriveItemRow(
                        icon = icon,
                        title = loc.title,
                        subtitle = loc.subtitle,
                        onClick = {
                            onSelectDrive(loc.file.absolutePath)
                            onDismiss()
                        }
                    )
                }

                DriveItemRow(
                    icon = Icons.Default.Extension,
                    title = "\\\\ Plugins di Rete e Cloud",
                    subtitle = "WebDAV, FTP, SFTP, LAN Samba, Checksum",
                    onClick = {
                        onOpenPlugins()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

@Composable
fun DriveItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun RenameDialog(
    item: FileItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameText by remember { mutableStateOf(item.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("Rinomina") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Nuovo nome per '${item.name}':",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_input_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nameText) },
                enabled = nameText.isNotBlank() && nameText.trim() != item.name,
                modifier = Modifier.testTag("rename_confirm_btn")
            ) {
                Text("Rinomina")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun ContextMenuDialog(
    item: FileItem,
    isSelected: Boolean,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
    onRename: () -> Unit,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onZip: () -> Unit,
    onChecksum: () -> Unit,
    onProperties: () -> Unit,
    onToggleSelect: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = item.formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ContextMenuItem(
                    icon = Icons.Default.PlayArrow,
                    key = "ENTER",
                    label = "Apri / Esegui (Doppio tap)",
                    onClick = {
                        onDismiss()
                        onAction()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Edit,
                    key = "F2",
                    label = "Rinomina",
                    onClick = {
                        onDismiss()
                        onRename()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Visibility,
                    key = "F3",
                    label = "Visualizza (Lister Hex/Testo)",
                    onClick = {
                        onDismiss()
                        onView()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Description,
                    key = "F4",
                    label = "Modifica (Editor di testo)",
                    onClick = {
                        onDismiss()
                        onEdit()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.FileCopy,
                    key = "F5",
                    label = "Copia nell'altro pannello",
                    onClick = {
                        onDismiss()
                        onCopy()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.DriveFileMove,
                    key = "F6",
                    label = "Sposta nell'altro pannello",
                    onClick = {
                        onDismiss()
                        onMove()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Archive,
                    key = "ZIP",
                    label = "Comprimi in archivio ZIP",
                    onClick = {
                        onDismiss()
                        onZip()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Fingerprint,
                    key = "HASH",
                    label = "Calcola Checksum (MD5/SHA256)",
                    onClick = {
                        onDismiss()
                        onChecksum()
                    }
                )

                ContextMenuItem(
                    icon = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    key = "SEL",
                    label = if (isSelected) "Deseleziona file (Tap singolo)" else "Seleziona file (Tap singolo)",
                    onClick = {
                        onDismiss()
                        onToggleSelect()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Info,
                    key = "INFO",
                    label = "Proprietà e Dettagli",
                    onClick = {
                        onDismiss()
                        onProperties()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Delete,
                    key = "F8",
                    label = "Elimina",
                    isDestructive = true,
                    onClick = {
                        onDismiss()
                        onDelete()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun ContextMenuItem(
    icon: ImageVector,
    key: String,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
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
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FilePropertiesDialog(
    item: FileItem,
    onDismiss: () -> Unit
) {
    val dateStr = if (item.lastModified <= 0) "N/A" else {
        java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(item.lastModified))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("Proprietà File") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PropertyField(label = "Nome", value = item.name)
                PropertyField(label = "Tipo", value = if (item.isDirectory) "Cartella" else "File (${item.extension.ifEmpty { "nessuna estensione" }})")
                PropertyField(label = "Percorso", value = item.path)
                PropertyField(label = "Dimensione", value = "${item.formattedSize} (${item.size} bytes)")
                PropertyField(label = "Ultima modifica", value = dateStr)
                PropertyField(label = "Permessi UNIX", value = item.permissions)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

@Composable
fun PropertyField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
        Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(top = 4.dp))
    }
}
