package com.example.totalcommander.config

import com.example.totalcommander.model.TcConfiguration
import java.io.File

object IniConfigManager {

    fun generateDefaultIni(): String {
        return """
; Total Commander for Mobile Configuration File
; Editable manually or via Total Commander Configuration Dialog

[Configuration]
FontSize=14
ThemePreset=classic_blue
ShowHiddenFiles=1
ShowSystemDrives=1
ConfirmDelete=1
ConfirmOverwrite=1
DisplayMode=detailed
CaseSensitiveSort=0

[Layout]
SplitRatio=0.50
DualPaneMode=tabs

[Buttonbar]
VisibleButtons=VIEW,EDIT,COPY,MOVE,NEW_DIR,DELETE,ZIP,TERM,SEARCH,CONFIG

[Plugins]
CloudDrive=1
FtpSftp=1
LanSamba=1
ChecksumUtil=1
ApkInspector=1
        """.trimIndent()
    }

    fun parseIni(content: String): TcConfiguration {
        var currentSection = ""
        val sectionMap = mutableMapOf<String, MutableMap<String, String>>()

        content.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith(";") || trimmed.startsWith("#")) {
                return@forEach
            }
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                currentSection = trimmed.substring(1, trimmed.length - 1).trim().lowercase()
                sectionMap.putIfAbsent(currentSection, mutableMapOf())
            } else if (currentSection.isNotEmpty() && trimmed.contains("=")) {
                val parts = trimmed.split("=", limit = 2)
                val key = parts[0].trim().lowercase()
                val value = parts[1].trim()
                sectionMap[currentSection]?.put(key, value)
            }
        }

        val configSec = sectionMap["configuration"] ?: emptyMap()
        val layoutSec = sectionMap["layout"] ?: emptyMap()
        val buttonSec = sectionMap["buttonbar"] ?: emptyMap()

        val fontSize = configSec["fontsize"]?.toIntOrNull() ?: 14
        val themePreset = configSec["themepreset"] ?: "classic_blue"
        val showHiddenFiles = configSec["showhiddenfiles"]?.let { it == "1" || it.equals("true", true) } ?: true
        val showSystemDrives = configSec["showsystemdrives"]?.let { it == "1" || it.equals("true", true) } ?: true
        val confirmDelete = configSec["confirmdelete"]?.let { it == "1" || it.equals("true", true) } ?: true
        val confirmOverwrite = configSec["confirmoverwrite"]?.let { it == "1" || it.equals("true", true) } ?: true
        val displayMode = configSec["displaymode"] ?: "detailed"
        val caseSensitiveSort = configSec["casesensitivesort"]?.let { it == "1" || it.equals("true", true) } ?: false

        val splitRatio = layoutSec["splitratio"]?.toFloatOrNull() ?: 0.5f
        val dualPaneMode = layoutSec["dualpanemode"] ?: "tabs"

        val buttonsStr = buttonSec["visiblebuttons"]
        val buttonsList = if (!buttonsStr.isNullOrBlank()) {
            buttonsStr.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }
        } else {
            listOf("VIEW", "EDIT", "COPY", "MOVE", "NEW_DIR", "DELETE", "ZIP", "TERM", "SEARCH", "CONFIG")
        }

        return TcConfiguration(
            fontSize = fontSize.coerceIn(10, 24),
            themePreset = themePreset,
            showHiddenFiles = showHiddenFiles,
            showSystemDrives = showSystemDrives,
            confirmDelete = confirmDelete,
            confirmOverwrite = confirmOverwrite,
            displayMode = displayMode,
            caseSensitiveSort = caseSensitiveSort,
            splitRatio = splitRatio.coerceIn(0.2f, 0.8f),
            dualPaneMode = dualPaneMode,
            visibleButtons = buttonsList
        )
    }

    fun serializeIni(config: TcConfiguration): String {
        val buttons = config.visibleButtons.joinToString(",")
        return """
; Total Commander for Mobile Configuration File
; Automatically updated on ${java.util.Date()}

[Configuration]
FontSize=${config.fontSize}
ThemePreset=${config.themePreset}
ShowHiddenFiles=${if (config.showHiddenFiles) "1" else "0"}
ShowSystemDrives=${if (config.showSystemDrives) "1" else "0"}
ConfirmDelete=${if (config.confirmDelete) "1" else "0"}
ConfirmOverwrite=${if (config.confirmOverwrite) "1" else "0"}
DisplayMode=${config.displayMode}
CaseSensitiveSort=${if (config.caseSensitiveSort) "1" else "0"}

[Layout]
SplitRatio=${String.format(java.util.Locale.US, "%.2f", config.splitRatio)}
DualPaneMode=${config.dualPaneMode}

[Buttonbar]
VisibleButtons=$buttons

[Plugins]
CloudDrive=1
FtpSftp=1
LanSamba=1
ChecksumUtil=1
ApkInspector=1
        """.trimIndent()
    }

    fun loadFromFile(file: File): TcConfiguration {
        return try {
            if (file.exists()) {
                parseIni(file.readText())
            } else {
                val defaultContent = generateDefaultIni()
                file.parentFile?.mkdirs()
                file.writeText(defaultContent)
                parseIni(defaultContent)
            }
        } catch (e: Exception) {
            TcConfiguration()
        }
    }

    fun saveToFile(file: File, config: TcConfiguration) {
        try {
            file.parentFile?.mkdirs()
            file.writeText(serializeIni(config))
        } catch (_: Exception) {
        }
    }
}
