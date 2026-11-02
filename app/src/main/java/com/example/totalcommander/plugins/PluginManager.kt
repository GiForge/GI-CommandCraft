package com.example.totalcommander.plugins

import android.content.Context
import com.example.totalcommander.model.PluginType
import com.example.totalcommander.model.TcPlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class PluginManager(private val context: Context) {

    private val pluginsFile = File(context.filesDir, "Config/plugins.json")

    private val _plugins = MutableStateFlow<List<TcPlugin>>(emptyList())
    val plugins: StateFlow<List<TcPlugin>> = _plugins.asStateFlow()

    init {
        loadPlugins()
    }

    private fun loadPlugins() {
        val defaultList = listOf(
            TcPlugin(
                id = "plugin_cloud",
                name = "Cloud Storage Connector",
                author = "Total Commander Ext Team",
                version = "2.10",
                type = PluginType.STORAGE,
                description = "Seamless integration with Google Drive, Nextcloud, and WebDAV endpoints.",
                isEnabled = true,
                iconKey = "cloud",
                configProps = mapOf(
                    "Service" to "Nextcloud / WebDAV",
                    "Server URL" to "https://cloud.example.org/remote.php/webdav",
                    "Status" to "Online (Synced)"
                )
            ),
            TcPlugin(
                id = "plugin_ftp",
                name = "FTP & SFTP Client",
                author = "C. Ghisler & Co.",
                version = "3.24",
                type = PluginType.NETWORK,
                description = "Connect to remote FTP and SSH File Transfer Protocol (SFTP) hosts with SSL/TLS.",
                isEnabled = true,
                iconKey = "ftp",
                configProps = mapOf(
                    "Default Port" to "22 (SFTP) / 21 (FTP)",
                    "Encoding" to "UTF-8",
                    "Passive Mode" to "Enabled"
                )
            ),
            TcPlugin(
                id = "plugin_lan",
                name = "LAN / Samba (SMB) Network",
                author = "Total Commander Community",
                version = "3.11",
                type = PluginType.NETWORK,
                description = "Browse Windows shares, NAS servers, and local Samba network shares.",
                isEnabled = true,
                iconKey = "lan",
                configProps = mapOf(
                    "Workgroup" to "WORKGROUP",
                    "SMB Version" to "SMB 2.1 / 3.0",
                    "Auto-Discovery" to "Enabled"
                )
            ),
            TcPlugin(
                id = "plugin_checksum",
                name = "Hash & Checksum Utility",
                author = "Security Tools",
                version = "1.50",
                type = PluginType.TOOL,
                description = "Calculate and verify CRC32, MD5, SHA-1, and SHA-256 hashes against checksum files.",
                isEnabled = true,
                iconKey = "checksum",
                configProps = mapOf(
                    "Algorithms" to "CRC32, MD5, SHA-256",
                    "Auto-Verify .md5" to "Yes"
                )
            ),
            TcPlugin(
                id = "plugin_apk",
                name = "APK & Package Inspector",
                author = "Android Tools",
                version = "1.20",
                type = PluginType.PACKER,
                description = "Decompile AndroidManifest.xml and inspect classes.dex and resource tables inside APK files.",
                isEnabled = true,
                iconKey = "apk",
                configProps = mapOf(
                    "Show Permissions" to "Enabled",
                    "Parse Manifest" to "Enabled"
                )
            )
        )

        try {
            if (pluginsFile.exists()) {
                val jsonStr = pluginsFile.readText()
                val jsonArray = JSONArray(jsonStr)
                val loaded = mutableListOf<TcPlugin>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val propsObj = obj.optJSONObject("configProps")
                    val props = mutableMapOf<String, String>()
                    propsObj?.keys()?.forEach { k ->
                        props[k] = propsObj.optString(k)
                    }
                    loaded.add(
                        TcPlugin(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            author = obj.getString("author"),
                            version = obj.getString("version"),
                            type = PluginType.valueOf(obj.optString("type", "TOOL")),
                            description = obj.getString("description"),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            iconKey = obj.optString("iconKey", "plugin"),
                            configProps = props
                        )
                    )
                }
                if (loaded.isNotEmpty()) {
                    _plugins.value = loaded
                    return
                }
            }
        } catch (_: Exception) {
        }

        _plugins.value = defaultList
        savePlugins(defaultList)
    }

    private fun savePlugins(list: List<TcPlugin>) {
        try {
            pluginsFile.parentFile?.mkdirs()
            val arr = JSONArray()
            list.forEach { p ->
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("author", p.author)
                    put("version", p.version)
                    put("type", p.type.name)
                    put("description", p.description)
                    put("isEnabled", p.isEnabled)
                    put("iconKey", p.iconKey)
                    val pObj = JSONObject()
                    p.configProps.forEach { (k, v) -> pObj.put(k, v) }
                    put("configProps", pObj)
                }
                arr.put(obj)
            }
            pluginsFile.writeText(arr.toString(2))
        } catch (_: Exception) {
        }
    }

    fun togglePlugin(id: String, enabled: Boolean) {
        val updated = _plugins.value.map {
            if (it.id == id) it.copy(isEnabled = enabled) else it
        }
        _plugins.value = updated
        savePlugins(updated)
    }

    fun addCustomPlugin(
        name: String,
        author: String,
        version: String,
        type: PluginType,
        description: String,
        config: Map<String, String>
    ) {
        val newPlugin = TcPlugin(
            id = "plugin_custom_" + System.currentTimeMillis(),
            name = name,
            author = author.ifBlank { "Third-Party Developer" },
            version = version.ifBlank { "1.0.0" },
            type = type,
            description = description.ifBlank { "Custom external Total Commander plugin extension." },
            isEnabled = true,
            iconKey = "custom",
            configProps = config
        )
        val updated = _plugins.value + newPlugin
        _plugins.value = updated
        savePlugins(updated)
    }

    fun removePlugin(id: String) {
        val updated = _plugins.value.filter { it.id != id }
        _plugins.value = updated
        savePlugins(updated)
    }
}
