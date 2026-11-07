package com.example.totalcommander.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.totalcommander.model.FileItem
import java.io.File
import java.util.Locale

object FileShareHelper {

    fun getMimeType(file: File): String {
        val extension = file.extension.lowercase(Locale.getDefault())
        if (extension.isEmpty()) return "*/*"

        // Common Android and document formats
        return when (extension) {
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "pdf" -> "application/pdf"
            "txt", "log", "ini", "conf", "cfg" -> "text/plain"
            "html", "htm" -> "text/html"
            "json" -> "application/json"
            "xml" -> "text/xml"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "svg" -> "image/svg+xml"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "flac" -> "audio/flac"
            "m4a" -> "audio/mp4"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "avi" -> "video/x-msvideo"
            "zip" -> "application/zip"
            "apk" -> "application/vnd.android.package-archive"
            else -> {
                val fromMap = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                fromMap ?: "*/*"
            }
        }
    }

    private fun getFileUri(context: Context, file: File): Uri {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (_: Exception) {
            Uri.fromFile(file)
        }
    }

    /**
     * Apri con un'app esterna (es. Word per file .doc, Acrobat per .pdf, lettore multimediale per video, ecc.)
     */
    fun openWith(context: Context, file: File, onError: (String) -> Unit) {
        if (!file.exists()) {
            onError("Il file non esiste: ${file.name}")
            return
        }

        try {
            val uri = getFileUri(context, file)
            val mimeType = getMimeType(file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Apri '${file.name}' con...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            onError("Nessuna app installata per aprire file di tipo '${file.extension}'")
        } catch (e: Exception) {
            onError("Errore durante l'apertura: ${e.localizedMessage}")
        }
    }

    /**
     * Condividi file singolo tramite Bluetooth o altre app (Quick Share, Gmail, WhatsApp, ecc.)
     */
    fun shareSingle(context: Context, file: File, onError: (String) -> Unit) {
        if (!file.exists()) {
            onError("Il file non esiste: ${file.name}")
            return
        }

        try {
            val uri = getFileUri(context, file)
            val mimeType = getMimeType(file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                putExtra(Intent.EXTRA_TEXT, "Condivisione file: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Condividi '${file.name}' (Bluetooth / App)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            onError("Nessuna applicazione disponibile per la condivisione")
        } catch (e: Exception) {
            onError("Errore durante la condivisione: ${e.localizedMessage}")
        }
    }

    /**
     * Condividi file multipli tramite Bluetooth o altre app
     */
    fun shareMultiple(context: Context, files: List<File>, onError: (String) -> Unit) {
        val validFiles = files.filter { it.exists() && !it.isDirectory }
        if (validFiles.isEmpty()) {
            onError("Nessun file valido da condividere")
            return
        }

        if (validFiles.size == 1) {
            shareSingle(context, validFiles.first(), onError)
            return
        }

        try {
            val uris = ArrayList<Uri>()
            validFiles.forEach { f ->
                uris.add(getFileUri(context, f))
            }

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                putExtra(Intent.EXTRA_SUBJECT, "Condivisione ${validFiles.size} file")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Condividi ${validFiles.size} file selezionati (Bluetooth / App)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            onError("Errore durante la condivisione multipla: ${e.localizedMessage}")
        }
    }
}
