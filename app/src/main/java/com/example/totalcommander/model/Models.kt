package com.example.totalcommander.model

import java.io.File

enum class PanelId {
    LEFT, RIGHT
}

enum class SortMode(val label: String) {
    NAME("Name"),
    EXT("Extension"),
    SIZE("Size"),
    DATE("Date")
}

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val isZipArchive: Boolean = false,
    val isZipEntry: Boolean = false,
    val zipArchiveSourcePath: String? = null,
    val zipEntryInternalPath: String? = null,
    val size: Long = 0L,
    val lastModified: Long = 0L,
    val extension: String = "",
    val permissions: String = "rw-r--r--",
    val mimeType: String = "*/*",
    val isParentDir: Boolean = false
) {
    val isImage: Boolean
        get() = extension.lowercase() in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg")

    val isTextOrCode: Boolean
        get() = extension.lowercase() in listOf("txt", "ini", "log", "json", "xml", "kt", "java", "sh", "properties", "md", "csv", "html", "css", "js")

    val formattedSize: String
        get() {
            if (isDirectory && !isZipEntry) return "<DIR>"
            if (isZipEntry && isDirectory) return "<DIR>"
            if (size < 1024) return "$size B"
            val kb = size / 1024.0
            if (kb < 1024) return String.format("%.1f KB", kb)
            val mb = kb / 1024.0
            if (mb < 1024) return String.format("%.1f MB", mb)
            val gb = mb / 1024.0
            return String.format("%.2f GB", gb)
        }
}

data class PanelState(
    val id: PanelId,
    val currentPath: String,
    val history: List<String> = emptyList(),
    val historyIndex: Int = 0,
    val items: List<FileItem> = emptyList(),
    val selectedPaths: Set<String> = emptySet(),
    val sortMode: SortMode = SortMode.NAME,
    val sortAscending: Boolean = true,
    val filterQuery: String = "",
    val isZipMode: Boolean = false,
    val zipArchiveFile: File? = null,
    val zipInternalPath: String = "",
    val isLoading: Boolean = false
) {
    val canGoBack: Boolean get() = historyIndex > 0
    val canGoForward: Boolean get() = historyIndex < history.size - 1

    val selectedCount: Int get() = selectedPaths.size

    val selectedTotalSize: Long
        get() = items.filter { it.path in selectedPaths && !it.isDirectory }.sumOf { it.size }
}

enum class PluginType(val title: String) {
    STORAGE("Cloud Storage"),
    NETWORK("Network (FTP/SFTP/LAN)"),
    TOOL("Utility & Checksum"),
    PACKER("Archive / Packer")
}

data class TcPlugin(
    val id: String,
    val name: String,
    val author: String,
    val version: String,
    val type: PluginType,
    val description: String,
    val isEnabled: Boolean = true,
    val iconKey: String = "plugin",
    val configProps: Map<String, String> = emptyMap()
)

data class TcConfiguration(
    // [Configuration]
    val fontSize: Int = 14,
    val themePreset: String = "classic_blue", // classic_blue, modern_dark, norton_cyan, cyberpunk
    val showHiddenFiles: Boolean = true,
    val showSystemDrives: Boolean = true,
    val confirmDelete: Boolean = true,
    val confirmOverwrite: Boolean = true,
    val displayMode: String = "detailed", // detailed, compact, icons
    val caseSensitiveSort: Boolean = false,

    // [Layout]
    val splitRatio: Float = 0.5f,
    val dualPaneMode: String = "tabs", // tabs, split

    // [Buttonbar]
    val visibleButtons: List<String> = listOf("VIEW", "EDIT", "COPY", "MOVE", "NEW_DIR", "DELETE", "ZIP", "TERM", "SEARCH", "CONFIG")
)
