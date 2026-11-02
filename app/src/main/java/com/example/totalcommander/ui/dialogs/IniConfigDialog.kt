package com.example.totalcommander.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.totalcommander.config.IniConfigManager
import com.example.totalcommander.model.TcConfiguration
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IniConfigDialog(
    currentConfig: TcConfiguration,
    iniFile: File,
    onSaveConfig: (TcConfiguration) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Visual options state
    var themePreset by remember { mutableStateOf(currentConfig.themePreset) }
    var fontSize by remember { mutableFloatStateOf(currentConfig.fontSize.toFloat()) }
    var showHidden by remember { mutableStateOf(currentConfig.showHiddenFiles) }
    var confirmDelete by remember { mutableStateOf(currentConfig.confirmDelete) }
    var confirmOverwrite by remember { mutableStateOf(currentConfig.confirmOverwrite) }
    var displayMode by remember { mutableStateOf(currentConfig.displayMode) }
    var dualPaneMode by remember { mutableStateOf(currentConfig.dualPaneMode) }

    // Raw INI state
    var rawIniText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        rawIniText = if (iniFile.exists()) iniFile.readText() else IniConfigManager.generateDefaultIni()
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
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Settings & Customization",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Config File: /Config/totalcmd.ini",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("config_dialog_close_btn")) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (selectedTab == 0) {
                                    val updated = currentConfig.copy(
                                        themePreset = themePreset,
                                        fontSize = fontSize.toInt(),
                                        showHiddenFiles = showHidden,
                                        confirmDelete = confirmDelete,
                                        confirmOverwrite = confirmOverwrite,
                                        displayMode = displayMode,
                                        dualPaneMode = dualPaneMode
                                    )
                                    onSaveConfig(updated)
                                } else {
                                    val parsed = IniConfigManager.parseIni(rawIniText)
                                    iniFile.writeText(rawIniText)
                                    onSaveConfig(parsed)
                                }
                                onDismiss()
                            },
                            modifier = Modifier.testTag("config_save_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save and Apply",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Visual Options")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            // Sync raw text before switching
                            val currentUiConfig = currentConfig.copy(
                                themePreset = themePreset,
                                fontSize = fontSize.toInt(),
                                showHiddenFiles = showHidden,
                                confirmDelete = confirmDelete,
                                confirmOverwrite = confirmOverwrite,
                                displayMode = displayMode,
                                dualPaneMode = dualPaneMode
                            )
                            rawIniText = IniConfigManager.serializeIni(currentUiConfig)
                            selectedTab = 1
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Edit totalcmd.ini")
                            }
                        }
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                if (selectedTab == 0) {
                    // Visual settings
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "COLOR THEME PRESET",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ThemeCard(
                                    title = "TC Blue",
                                    color = Color(0xFF0D47A1),
                                    isSelected = themePreset == "classic_blue",
                                    onClick = { themePreset = "classic_blue" },
                                    modifier = Modifier.weight(1f)
                                )
                                ThemeCard(
                                    title = "Dark",
                                    color = Color(0xFF1E2838),
                                    isSelected = themePreset == "modern_dark",
                                    onClick = { themePreset = "modern_dark" },
                                    modifier = Modifier.weight(1f)
                                )
                                ThemeCard(
                                    title = "Norton",
                                    color = Color(0xFF003865),
                                    isSelected = themePreset == "norton_cyan",
                                    onClick = { themePreset = "norton_cyan" },
                                    modifier = Modifier.weight(1f)
                                )
                                ThemeCard(
                                    title = "Cyber",
                                    color = Color(0xFF7B2CBF),
                                    isSelected = themePreset == "cyberpunk",
                                    onClick = { themePreset = "cyberpunk" },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }

                        item {
                            Text(
                                text = "FONT SIZE (${fontSize.toInt()} SP)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Slider(
                                value = fontSize,
                                onValueChange = { fontSize = it },
                                valueRange = 10f..22f,
                                steps = 5
                            )
                        }

                        item {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }

                        item {
                            Text(
                                text = "PANEL & LAYOUT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Dual Pane Mode", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Tabs (toggleable) or Side-by-Side Split",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (dualPaneMode == "tabs") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { dualPaneMode = "tabs" }
                                    ) {
                                        Text(
                                            text = "Tabs",
                                            color = if (dualPaneMode == "tabs") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (dualPaneMode == "split") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { dualPaneMode = "split" }
                                    ) {
                                        Text(
                                            text = "Split",
                                            color = if (dualPaneMode == "split") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }

                        item {
                            Text(
                                text = "FILE DISPLAY & BEHAVIOR",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Show Hidden Files (dotfiles)")
                                Switch(checked = showHidden, onCheckedChange = { showHidden = it })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Confirm Before Delete")
                                Switch(checked = confirmDelete, onCheckedChange = { confirmDelete = it })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Confirm Before Overwriting")
                                Switch(checked = confirmOverwrite, onCheckedChange = { confirmOverwrite = it })
                            }
                        }
                    }
                } else {
                    // Raw INI file editor
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFF0F111A))
                            .padding(12.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E2230),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Direct editing of totalcmd.ini format. Changes apply live upon saving.",
                                color = Color(0xFF82B1FF),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        BasicTextField(
                            value = rawIniText,
                            onValueChange = { rawIniText = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFFE0E0E0),
                                lineHeight = 18.sp
                            ),
                            cursorBrush = SolidColor(Color(0xFF82B1FF)),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("raw_ini_editor_input")
                        )
                    }
                }

                // Bottom Action Button
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (selectedTab == 0) {
                                    val updated = currentConfig.copy(
                                        themePreset = themePreset,
                                        fontSize = fontSize.toInt(),
                                        showHiddenFiles = showHidden,
                                        confirmDelete = confirmDelete,
                                        confirmOverwrite = confirmOverwrite,
                                        displayMode = displayMode,
                                        dualPaneMode = dualPaneMode
                                    )
                                    onSaveConfig(updated)
                                } else {
                                    val parsed = IniConfigManager.parseIni(rawIniText)
                                    iniFile.writeText(rawIniText)
                                    onSaveConfig(parsed)
                                }
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply Settings")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    title: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
