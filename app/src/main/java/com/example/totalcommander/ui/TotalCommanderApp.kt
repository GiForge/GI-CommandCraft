package com.example.totalcommander.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.totalcommander.model.FileItem
import com.example.totalcommander.model.PanelId
import com.example.totalcommander.ui.components.BottomFKeysBar
import com.example.totalcommander.ui.components.DualPanelLayout
import com.example.totalcommander.ui.components.TopCommanderBar
import com.example.totalcommander.ui.dialogs.AdvancedSearchDialog
import com.example.totalcommander.ui.dialogs.ChecksumDialog
import com.example.totalcommander.ui.dialogs.CopyConfirmDialog
import com.example.totalcommander.ui.dialogs.DeleteConfirmDialog
import com.example.totalcommander.ui.dialogs.DriveSelectorDialog
import com.example.totalcommander.ui.dialogs.ContextMenuDialog
import com.example.totalcommander.ui.dialogs.FilePropertiesDialog
import com.example.totalcommander.ui.dialogs.ImageViewerDialog
import com.example.totalcommander.ui.dialogs.IniConfigDialog
import com.example.totalcommander.ui.dialogs.MoveConfirmDialog
import com.example.totalcommander.ui.dialogs.NewFileDialog
import com.example.totalcommander.ui.dialogs.NewFolderDialog
import com.example.totalcommander.ui.dialogs.PluginManagerDialog
import com.example.totalcommander.ui.dialogs.RenameDialog
import com.example.totalcommander.ui.dialogs.TerminalSheet
import com.example.totalcommander.ui.dialogs.TextViewerEditorDialog
import com.example.totalcommander.ui.dialogs.ZipCompressDialog
import com.example.totalcommander.viewmodel.TotalCommanderViewModel
import kotlinx.coroutines.flow.collectLatest
import java.io.File

