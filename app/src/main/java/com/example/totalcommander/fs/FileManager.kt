package com.example.totalcommander.fs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.totalcommander.model.FileItem
import com.example.totalcommander.model.SortMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class StorageLocation(
    val title: String,
    val subtitle: String,
    val file: File,
    val iconKey: String,
    val isAvailable: Boolean = true
)

class FileManager(private val context: Context) {

    val systemRootDir: File = File("/")
    val internalStorageDir: File = Environment.getExternalStorageDirectory() // e.g. /storage/emulated/0
    val downloadsDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val documentsDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val dcimDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
    val picturesDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val musicDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    val moviesDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
    val appPrivateDir: File = context.filesDir
    val appExternalDir: File? = context.getExternalFilesDir(null)

    // Primary root: real smartphone shared storage /storage/emulated/0
    val rootDir: File
        get() = if (internalStorageDir.exists() && internalStorageDir.canRead()) internalStorageDir else appPrivateDir

    val configDir: File = File(context.filesDir, "Config")
    val iniFile: File = File(configDir, "totalcmd.ini")

    fun hasAllFilesPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun openPermissionSettings(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:" + ctx.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(intent)
            } catch (_: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(intent)
            }
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:" + ctx.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(intent)
        }
    }

    fun getStorageLocations(): List<StorageLocation> {
        val list = mutableListOf<StorageLocation>()

        list.add(
            StorageLocation(
                title = "Memoria Condivisa dello Smartphone",
                subtitle = internalStorageDir.absolutePath,
                file = internalStorageDir,
                iconKey = "phone",
                isAvailable = internalStorageDir.exists()
            )
        )

        list.add(
            StorageLocation(
                title = "Root del File System (/)",
                subtitle = "File system Linux del dispositivo",
                file = systemRootDir,
                iconKey = "system_root",
                isAvailable = true
            )
        )

        list.add(
            StorageLocation(
                title = "Download",
                subtitle = downloadsDir.absolutePath,
                file = downloadsDir,
                iconKey = "download",
                isAvailable = downloadsDir.exists()
            )
        )

        list.add(
            StorageLocation(
                title = "Fotocamera (DCIM)",
                subtitle = dcimDir.absolutePath,
                file = dcimDir,
                iconKey = "camera",
                isAvailable = dcimDir.exists()
            )
        )

        list.add(
            StorageLocation(
                title = "Immagini (Pictures)",
                subtitle = picturesDir.absolutePath,
                file = picturesDir,
                iconKey = "picture",
                isAvailable = picturesDir.exists()
            )
        )

        list.add(
            StorageLocation(
                title = "Documenti (Documents)",
                subtitle = documentsDir.absolutePath,
                file = documentsDir,
                iconKey = "document",
                isAvailable = documentsDir.exists()
            )
        )

        val storageDir = File("/storage")
        if (storageDir.exists() && storageDir.isDirectory) {
            val extraDisks = storageDir.listFiles()?.filter { it.name != "emulated" && it.name != "self" } ?: emptyList()
            extraDisks.forEach { disk ->
                list.add(
                    StorageLocation(
                        title = "Scheda SD / Disco Esterno (${disk.name})",
                        subtitle = disk.absolutePath,
                        file = disk,
                        iconKey = "sd_card",
                        isAvailable = disk.canRead()
                    )
                )
            }
        }

        list.add(
            StorageLocation(
                title = "Dati Sandbox Total Commander",
                subtitle = appPrivateDir.absolutePath,
                file = appPrivateDir,
                iconKey = "app_data",
                isAvailable = true
            )
        )

        return list
    }

    init {
        initializeSampleFilesystem()
    }

    private fun initializeSampleFilesystem() {
        try {
            if (!rootDir.exists()) rootDir.mkdirs()
            configDir.mkdirs()

            // 1. Documents
            val docDir = File(rootDir, "Documents")
            docDir.mkdirs()
            val readmeFile = File(docDir, "Readme_TotalCommander.md")
            if (!readmeFile.exists()) {
                readmeFile.writeText(
                    """
# Total Commander for Android
Version 3.40 (Mobile Edition)

Welcome to the power user's ultimate dual-panel file manager!

### Key Features:
- **Dual Panel Navigation**: Seamless file operations between Panel A and Panel B.
- **F-Keys Toolbar**:
  - F3 View: Integrated Text / Hex Viewer
  - F4 Edit: Fast Text Editor
  - F5 Copy: Copy directly from active to target panel
  - F6 Move: Move items across panels
  - F7 New Dir: Create new folder
  - F8 Delete: Safe deletion with confirmation
  - F9 Terminal: Command line shell with unix utilities
  - F10 Search / Config: Advanced search & totalcmd.ini customization
- **ZIP Archive Browsing**: Enter .zip files just like normal directories!
- **External Plugins**: Cloud storage, FTP/SFTP, LAN Samba, and Checksum calculators.
- **Drag & Drop**: Long-press any file and drag it across tabs/panels.
                    """.trimIndent()
                )
            }

            val specFile = File(docDir, "Project_Specs.txt")
            if (!specFile.exists()) {
                specFile.writeText("Architecture: Jetpack Compose + Clean MVVM\nStorage: Dual Panel Virtual & Local FileSystem\nPlugins: Modular extension provider\nConfig: totalcmd.ini support")
            }

            val reportFile = File(docDir, "Quarterly_Summary.csv")
            if (!reportFile.exists()) {
                reportFile.writeText("Quarter,Revenue,Expenses,Growth\nQ1,120000,85000,+14%\nQ2,145000,90000,+20%\nQ3,168000,98000,+18%\nQ4,210000,105000,+25%")
            }

            // 2. Pictures
            val picDir = File(rootDir, "Pictures")
            picDir.mkdirs()
            createSampleImage(File(picDir, "Banner_Commander.png"), "TOTAL COMMANDER", Color.rgb(21, 101, 192), Color.rgb(255, 213, 79))
            createSampleImage(File(picDir, "Architecture_Diagram.png"), "DUAL PANEL ENGINE", Color.rgb(46, 125, 50), Color.WHITE)
            createSampleImage(File(picDir, "Mobile_Palette.png"), "COLOR THEMES", Color.rgb(106, 27, 154), Color.rgb(178, 235, 242))

            // 3. Scripts
            val scriptsDir = File(rootDir, "Scripts")
            scriptsDir.mkdirs()
            val backupSh = File(scriptsDir, "backup_project.sh")
            if (!backupSh.exists()) {
                backupSh.writeText("#!/bin/sh\n# Total Commander Backup Script\necho 'Starting backup process...'\nmkdir -p ../Archives/Backups\ncp -r ../Documents ../Archives/Backups/\necho 'Backup completed successfully.'\n")
            }

            // 4. Archives with real ZIP
            val archivesDir = File(rootDir, "Archives")
            archivesDir.mkdirs()
            val sampleZip = File(archivesDir, "Sample_Package.zip")
            if (!sampleZip.exists()) {
                createSampleZipArchive(sampleZip)
            }

            // 5. Downloads
            val downloadsDir = File(rootDir, "Downloads")
            downloadsDir.mkdirs()
            val logFile = File(downloadsDir, "system_events.log")
            if (!logFile.exists()) {
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                logFile.writeText("[$now] [INFO] Total Commander mobile service initialized\n[$now] [INFO] Dual panel filesystem mounted\n[$now] [INFO] Plugins registered: Cloud, FTP, LAN, Checksum\n")
            }

        } catch (_: Exception) {
        }
    }

    private fun createSampleImage(file: File, text: String, bgColor: Int, textColor: Int) {
        if (file.exists()) return
        try {
            val width = 640
            val height = 480
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(bgColor)

            val paint = Paint().apply {
                isAntiAlias = true
                color = textColor
                textSize = 36f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText(text, width / 2f, height / 2f - 20, paint)

            val subPaint = Paint().apply {
                isAntiAlias = true
                color = textColor
                alpha = 180
                textSize = 20f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Total Commander Mobile Image Viewer", width / 2f, height / 2f + 40, subPaint)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
        } catch (_: Exception) {
        }
    }

    private fun createSampleZipArchive(zipFile: File) {
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                // Entry 1: notes.txt
                zos.putNextEntry(ZipEntry("Archive_Notes.txt"))
                zos.write("This file is read directly from inside Sample_Package.zip!\nTotal Commander allows seamless exploration of compressed archives.".toByteArray())
                zos.closeEntry()

                // Entry 2: subfolder
                zos.putNextEntry(ZipEntry("Plugins_Docs/"))
                zos.closeEntry()

                // Entry 3: subfolder file
                zos.putNextEntry(ZipEntry("Plugins_Docs/api_specification.md"))
                zos.write("# Total Commander Plugin API\nPlugins can register custom drives, virtual filesystems, and tool commands.".toByteArray())
                zos.closeEntry()
            }
        } catch (_: Exception) {
        }
    }

    fun getVirtualRootItems(): List<FileItem> {
        val items = mutableListOf<FileItem>()

        // 1. Primary smartphone internal shared storage
        if (internalStorageDir.exists()) {
            items.add(
                FileItem(
                    name = "📱 Memoria Smartphone",
                    path = internalStorageDir.absolutePath,
                    isDirectory = true,
                    size = 0L,
                    lastModified = internalStorageDir.lastModified(),
                    permissions = "drwxrwxr-x"
                )
            )
        }

        // 2. Download
        if (downloadsDir.exists()) {
            items.add(
                FileItem(
                    name = "📥 Download",
                    path = downloadsDir.absolutePath,
                    isDirectory = true,
                    size = 0L,
                    lastModified = downloadsDir.lastModified(),
                    permissions = "drwxrwxr-x"
                )
            )
        }

        // 3. DCIM (Camera)
        if (dcimDir.exists()) {
            items.add(
                FileItem(
                    name = "📷 DCIM (Fotocamera)",
                    path = dcimDir.absolutePath,
                    isDirectory = true,
                    size = 0L,
                    lastModified = dcimDir.lastModified(),
                    permissions = "drwxrwxr-x"
                )
            )
        }

        // 4. Documents
        if (documentsDir.exists()) {
            items.add(
                FileItem(
                    name = "📄 Documenti",
                    path = documentsDir.absolutePath,
                    isDirectory = true,
                    size = 0L,
                    lastModified = documentsDir.lastModified(),
                    permissions = "drwxrwxr-x"
                )
            )
        }

        // 5. Pictures
        if (picturesDir.exists()) {
            items.add(
                FileItem(
                    name = "🖼️ Immagini (Pictures)",
                    path = picturesDir.absolutePath,
                    isDirectory = true,
                    size = 0L,
                    lastModified = picturesDir.lastModified(),
                    permissions = "drwxrwxr-x"
                )
            )
        }

        // 6. Music
        if (musicDir.exists()) {
            items.add(
                FileItem(
                    name = "🎵 Musica",
                    path = musicDir.absolutePath,
                    isDirectory = true,
                    size = 0L,
                    lastModified = musicDir.lastModified(),
                    permissions = "drwxrwxr-x"
                )
            )
        }

        // 7. Secondary SD Card / Removable media if mounted
        val storageDir = File("/storage")
        if (storageDir.exists() && storageDir.isDirectory) {
            val extraDisks = storageDir.listFiles()?.filter { it.name != "emulated" && it.name != "self" } ?: emptyList()
            extraDisks.forEach { disk ->
                items.add(
                    FileItem(
                        name = "💾 Scheda SD (${disk.name})",
                        path = disk.absolutePath,
                        isDirectory = true,
                        size = 0L,
                        lastModified = disk.lastModified(),
                        permissions = "drwxr-xr-x"
                    )
                )
            }
        }

        // 8. App Private Storage (Sandbox)
        items.add(
            FileItem(
                name = "📦 Spazio Dati App (Sandbox)",
                path = appPrivateDir.absolutePath,
                isDirectory = true,
                size = 0L,
                lastModified = appPrivateDir.lastModified(),
                permissions = "drwx------"
            )
        )

        // 9. Accessible system root folders if readable
        val sysRoot = File("/")
        val sysFiles = sysRoot.listFiles() ?: emptyArray()
        sysFiles.filter { it.isDirectory && !it.name.startsWith(".") && it.name !in listOf("proc", "acct", "sys") }.forEach {
            items.add(
                FileItem(
                    name = "📁 /${it.name}",
                    path = it.absolutePath,
                    isDirectory = true,
                    size = 0L,
                    lastModified = it.lastModified(),
                    permissions = "dr-xr-xr-x"
                )
            )
        }

        return items
    }

    suspend fun listDirectory(
        dir: File,
        showHidden: Boolean,
        sortMode: SortMode,
        sortAscending: Boolean
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()

        val isSystemRoot = dir.absolutePath == "/" || dir.absolutePath == "/storage" || dir.absolutePath == "/storage/emulated"

        // Always add parent directory item
        if (dir.absolutePath != "/" && dir.parentFile != null) {
            result.add(
                FileItem(
                    name = "..",
                    path = dir.parentFile!!.absolutePath,
                    isDirectory = true,
                    isParentDir = true,
                    size = 0L,
                    lastModified = dir.parentFile!!.lastModified(),
                    permissions = "drwxr-xr-x"
                )
            )
        } else if (dir.absolutePath == "/") {
            // At root "/" -> provide [..] navigation to phone's shared storage so user can never be stuck!
            result.add(
                FileItem(
                    name = ".. (Torna a Memoria Smartphone)",
                    path = internalStorageDir.absolutePath,
                    isDirectory = true,
                    isParentDir = true,
                    size = 0L,
                    lastModified = System.currentTimeMillis(),
                    permissions = "drwxr-xr-x"
                )
            )
        }

        val rawFiles = dir.listFiles() ?: emptyArray()

        val filtered = rawFiles.filter { file ->
            if (!showHidden && file.name.startsWith(".")) false else true
        }

        val items = filtered.map { file ->
            val ext = file.extension.lowercase()
            val isZip = ext == "zip" || ext == "jar" || ext == "apk"
            val perms = buildString {
                append(if (file.isDirectory) "d" else "-")
                append(if (file.canRead()) "r" else "-")
                append(if (file.canWrite()) "w" else "-")
                append(if (file.canExecute()) "x" else "-")
                append("r--")
            }
            FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                isZipArchive = isZip && !file.isDirectory,
                size = if (file.isDirectory) 0L else file.length(),
                lastModified = file.lastModified(),
                extension = ext,
                permissions = perms,
                mimeType = getMimeType(ext)
            )
        }.toMutableList()

        // If at root or restricted system folder where files cannot be listed or is empty,
        // inject the virtual root shortcuts so device storage is always accessible!
        if (isSystemRoot || items.isEmpty()) {
            val rootShortcuts = getVirtualRootItems()
            rootShortcuts.forEach { sc ->
                if (items.none { it.path == sc.path } && result.none { it.path == sc.path }) {
                    items.add(sc)
                }
            }
        }

        // Directories first, then files, sorted by sortMode
        val sorted = items.sortedWith { a, b ->
            if (a.isDirectory != b.isDirectory) {
                if (a.isDirectory) -1 else 1
            } else {
                val cmp = when (sortMode) {
                    SortMode.NAME -> a.name.compareTo(b.name, ignoreCase = true)
                    SortMode.EXT -> a.extension.compareTo(b.extension, ignoreCase = true)
                    SortMode.SIZE -> a.size.compareTo(b.size)
                    SortMode.DATE -> a.lastModified.compareTo(b.lastModified)
                }
                if (sortAscending) cmp else -cmp
            }
        }

        result.addAll(sorted)
        result
    }

    suspend fun listZipArchive(
        zipFile: File,
        internalPath: String
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()

        // Add parent entry
        val normalizedInternal = internalPath.trim().removePrefix("/").removeSuffix("/")
        val parentPath = if (normalizedInternal.contains("/")) {
            normalizedInternal.substringBeforeLast("/")
        } else {
            ""
        }

        result.add(
            FileItem(
                name = "..",
                path = if (normalizedInternal.isEmpty()) zipFile.parentFile?.absolutePath ?: rootDir.absolutePath else "zip://${zipFile.absolutePath}/$parentPath",
                isDirectory = true,
                isParentDir = true,
                isZipEntry = true,
                zipArchiveSourcePath = zipFile.absolutePath,
                zipEntryInternalPath = parentPath
            )
        )

        try {
            ZipFile(zipFile).use { zf ->
                val entries = zf.entries()
                val immediateDirs = mutableSetOf<String>()
                val immediateFiles = mutableListOf<FileItem>()

                val prefix = if (normalizedInternal.isEmpty()) "" else "$normalizedInternal/"

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val entryName = entry.name.removePrefix("/")

                    if (!entryName.startsWith(prefix)) continue

                    val relName = entryName.removePrefix(prefix)
                    if (relName.isEmpty()) continue

                    val slashIndex = relName.indexOf('/')
                    if (slashIndex != -1) {
                        // Sub-directory
                        val dirName = relName.substring(0, slashIndex)
                        if (dirName.isNotEmpty()) {
                            immediateDirs.add(dirName)
                        }
                    } else {
                        // File inside this internal path
                        val ext = relName.substringAfterLast('.', "")
                        immediateFiles.add(
                            FileItem(
                                name = relName,
                                path = "zip://${zipFile.absolutePath}/$prefix$relName",
                                isDirectory = entry.isDirectory,
                                isZipEntry = true,
                                zipArchiveSourcePath = zipFile.absolutePath,
                                zipEntryInternalPath = "$prefix$relName",
                                size = entry.size.coerceAtLeast(0L),
                                lastModified = entry.time,
                                extension = ext,
                                permissions = if (entry.isDirectory) "drwxr-xr-x" else "-rw-r--r--",
                                mimeType = getMimeType(ext)
                            )
                        )
                    }
                }

                // Add directories
                immediateDirs.sorted().forEach { dirName ->
                    result.add(
                        FileItem(
                            name = dirName,
                            path = "zip://${zipFile.absolutePath}/$prefix$dirName",
                            isDirectory = true,
                            isZipEntry = true,
                            zipArchiveSourcePath = zipFile.absolutePath,
                            zipEntryInternalPath = "$prefix$dirName",
                            permissions = "drwxr-xr-x"
                        )
                    )
                }

                // Add files
                immediateFiles.sortBy { it.name.lowercase() }
                result.addAll(immediateFiles)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        result
    }

    suspend fun createDirectory(parentDir: File, name: String): Boolean = withContext(Dispatchers.IO) {
        val newDir = File(parentDir, name)
        newDir.mkdirs()
    }

    suspend fun createFile(parentDir: File, name: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(parentDir, name)
            file.writeText(content)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun renameItem(file: File, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val target = File(file.parentFile, newName)
            file.renameTo(target)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun copyItems(
        sourceItems: List<FileItem>,
        targetDir: File,
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            targetDir.mkdirs()
            sourceItems.forEachIndexed { index, item ->
                onProgress(index + 1, sourceItems.size, item.name)
                if (item.isZipEntry && item.zipArchiveSourcePath != null && item.zipEntryInternalPath != null) {
                    extractSingleZipEntry(File(item.zipArchiveSourcePath), item.zipEntryInternalPath, targetDir)
                } else {
                    val src = File(item.path)
                    val dst = File(targetDir, src.name)
                    if (src.isDirectory) {
                        src.copyRecursively(dst, overwrite = true)
                    } else {
                        src.copyTo(dst, overwrite = true)
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun moveItems(sourceItems: List<FileItem>, targetDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            targetDir.mkdirs()
            sourceItems.forEach { item ->
                val src = File(item.path)
                val dst = File(targetDir, src.name)
                if (src.renameTo(dst)) {
                    // Success
                } else {
                    if (src.isDirectory) {
                        src.copyRecursively(dst, overwrite = true)
                        src.deleteRecursively()
                    } else {
                        src.copyTo(dst, overwrite = true)
                        src.delete()
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteItems(items: List<FileItem>): Boolean = withContext(Dispatchers.IO) {
        try {
            items.forEach { item ->
                val file = File(item.path)
                if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun compressToZip(
        sourceItems: List<FileItem>,
        destZipFile: File,
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            destZipFile.parentFile?.mkdirs()
            ZipOutputStream(BufferedOutputStream(FileOutputStream(destZipFile))).use { zos ->
                sourceItems.forEachIndexed { index, item ->
                    onProgress(index + 1, sourceItems.size, item.name)
                    val src = File(item.path)
                    zipFileOrDir(src, src.name, zos)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun zipFileOrDir(file: File, baseName: String, zos: ZipOutputStream) {
        if (file.isDirectory) {
            val entryName = if (baseName.endsWith("/")) baseName else "$baseName/"
            zos.putNextEntry(ZipEntry(entryName))
            zos.closeEntry()
            file.listFiles()?.forEach { child ->
                zipFileOrDir(child, "$entryName${child.name}", zos)
            }
        } else {
            zos.putNextEntry(ZipEntry(baseName))
            FileInputStream(file).use { fis ->
                fis.copyTo(zos)
            }
            zos.closeEntry()
        }
    }

    suspend fun extractZipArchive(
        zipFile: File,
        targetDir: File,
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            targetDir.mkdirs()
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                var count = 0
                while (entry != null) {
                    count++
                    onProgress(count, -1, entry.name)
                    val outFile = File(targetDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun extractSingleZipEntry(zipFile: File, entryPath: String, targetDir: File) {
        ZipFile(zipFile).use { zf ->
            val entry = zf.getEntry(entryPath) ?: return
            val outName = entryPath.substringAfterLast('/')
            val outFile = File(targetDir, outName)
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                zf.getInputStream(entry).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    suspend fun readZipEntryText(zipFile: File, entryPath: String): String = withContext(Dispatchers.IO) {
        try {
            ZipFile(zipFile).use { zf ->
                val entry = zf.getEntry(entryPath) ?: return@withContext "Entry not found in archive"
                zf.getInputStream(entry).use { it.bufferedReader().readText() }
            }
        } catch (e: Exception) {
            "Error reading entry: ${e.message}"
        }
    }

    suspend fun calculateChecksum(file: File, algorithm: String): String = withContext(Dispatchers.IO) {
        try {
            if (algorithm.uppercase() == "CRC32") {
                val crc = CRC32()
                FileInputStream(file).use { fis ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        crc.update(buffer, 0, bytesRead)
                    }
                }
                return@withContext String.format("%08X", crc.value)
            } else {
                val digest = MessageDigest.getInstance(algorithm)
                FileInputStream(file).use { fis ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                }
                return@withContext digest.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun searchFiles(
        root: File,
        query: String,
        recursive: Boolean,
        searchContent: Boolean,
        minSizeKb: Long?,
        maxSizeKb: Long?,
        dateFilterDays: Int?,
        searchInZips: Boolean
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val matches = mutableListOf<FileItem>()
        val lowerQuery = query.lowercase().trim()
        val now = System.currentTimeMillis()
        val minTime = dateFilterDays?.let { now - (it * 24L * 3600L * 1000L) }

        fun checkMatch(file: File): Boolean {
            if (lowerQuery.isNotEmpty()) {
                val pattern = lowerQuery.replace("*", ".*").replace("?", ".")
                val nameMatches = try {
                    file.name.lowercase().matches(Regex(pattern))
                } catch (_: Exception) {
                    file.name.contains(lowerQuery, ignoreCase = true)
                }

                if (!nameMatches) {
                    if (searchContent && !file.isDirectory && file.length() < 2 * 1024 * 1024) {
                        try {
                            val content = file.readText()
                            if (!content.contains(lowerQuery, ignoreCase = true)) return false
                        } catch (_: Exception) {
                            return false
                        }
                    } else {
                        return false
                    }
                }
            }

            if (minSizeKb != null && !file.isDirectory && file.length() < minSizeKb * 1024) return false
            if (maxSizeKb != null && !file.isDirectory && file.length() > maxSizeKb * 1024) return false
            if (minTime != null && file.lastModified() < minTime) return false

            return true
        }

        fun scanDir(dir: File) {
            val children = dir.listFiles() ?: return
            for (child in children) {
                if (checkMatch(child)) {
                    val ext = child.extension.lowercase()
                    matches.add(
                        FileItem(
                            name = child.name,
                            path = child.absolutePath,
                            isDirectory = child.isDirectory,
                            isZipArchive = !child.isDirectory && (ext == "zip" || ext == "apk" || ext == "jar"),
                            size = if (child.isDirectory) 0L else child.length(),
                            lastModified = child.lastModified(),
                            extension = ext,
                            mimeType = getMimeType(ext)
                        )
                    )
                }

                if (child.isDirectory && recursive) {
                    scanDir(child)
                } else if (!child.isDirectory && searchInZips && child.extension.lowercase() == "zip") {
                    // Search inside zip
                    try {
                        ZipFile(child).use { zf ->
                            val entries = zf.entries()
                            while (entries.hasMoreElements()) {
                                val entry = entries.nextElement()
                                if (!entry.isDirectory && entry.name.contains(lowerQuery, ignoreCase = true)) {
                                    matches.add(
                                        FileItem(
                                            name = "${child.name} > ${entry.name}",
                                            path = "zip://${child.absolutePath}/${entry.name}",
                                            isDirectory = false,
                                            isZipEntry = true,
                                            zipArchiveSourcePath = child.absolutePath,
                                            zipEntryInternalPath = entry.name,
                                            size = entry.size.coerceAtLeast(0L),
                                            lastModified = entry.time,
                                            extension = entry.name.substringAfterLast('.', "")
                                        )
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }

        scanDir(root)
        matches
    }

    private fun getMimeType(extension: String): String = when (extension) {
        "txt", "ini", "log", "md" -> "text/plain"
        "html", "htm" -> "text/html"
        "json" -> "application/json"
        "xml" -> "application/xml"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "bmp" -> "image/bmp"
        "zip" -> "application/zip"
        "apk" -> "application/vnd.android.package-archive"
        "pdf" -> "application/pdf"
        "sh" -> "application/x-sh"
        else -> "*/*"
    }
}
