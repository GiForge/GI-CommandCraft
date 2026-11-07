package com.example.totalcommander.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.totalcommander.config.IniConfigManager
import com.example.totalcommander.fs.FileManager
import com.example.totalcommander.model.FileItem
import com.example.totalcommander.model.PanelId
import com.example.totalcommander.model.PanelState
import com.example.totalcommander.model.PluginType
import com.example.totalcommander.model.SortMode
import com.example.totalcommander.model.TcConfiguration
import com.example.totalcommander.network.LanNetworkManager
import com.example.totalcommander.plugins.PluginManager
import com.example.totalcommander.terminal.TerminalLine
import com.example.totalcommander.terminal.TerminalLineType
import com.example.totalcommander.terminal.TerminalManager
import com.example.totalcommander.util.FileShareHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class TotalCommanderViewModel(application: Application) : AndroidViewModel(application) {

    val fileManager = FileManager(application)
    val pluginManager = PluginManager(application)
    val lanManager = LanNetworkManager(application)
    val terminalManager = TerminalManager(fileManager)

    // Configuration
    private val _config = MutableStateFlow(IniConfigManager.loadFromFile(fileManager.iniFile))
    val config: StateFlow<TcConfiguration> = _config.asStateFlow()

    // Active panel
    private val _activePanel = MutableStateFlow(PanelId.LEFT)
    val activePanel: StateFlow<PanelId> = _activePanel.asStateFlow()

    // Dual pane display mode override or INI
    private val _isSplitView = MutableStateFlow(false)
    val isSplitView: StateFlow<Boolean> = _isSplitView.asStateFlow()

    // Storage permission state
    private val _hasStoragePermission = MutableStateFlow(fileManager.hasAllFilesPermission())
    val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

    fun checkStoragePermission() {
        val granted = fileManager.hasAllFilesPermission()
        _hasStoragePermission.value = granted
        if (granted) {
            loadPanel(PanelId.LEFT)
            loadPanel(PanelId.RIGHT)
        }
    }

    fun requestStoragePermission(context: android.content.Context) {
        fileManager.openPermissionSettings(context)
    }

    // Left panel state (default to smartphone primary internal storage /storage/emulated/0)
    private val _leftPanel = MutableStateFlow(
        PanelState(
            id = PanelId.LEFT,
            currentPath = fileManager.internalStorageDir.absolutePath.let {
                if (File(it).exists()) it else fileManager.rootDir.absolutePath
            },
            history = listOf(
                fileManager.internalStorageDir.absolutePath.let {
                    if (File(it).exists()) it else fileManager.rootDir.absolutePath
                }
            ),
            historyIndex = 0
        )
    )
    val leftPanel: StateFlow<PanelState> = _leftPanel.asStateFlow()

    // Right panel state (initializes to Download or device root /)
    private val _rightPanel = MutableStateFlow(
        PanelState(
            id = PanelId.RIGHT,
            currentPath = fileManager.downloadsDir.absolutePath.let {
                if (File(it).exists()) it else fileManager.systemRootDir.absolutePath
            },
            history = listOf(
                fileManager.downloadsDir.absolutePath.let {
                    if (File(it).exists()) it else fileManager.systemRootDir.absolutePath
                }
            ),
            historyIndex = 0
        )
    )
    val rightPanel: StateFlow<PanelState> = _rightPanel.asStateFlow()

    // Drag and Drop state
    private val _draggedItems = MutableStateFlow<List<FileItem>?>(null)
    val draggedItems: StateFlow<List<FileItem>?> = _draggedItems.asStateFlow()

    // Terminal lines
    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(
        listOf(
            TerminalLine("Total Commander Shell [Version 3.40.1]", TerminalLineType.INFO),
            TerminalLine("Type 'help' to view available commands.", TerminalLineType.INFO)
        )
    )
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    // Active modals / overlays
    val activeImageViewer = MutableStateFlow<FileItem?>(null)
    val activeTextViewer = MutableStateFlow<FileItem?>(null)
    val activeTextEditor = MutableStateFlow<FileItem?>(null)
    val activeTerminalSheet = MutableStateFlow(false)
    val activeSearchDialog = MutableStateFlow(false)
    val activeConfigDialog = MutableStateFlow(false)
    val activePluginDialog = MutableStateFlow(false)
    val activeNewDirDialog = MutableStateFlow(false)
    val activeNewFileDialog = MutableStateFlow(false)
    val activeDeleteDialog = MutableStateFlow(false)
    val activeCopyDialog = MutableStateFlow(false)
    val activeMoveDialog = MutableStateFlow(false)
    val activeZipCompressDialog = MutableStateFlow(false)
    val activeZipExtractDialog = MutableStateFlow(false)
    val activeChecksumDialog = MutableStateFlow<FileItem?>(null)
    val activeDriveSelectorDialog = MutableStateFlow(false)
    val activeRenameDialog = MutableStateFlow<FileItem?>(null)
    val activeContextMenu = MutableStateFlow<Pair<PanelId, FileItem>?>(null)
    val activeDetailsDialog = MutableStateFlow<FileItem?>(null)
    val activeLanDialog = MutableStateFlow(false)

    // Snackbar notifications
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        _isSplitView.value = _config.value.dualPaneMode.equals("split", ignoreCase = true)
        loadPanel(PanelId.LEFT)
        loadPanel(PanelId.RIGHT)
    }

    fun setActivePanel(id: PanelId) {
        _activePanel.value = id
        val panel = getPanelState(id)
        if (!panel.isZipMode) {
            terminalManager.setCurrentDirectory(File(panel.currentPath))
        }
    }

    fun toggleSplitView() {
        val next = !_isSplitView.value
        _isSplitView.value = next
        updateConfig(_config.value.copy(dualPaneMode = if (next) "split" else "tabs"))
    }

    private fun getPanelState(id: PanelId): PanelState {
        return if (id == PanelId.LEFT) _leftPanel.value else _rightPanel.value
    }

    private fun updatePanelState(id: PanelId, transform: (PanelState) -> PanelState) {
        if (id == PanelId.LEFT) {
            _leftPanel.value = transform(_leftPanel.value)
        } else {
            _rightPanel.value = transform(_rightPanel.value)
        }
    }

    fun loadPanel(id: PanelId) {
        viewModelScope.launch {
            val current = getPanelState(id)
            updatePanelState(id) { it.copy(isLoading = true) }

            if (current.isZipMode && current.zipArchiveFile != null) {
                val items = fileManager.listZipArchive(current.zipArchiveFile, current.zipInternalPath)
                updatePanelState(id) {
                    it.copy(
                        items = items,
                        isLoading = false,
                        selectedPaths = emptySet()
                    )
                }
            } else {
                val dir = File(current.currentPath)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val items = fileManager.listDirectory(
                    dir = dir,
                    showHidden = _config.value.showHiddenFiles,
                    sortMode = current.sortMode,
                    sortAscending = current.sortAscending
                )
                updatePanelState(id) {
                    it.copy(
                        items = items,
                        isLoading = false,
                        selectedPaths = emptySet()
                    )
                }
            }
        }
    }

    // Single tap: Selezione del file / directory
    fun onItemSelect(panelId: PanelId, item: FileItem) {
        setActivePanel(panelId)
        if (item.isParentDir) {
            goUp(panelId)
        } else {
            toggleSelection(panelId, item.path)
        }
    }

    // Doppio tap: Avvio azione su file / directory
    fun onItemAction(panelId: PanelId, item: FileItem) {
        setActivePanel(panelId)
        val panel = getPanelState(panelId)

        if (item.isParentDir) {
            goUp(panelId)
            return
        }

        if (panel.isZipMode) {
            if (item.isDirectory) {
                // Enter directory inside zip
                val newInternal = item.zipEntryInternalPath ?: ""
                updatePanelState(panelId) {
                    it.copy(
                        zipInternalPath = newInternal,
                        currentPath = "zip://${panel.zipArchiveFile?.name}/$newInternal"
                    )
                }
                loadPanel(panelId)
            } else if (item.isTextOrCode) {
                activeTextViewer.value = item
            } else {
                notify("Voce archivio: ${item.name} (${item.formattedSize})")
            }
            return
        }

        val file = File(item.path)
        if (item.isZipArchive) {
            enterZipMode(panelId, file)
            return
        }

        if (item.isDirectory) {
            navigateToDirectory(panelId, file.absolutePath)
        } else if (item.isImage) {
            activeImageViewer.value = item
        } else if (item.isTextOrCode) {
            activeTextViewer.value = item
        } else {
            activeTextViewer.value = item
        }
    }

    // Tap prolungato: Apertura menu contestuale con tutte le azioni possibili
    fun onItemContextMenu(panelId: PanelId, item: FileItem) {
        setActivePanel(panelId)
        activeContextMenu.value = Pair(panelId, item)
    }

    // Compatibilità
    fun onItemClick(panelId: PanelId, item: FileItem) = onItemSelect(panelId, item)
    fun onItemLongClick(panelId: PanelId, item: FileItem) = onItemContextMenu(panelId, item)

    fun renameItem(item: FileItem, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed == item.name) {
            activeRenameDialog.value = null
            return
        }
        viewModelScope.launch {
            val file = File(item.path)
            val success = fileManager.renameItem(file, trimmed)
            if (success) {
                notify("Elemento rinominato in '$trimmed'")
                loadPanel(PanelId.LEFT)
                loadPanel(PanelId.RIGHT)
            } else {
                notify("Errore: Impossibile rinominare '${item.name}'")
            }
            activeRenameDialog.value = null
        }
    }

    fun openRenameSelected(panelId: PanelId) {
        val panel = getPanelState(panelId)
        val selectedItem = panel.items.firstOrNull { it.path in panel.selectedPaths && !it.isParentDir }
            ?: panel.items.firstOrNull { !it.isParentDir }
        if (selectedItem != null) {
            activeRenameDialog.value = selectedItem
        } else {
            notify("Seleziona prima un file da rinominare")
        }
    }

    fun goToPhoneStorage(panelId: PanelId) {
        navigateToDirectory(panelId, fileManager.internalStorageDir.absolutePath)
    }

    fun shareItem(context: android.content.Context, item: FileItem) {
        if (item.isDirectory) {
            notify("Seleziona uno o più file per la condivisione")
            return
        }
        FileShareHelper.shareSingle(context, File(item.path)) { err ->
            notify(err)
        }
    }

    fun shareSelected(context: android.content.Context, panelId: PanelId) {
        val panel = getPanelState(panelId)
        val selectedFiles = panel.items
            .filter { it.path in panel.selectedPaths && !it.isDirectory && !it.isParentDir }
            .map { File(it.path) }

        if (selectedFiles.isNotEmpty()) {
            FileShareHelper.shareMultiple(context, selectedFiles) { err ->
                notify(err)
            }
        } else {
            val firstFile = panel.items.firstOrNull { !it.isDirectory && !it.isParentDir }
            if (firstFile != null) {
                FileShareHelper.shareSingle(context, File(firstFile.path)) { err ->
                    notify(err)
                }
            } else {
                notify("Nessun file selezionato per la condivisione")
            }
        }
    }

    fun openWithItem(context: android.content.Context, item: FileItem) {
        if (item.isDirectory) {
            notify("Seleziona un file da aprire con un'app esterna")
            return
        }
        FileShareHelper.openWith(context, File(item.path)) { err ->
            notify(err)
        }
    }

    fun openWithSelected(context: android.content.Context, panelId: PanelId) {
        val panel = getPanelState(panelId)
        val target = panel.items.firstOrNull { it.path in panel.selectedPaths && !it.isDirectory && !it.isParentDir }
            ?: panel.items.firstOrNull { !it.isDirectory && !it.isParentDir }

        if (target != null) {
            openWithItem(context, target)
        } else {
            notify("Nessun file selezionato da aprire")
        }
    }

    fun openLanShare(panelId: PanelId, localPath: String) {
        val target = File(localPath)
        if (!target.exists()) target.mkdirs()
        navigateToDirectory(panelId, target.absolutePath)
        notify("Aperta condivisione di rete: ${target.name}")
    }

    private fun enterZipMode(panelId: PanelId, zipFile: File) {
        updatePanelState(panelId) {
            val newHist = it.history.take(it.historyIndex + 1) + "zip://${zipFile.absolutePath}"
            it.copy(
                isZipMode = true,
                zipArchiveFile = zipFile,
                zipInternalPath = "",
                currentPath = "zip://${zipFile.name}/",
                history = newHist,
                historyIndex = newHist.size - 1
            )
        }
        loadPanel(panelId)
    }

    fun exitZipMode(panelId: PanelId) {
        val panel = getPanelState(panelId)
        val targetPath = panel.zipArchiveFile?.parentFile?.absolutePath ?: fileManager.rootDir.absolutePath
        updatePanelState(panelId) {
            val newHist = it.history.take(it.historyIndex + 1) + targetPath
            it.copy(
                isZipMode = false,
                zipArchiveFile = null,
                zipInternalPath = "",
                currentPath = targetPath,
                history = newHist,
                historyIndex = newHist.size - 1
            )
        }
        loadPanel(panelId)
    }

    fun navigateToDirectory(panelId: PanelId, path: String) {
        val target = File(path)
        if (!target.exists()) return

        updatePanelState(panelId) {
            val newHist = it.history.take(it.historyIndex + 1) + target.absolutePath
            it.copy(
                isZipMode = false,
                zipArchiveFile = null,
                zipInternalPath = "",
                currentPath = target.absolutePath,
                history = newHist,
                historyIndex = newHist.size - 1
            )
        }
        terminalManager.setCurrentDirectory(target)
        loadPanel(panelId)
    }

    fun goUp(panelId: PanelId) {
        val panel = getPanelState(panelId)
        if (panel.isZipMode) {
            val currentInternal = panel.zipInternalPath.trim().removeSuffix("/")
            if (currentInternal.isEmpty()) {
                exitZipMode(panelId)
            } else {
                val parentInternal = if (currentInternal.contains("/")) {
                    currentInternal.substringBeforeLast("/")
                } else {
                    ""
                }
                updatePanelState(panelId) {
                    it.copy(
                        zipInternalPath = parentInternal,
                        currentPath = "zip://${panel.zipArchiveFile?.name}/$parentInternal"
                    )
                }
                loadPanel(panelId)
            }
            return
        }

        val current = File(panel.currentPath)
        val parent = current.parentFile
        if (parent != null && parent.exists() && parent.absolutePath != current.absolutePath) {
            navigateToDirectory(panelId, parent.absolutePath)
        } else {
            // At root "/" or no parent -> safely jump to phone internal storage
            navigateToDirectory(panelId, fileManager.internalStorageDir.absolutePath)
        }
    }

    fun goBack(panelId: PanelId) {
        val panel = getPanelState(panelId)
        if (panel.canGoBack) {
            val newIndex = panel.historyIndex - 1
            val path = panel.history[newIndex]
            updatePanelState(panelId) {
                it.copy(
                    currentPath = path,
                    historyIndex = newIndex,
                    isZipMode = false,
                    zipArchiveFile = null
                )
            }
            loadPanel(panelId)
        }
    }

    fun goForward(panelId: PanelId) {
        val panel = getPanelState(panelId)
        if (panel.canForward) {
            val newIndex = panel.historyIndex + 1
            val path = panel.history[newIndex]
            updatePanelState(panelId) {
                it.copy(
                    currentPath = path,
                    historyIndex = newIndex,
                    isZipMode = false,
                    zipArchiveFile = null
                )
            }
            loadPanel(panelId)
        }
    }

    val PanelState.canForward: Boolean get() = historyIndex < history.size - 1

    fun toggleSelection(panelId: PanelId, path: String) {
        updatePanelState(panelId) {
            val set = it.selectedPaths.toMutableSet()
            if (set.contains(path)) set.remove(path) else set.add(path)
            it.copy(selectedPaths = set)
        }
    }

    fun selectAll(panelId: PanelId) {
        updatePanelState(panelId) {
            val allPaths = it.items.filter { item -> !item.isParentDir }.map { item -> item.path }.toSet()
            it.copy(selectedPaths = allPaths)
        }
    }

    fun unselectAll(panelId: PanelId) {
        updatePanelState(panelId) { it.copy(selectedPaths = emptySet()) }
    }

    fun invertSelection(panelId: PanelId) {
        updatePanelState(panelId) {
            val currentSelected = it.selectedPaths
            val inverted = it.items
                .filter { item -> !item.isParentDir }
                .map { item -> item.path }
                .filter { path -> path !in currentSelected }
                .toSet()
            it.copy(selectedPaths = inverted)
        }
    }

    fun setSortMode(panelId: PanelId, mode: SortMode) {
        updatePanelState(panelId) {
            val asc = if (it.sortMode == mode) !it.sortAscending else true
            it.copy(sortMode = mode, sortAscending = asc)
        }
        loadPanel(panelId)
    }

    fun swapPanels() {
        val left = _leftPanel.value
        val right = _rightPanel.value
        _leftPanel.value = right.copy(id = PanelId.LEFT)
        _rightPanel.value = left.copy(id = PanelId.RIGHT)
        loadPanel(PanelId.LEFT)
        loadPanel(PanelId.RIGHT)
        notify("Swapped Left and Right panels")
    }

    fun equalizePanels() {
        val active = getPanelState(_activePanel.value)
        val inactiveId = if (_activePanel.value == PanelId.LEFT) PanelId.RIGHT else PanelId.LEFT
        updatePanelState(inactiveId) {
            it.copy(
                currentPath = active.currentPath,
                isZipMode = active.isZipMode,
                zipArchiveFile = active.zipArchiveFile,
                zipInternalPath = active.zipInternalPath
            )
        }
        loadPanel(inactiveId)
        notify("Panels synchronized to: ${File(active.currentPath).name}")
    }

    // Drag and Drop
    fun startDrag(items: List<FileItem>) {
        _draggedItems.value = items
    }

    fun endDrag() {
        _draggedItems.value = null
    }

    fun handleDrop(sourcePanelId: PanelId, targetPanelId: PanelId, targetFolder: File?, isMove: Boolean) {
        val itemsToDrop = _draggedItems.value ?: return
        endDrag()

        val destDir = targetFolder ?: File(getPanelState(targetPanelId).currentPath)
        if (!destDir.exists() || !destDir.isDirectory) {
            notify("Invalid drop target directory")
            return
        }

        viewModelScope.launch {
            val success = if (isMove) {
                fileManager.moveItems(itemsToDrop, destDir)
            } else {
                fileManager.copyItems(itemsToDrop, destDir)
            }

            if (success) {
                notify((if (isMove) "Moved " else "Copied ") + "${itemsToDrop.size} item(s) to ${destDir.name}")
                loadPanel(PanelId.LEFT)
                loadPanel(PanelId.RIGHT)
            } else {
                notify("Drop operation failed.")
            }
        }
    }

    // File Operations: Copy, Move, Delete, NewDir, NewFile, Zip, Extract
    fun getSelectedOrFocusedItems(panelId: PanelId): List<FileItem> {
        val panel = getPanelState(panelId)
        val selected = panel.items.filter { it.path in panel.selectedPaths }
        return if (selected.isNotEmpty()) {
            selected
        } else {
            // First non-parent item if available
            panel.items.filter { !it.isParentDir }.take(1)
        }
    }

    fun executeCopy(targetDir: File) {
        val panelId = _activePanel.value
        val items = getSelectedOrFocusedItems(panelId)
        if (items.isEmpty()) {
            notify("No items selected to copy")
            return
        }
        viewModelScope.launch {
            val ok = fileManager.copyItems(items, targetDir)
            if (ok) {
                notify("Successfully copied ${items.size} item(s) to ${targetDir.name}")
                loadPanel(PanelId.LEFT)
                loadPanel(PanelId.RIGHT)
                unselectAll(panelId)
            } else {
                notify("Failed to copy items")
            }
        }
    }

    fun executeMove(targetDir: File) {
        val panelId = _activePanel.value
        val items = getSelectedOrFocusedItems(panelId)
        if (items.isEmpty()) {
            notify("No items selected to move")
            return
        }
        viewModelScope.launch {
            val ok = fileManager.moveItems(items, targetDir)
            if (ok) {
                notify("Successfully moved ${items.size} item(s) to ${targetDir.name}")
                loadPanel(PanelId.LEFT)
                loadPanel(PanelId.RIGHT)
                unselectAll(panelId)
            } else {
                notify("Failed to move items")
            }
        }
    }

    fun executeDelete() {
        val panelId = _activePanel.value
        val items = getSelectedOrFocusedItems(panelId)
        if (items.isEmpty()) {
            notify("No items selected to delete")
            return
        }
        viewModelScope.launch {
            val ok = fileManager.deleteItems(items)
            if (ok) {
                notify("Deleted ${items.size} item(s)")
                loadPanel(panelId)
                unselectAll(panelId)
            } else {
                notify("Failed to delete items")
            }
        }
    }

    fun executeCreateDir(name: String) {
        val panel = getPanelState(_activePanel.value)
        if (panel.isZipMode) {
            notify("Cannot create directory inside read-only ZIP view")
            return
        }
        viewModelScope.launch {
            val ok = fileManager.createDirectory(File(panel.currentPath), name)
            if (ok) {
                notify("Created directory: $name")
                loadPanel(_activePanel.value)
            } else {
                notify("Failed to create directory")
            }
        }
    }

    fun executeCreateFile(name: String, content: String = "") {
        val panel = getPanelState(_activePanel.value)
        if (panel.isZipMode) {
            notify("Cannot create file inside ZIP view")
            return
        }
        viewModelScope.launch {
            val ok = fileManager.createFile(File(panel.currentPath), name, content)
            if (ok) {
                notify("Created file: $name")
                loadPanel(_activePanel.value)
            } else {
                notify("Failed to create file")
            }
        }
    }

    fun executeRename(oldFile: File, newName: String) {
        viewModelScope.launch {
            val ok = fileManager.renameItem(oldFile, newName)
            if (ok) {
                notify("Renamed to: $newName")
                loadPanel(_activePanel.value)
            } else {
                notify("Failed to rename")
            }
        }
    }

    fun executeCompressToZip(zipFileName: String) {
        val panelId = _activePanel.value
        val items = getSelectedOrFocusedItems(panelId)
        if (items.isEmpty()) {
            notify("No items selected to compress")
            return
        }

        val targetDir = File(getPanelState(panelId).currentPath)
        val validName = if (zipFileName.endsWith(".zip", ignoreCase = true)) zipFileName else "$zipFileName.zip"
        val zipFile = File(targetDir, validName)

        viewModelScope.launch {
            val ok = fileManager.compressToZip(items, zipFile)
            if (ok) {
                notify("Created ZIP archive: $validName")
                loadPanel(panelId)
                unselectAll(panelId)
            } else {
                notify("ZIP compression failed")
            }
        }
    }

    fun executeExtractZip(zipFile: File, targetDir: File) {
        viewModelScope.launch {
            val ok = fileManager.extractZipArchive(zipFile, targetDir)
            if (ok) {
                notify("Extracted ${zipFile.name} to ${targetDir.name}")
                loadPanel(PanelId.LEFT)
                loadPanel(PanelId.RIGHT)
            } else {
                notify("Extraction failed")
            }
        }
    }

    // Terminal
    fun runTerminalCommand(command: String) {
        if (command.trim() == "clear") {
            _terminalLines.value = emptyList()
            return
        }
        viewModelScope.launch {
            val lines = terminalManager.execute(command)
            _terminalLines.value = _terminalLines.value + lines
            // Sync active panel if terminal changed directory
            val active = getPanelState(_activePanel.value)
            if (terminalManager.currentDir.absolutePath != active.currentPath && !active.isZipMode) {
                navigateToDirectory(_activePanel.value, terminalManager.currentDir.absolutePath)
            }
        }
    }

    // Config & totalcmd.ini
    fun updateConfig(newConfig: TcConfiguration) {
        _config.value = newConfig
        IniConfigManager.saveToFile(fileManager.iniFile, newConfig)
        _isSplitView.value = newConfig.dualPaneMode.equals("split", ignoreCase = true)
        loadPanel(PanelId.LEFT)
        loadPanel(PanelId.RIGHT)
        notify("Saved settings to totalcmd.ini")
    }

    fun reloadConfigFromIni() {
        val reloaded = IniConfigManager.loadFromFile(fileManager.iniFile)
        _config.value = reloaded
        _isSplitView.value = reloaded.dualPaneMode.equals("split", ignoreCase = true)
        loadPanel(PanelId.LEFT)
        loadPanel(PanelId.RIGHT)
        notify("Reloaded totalcmd.ini")
    }

    fun notify(msg: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(msg)
        }
    }
}