@Composable
fun TotalCommanderApp(viewModel: TotalCommanderViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasStoragePermission by viewModel.hasStoragePermission.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkStoragePermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val config by viewModel.config.collectAsState()
    val activePanelId by viewModel.activePanel.collectAsState()
    val leftPanel by viewModel.leftPanel.collectAsState()
    val rightPanel by viewModel.rightPanel.collectAsState()
    val isSplitView by viewModel.isSplitView.collectAsState()
    val terminalLines by viewModel.terminalLines.collectAsState()

    // Modals
    val activeImage by viewModel.activeImageViewer.collectAsState()
    val activeText by viewModel.activeTextViewer.collectAsState()
    val activeEditor by viewModel.activeTextEditor.collectAsState()
    val activeTerm by viewModel.activeTerminalSheet.collectAsState()
    val activeSearch by viewModel.activeSearchDialog.collectAsState()
    val activeConfig by viewModel.activeConfigDialog.collectAsState()
    val activePlugins by viewModel.activePluginDialog.collectAsState()
    val activeNewDir by viewModel.activeNewDirDialog.collectAsState()
    val activeNewFile by viewModel.activeNewFileDialog.collectAsState()
    val activeDelete by viewModel.activeDeleteDialog.collectAsState()
    val activeCopy by viewModel.activeCopyDialog.collectAsState()
    val activeMove by viewModel.activeMoveDialog.collectAsState()
    val activeZipCompress by viewModel.activeZipCompressDialog.collectAsState()
    val activeChecksum by viewModel.activeChecksumDialog.collectAsState()
    val activeDriveSelector by viewModel.activeDriveSelectorDialog.collectAsState()
    val activeRename by viewModel.activeRenameDialog.collectAsState()
    val activeContextMenu by viewModel.activeContextMenu.collectAsState()
    val activeDetails by viewModel.activeDetailsDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val activePanelState = if (activePanelId == PanelId.LEFT) leftPanel else rightPanel
    val inactivePanelState = if (activePanelId == PanelId.LEFT) rightPanel else leftPanel

    Scaffold(
        topBar = {
            TopCommanderBar(
                activePanelId = activePanelId,
                activePanelState = activePanelState,
                isSplitView = isSplitView,
                onToggleSplitView = { viewModel.toggleSplitView() },
                onSwapPanels = { viewModel.swapPanels() },
                onEqualizePanels = { viewModel.equalizePanels() },
                onNavigateSegment = { path ->
                    if (path == "~") {
                        viewModel.navigateToDirectory(activePanelId, viewModel.fileManager.rootDir.absolutePath)
                    } else {
                        viewModel.navigateToDirectory(activePanelId, path)
                    }
                },
                onReload = { viewModel.loadPanel(activePanelId) },
                onSelectDrive = { viewModel.activeDriveSelectorDialog.value = true },
                onSelectAll = { viewModel.selectAll(activePanelId) },
                onUnselectAll = { viewModel.unselectAll(activePanelId) },
                onInvertSelection = { viewModel.invertSelection(activePanelId) },
                onSortChange = { mode -> viewModel.setSortMode(activePanelId, mode) },
                onOpenConfig = { viewModel.activeConfigDialog.value = true },
                onOpenPlugins = { viewModel.activePluginDialog.value = true },
                onOpenSearch = { viewModel.activeSearchDialog.value = true },
                onOpenTerminal = { viewModel.activeTerminalSheet.value = true }
            )
        },
        bottomBar = {
            BottomFKeysBar(
                selectedCount = activePanelState.selectedCount,
                onF2Rename = {
                    viewModel.openRenameSelected(activePanelId)
                },
                onF3View = {
                    val items = viewModel.getSelectedOrFocusedItems(activePanelId)
                    if (items.isNotEmpty()) {
                        val item = items.first()
                        if (item.isImage) {
                            viewModel.activeImageViewer.value = item
                        } else {
                            viewModel.activeTextViewer.value = item
                        }
                    }
                },
                onF4Edit = {
                    val items = viewModel.getSelectedOrFocusedItems(activePanelId)
                    if (items.isNotEmpty()) {
                        val item = items.first()
                        if (!item.isDirectory && !item.isZipEntry) {
                            viewModel.activeTextEditor.value = item
                        }
                    }
                },
                onF5Copy = {
                    viewModel.activeCopyDialog.value = true
                },
                onF6Move = {
                    viewModel.activeMoveDialog.value = true
                },
                onF7NewFolder = {
                    viewModel.activeNewDirDialog.value = true
                },
                onF8Delete = {
                    viewModel.activeDeleteDialog.value = true
                },
                onF9Terminal = {
                    viewModel.activeTerminalSheet.value = true
                },
                onF10Search = {
                    viewModel.activeSearchDialog.value = true
                },
                onZipCompress = {
                    viewModel.activeZipCompressDialog.value = true
                },
                onChecksum = {
                    val items = viewModel.getSelectedOrFocusedItems(activePanelId)
                    if (items.isNotEmpty() && !items.first().isDirectory) {
                        viewModel.activeChecksumDialog.value = items.first()
                    }
                },
                onPlugins = {
                    viewModel.activePluginDialog.value = true
                },
                onConfig = {
                    viewModel.activeConfigDialog.value = true
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!hasStoragePermission) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Accesso Memoria Smartphone Limitato",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Concedi l'accesso per visualizzare Download, Foto, DCIM e file reali.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Button(
                            onClick = { viewModel.requestStoragePermission(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("grant_storage_perm_btn")
                        ) {
                            Text("Abilita", fontSize = 11.sp)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                DualPanelLayout(
                    activePanelId = activePanelId,
                    leftPanel = leftPanel,
                    rightPanel = rightPanel,
                    isSplitView = isSplitView,
                    displayMode = config.displayMode,
                    onSelectPanel = { id -> viewModel.setActivePanel(id) },
                    onItemSelect = { id, item -> viewModel.onItemSelect(id, item) },
                    onItemAction = { id, item -> viewModel.onItemAction(id, item) },
                    onItemContextMenu = { id, item -> viewModel.onItemContextMenu(id, item) },
                    onSortChange = { id, mode -> viewModel.setSortMode(id, mode) },
                    onGoToHome = { id -> viewModel.goToPhoneStorage(id) }
                )
            }
        }
    }

    // Dialogs & Sheets
    activeImage?.let { imgItem ->
        val folderImages = activePanelState.items.filter { it.isImage }
        ImageViewerDialog(
            initialItem = imgItem,
            allFolderImages = if (folderImages.isNotEmpty()) folderImages else listOf(imgItem),
            onDismiss = { viewModel.activeImageViewer.value = null }
        )
    }

    activeText?.let { txtItem ->
        TextViewerEditorDialog(
            item = txtItem,
            isEditMode = false,
            fileManager = viewModel.fileManager,
            onSave = { },
            onSwitchMode = {
                viewModel.activeTextViewer.value = null
                viewModel.activeTextEditor.value = txtItem
            },
            onDismiss = { viewModel.activeTextViewer.value = null }
        )
    }

    activeEditor?.let { editItem ->
        TextViewerEditorDialog(
            item = editItem,
            isEditMode = true,
            fileManager = viewModel.fileManager,
            onSave = { newContent ->
                File(editItem.path).writeText(newContent)
                viewModel.loadPanel(activePanelId)
            },
            onSwitchMode = { },
            onDismiss = { viewModel.activeTextEditor.value = null }
        )
    }

    if (activeTerm) {
        TerminalSheet(
            terminalLines = terminalLines,
            currentDirectory = File(activePanelState.currentPath),
            commandHistory = viewModel.terminalManager.getHistory(),
            onExecuteCommand = { cmd -> viewModel.runTerminalCommand(cmd) },
            onDismiss = { viewModel.activeTerminalSheet.value = false }
        )
    }

    if (activeSearch) {
        AdvancedSearchDialog(
            currentDir = File(activePanelState.currentPath),
            fileManager = viewModel.fileManager,
            onSelectResult = { result ->
                viewModel.activeSearchDialog.value = false
                if (result.isZipEntry && result.zipArchiveSourcePath != null) {
                    viewModel.onItemClick(activePanelId, FileItem(
                        name = File(result.zipArchiveSourcePath).name,
                        path = result.zipArchiveSourcePath,
                        isDirectory = false,
                        isZipArchive = true
                    ))
                } else if (result.isDirectory) {
                    viewModel.navigateToDirectory(activePanelId, result.path)
                } else if (result.isImage) {
                    viewModel.activeImageViewer.value = result
                } else if (result.isTextOrCode) {
                    viewModel.activeTextViewer.value = result
                }
            },
            onDismiss = { viewModel.activeSearchDialog.value = false }
        )
    }

    if (activePlugins) {
        PluginManagerDialog(
            pluginManager = viewModel.pluginManager,
            onDismiss = { viewModel.activePluginDialog.value = false }
        )
    }

    if (activeConfig) {
        IniConfigDialog(
            currentConfig = config,
            iniFile = viewModel.fileManager.iniFile,
            onSaveConfig = { newConfig -> viewModel.updateConfig(newConfig) },
            onDismiss = { viewModel.activeConfigDialog.value = false }
        )
    }

    if (activeCopy) {
        CopyConfirmDialog(
            items = viewModel.getSelectedOrFocusedItems(activePanelId),
            targetDir = File(inactivePanelState.currentPath),
            onConfirm = {
                viewModel.executeCopy(File(inactivePanelState.currentPath))
                viewModel.activeCopyDialog.value = false
            },
            onDismiss = { viewModel.activeCopyDialog.value = false }
        )
    }

    if (activeMove) {
        MoveConfirmDialog(
            items = viewModel.getSelectedOrFocusedItems(activePanelId),
            targetDir = File(inactivePanelState.currentPath),
            onConfirm = {
                viewModel.executeMove(File(inactivePanelState.currentPath))
                viewModel.activeMoveDialog.value = false
            },
            onDismiss = { viewModel.activeMoveDialog.value = false }
        )
    }

    if (activeDelete) {
        DeleteConfirmDialog(
            items = viewModel.getSelectedOrFocusedItems(activePanelId),
            onConfirm = {
                viewModel.executeDelete()
                viewModel.activeDeleteDialog.value = false
            },
            onDismiss = { viewModel.activeDeleteDialog.value = false }
        )
    }

    if (activeNewDir) {
        NewFolderDialog(
            onConfirm = { name ->
                viewModel.executeCreateDir(name)
                viewModel.activeNewDirDialog.value = false
            },
            onDismiss = { viewModel.activeNewDirDialog.value = false }
        )
    }

    if (activeNewFile) {
        NewFileDialog(
            onConfirm = { name ->
                viewModel.executeCreateFile(name)
                viewModel.activeNewFileDialog.value = false
            },
            onDismiss = { viewModel.activeNewFileDialog.value = false }
        )
    }

    if (activeZipCompress) {
        ZipCompressDialog(
            items = viewModel.getSelectedOrFocusedItems(activePanelId),
            onConfirm = { zipName ->
                viewModel.executeCompressToZip(zipName)
                viewModel.activeZipCompressDialog.value = false
            },
            onDismiss = { viewModel.activeZipCompressDialog.value = false }
        )
    }

    activeChecksum?.let { item ->
        ChecksumDialog(
            item = item,
            fileManager = viewModel.fileManager,
            onDismiss = { viewModel.activeChecksumDialog.value = null }
        )
    }

    if (activeDriveSelector) {
        DriveSelectorDialog(
            fileManager = viewModel.fileManager,
            onSelectDrive = { path ->
                viewModel.navigateToDirectory(activePanelId, path)
            },
            onOpenPlugins = {
                viewModel.activePluginDialog.value = true
            },
            onRequestPermission = {
                viewModel.requestStoragePermission(context)
            },
            onDismiss = { viewModel.activeDriveSelectorDialog.value = false }
        )
    }

    activeRename?.let { item ->
        RenameDialog(
            item = item,
            onDismiss = { viewModel.activeRenameDialog.value = null },
            onConfirm = { newName ->
                viewModel.renameItem(item, newName)
            }
        )
    }

    activeContextMenu?.let { (panelId, item) ->
        val panel = if (panelId == PanelId.LEFT) leftPanel else rightPanel
        val isSelected = item.path in panel.selectedPaths

        ContextMenuDialog(
            item = item,
            isSelected = isSelected,
            onDismiss = { viewModel.activeContextMenu.value = null },
            onAction = { viewModel.onItemAction(panelId, item) },
            onRename = { viewModel.activeRenameDialog.value = item },
            onView = {
                if (item.isImage) {
                    viewModel.activeImageViewer.value = item
                } else {
                    viewModel.activeTextViewer.value = item
                }
            },
            onEdit = {
                if (!item.isDirectory) {
                    viewModel.activeTextEditor.value = item
                } else {
                    viewModel.notify("Impossibile modificare una cartella con l'editor di testo")
                }
            },
            onCopy = {
                if (item.path !in panel.selectedPaths) {
                    viewModel.toggleSelection(panelId, item.path)
                }
                viewModel.activeCopyDialog.value = true
            },
            onMove = {
                if (item.path !in panel.selectedPaths) {
                    viewModel.toggleSelection(panelId, item.path)
                }
                viewModel.activeMoveDialog.value = true
            },
            onZip = {
                if (item.path !in panel.selectedPaths) {
                    viewModel.toggleSelection(panelId, item.path)
                }
                viewModel.activeZipCompressDialog.value = true
            },
            onChecksum = {
                if (!item.isDirectory) {
                    viewModel.activeChecksumDialog.value = item
                } else {
                    viewModel.notify("Seleziona un file per calcolare il checksum")
                }
            },
            onProperties = {
                viewModel.activeDetailsDialog.value = item
            },
            onToggleSelect = {
                viewModel.toggleSelection(panelId, item.path)
            },
            onDelete = {
                if (item.path !in panel.selectedPaths) {
                    viewModel.toggleSelection(panelId, item.path)
                }
                viewModel.activeDeleteDialog.value = true
            }
        )
    }

    activeDetails?.let { item ->
        FilePropertiesDialog(
            item = item,
            onDismiss = { viewModel.activeDetailsDialog.value = null }
        )
    }
}
