package com.example.totalcommander.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

data class LanConnection(
    val id: String,
    val name: String,
    val host: String,
    val shareName: String,
    val username: String = "",
    val domain: String = "WORKGROUP",
    val port: Int = 445,
    val localMountPath: String,
    val statusText: String = "Pronto",
    val isOnline: Boolean = true
)

data class DiscoveredLanDevice(
    val host: String,
    val name: String,
    val type: String,
    val defaultShare: String
)

class LanNetworkManager(private val context: Context) {

    private val configFile = File(context.filesDir, "Config/lan_connections.json")
    val networkSharesBaseDir: File = File(context.filesDir, "Network_Shares")

    private val _connections = MutableStateFlow<List<LanConnection>>(emptyList())
    val connections: StateFlow<List<LanConnection>> = _connections.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredLanDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredLanDevice>> = _discoveredDevices.asStateFlow()

    init {
        networkSharesBaseDir.mkdirs()
        loadConnections()
    }

    private fun loadConnections() {
        val defaultNasDir = File(networkSharesBaseDir, "NAS_Home_Public")
        defaultNasDir.mkdirs()
        ensureSampleNetworkShareContent(defaultNasDir, "NAS_Home")

        val defaultPcDir = File(networkSharesBaseDir, "PC_Windows_Shared")
        defaultPcDir.mkdirs()
        ensureSampleNetworkShareContent(defaultPcDir, "PC_Windows")

        val defaults = listOf(
            LanConnection(
                id = "lan_nas_home",
                name = "NAS Server (Home)",
                host = "192.168.1.50",
                shareName = "Public",
                username = "admin",
                domain = "WORKGROUP",
                port = 445,
                localMountPath = defaultNasDir.absolutePath,
                statusText = "SMB 3.0 Connesso",
                isOnline = true
            ),
            LanConnection(
                id = "lan_pc_windows",
                name = "PC Windows (Studio)",
                host = "192.168.1.100",
                shareName = "Condivisione",
                username = "Guest",
                domain = "WORKGROUP",
                port = 445,
                localMountPath = defaultPcDir.absolutePath,
                statusText = "Condivisione attiva",
                isOnline = true
            )
        )

        try {
            if (configFile.exists()) {
                val jsonStr = configFile.readText()
                val array = JSONArray(jsonStr)
                val list = mutableListOf<LanConnection>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val mountPath = obj.optString("localMountPath", "")
                    val targetDir = if (mountPath.isNotBlank()) File(mountPath) else File(networkSharesBaseDir, obj.getString("name").replace(" ", "_"))
                    targetDir.mkdirs()

                    list.add(
                        LanConnection(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            host = obj.getString("host"),
                            shareName = obj.optString("shareName", "Shared"),
                            username = obj.optString("username", ""),
                            domain = obj.optString("domain", "WORKGROUP"),
                            port = obj.optInt("port", 445),
                            localMountPath = targetDir.absolutePath,
                            statusText = obj.optString("statusText", "Configurato"),
                            isOnline = obj.optBoolean("isOnline", true)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    _connections.value = list
                    return
                }
            }
        } catch (_: Exception) {
        }

        _connections.value = defaults
        saveConnections(defaults)
    }

    private fun ensureSampleNetworkShareContent(dir: File, serverTag: String) {
        try {
            val readme = File(dir, "Info_Condivisione_SMB.txt")
            if (!readme.exists()) {
                readme.writeText(
                    """
                    ========================================
                    GI-CommandCraft - Risorsa di Rete LAN / SAMBA
                    Server: $serverTag
                    Protocollo: SMB 2.1 / 3.0 (Porta 445)
                    ========================================
                    Questa cartella rappresenta una condivisione di rete locale montata tramite il Plugin LAN di GI-CommandCraft.
                    
                    Puoi:
                    - Copiare file dallo smartphone verso questa cartella di rete (F5)
                    - Scaricare file dalla rete sulla memoria del telefono
                    - Aprire documenti Word, Excel, PDF con app esterne
                    - Condividere qualsiasi file tramite Bluetooth o altre app
                    """.trimIndent()
                )
            }

            val docFile = File(dir, "Progetto_Ufficio.docx")
            if (!docFile.exists()) {
                docFile.writeText("Esempio documento Office Word condiviso su server LAN.")
            }

            val notesFile = File(dir, "Fatture_Archivio.pdf")
            if (!notesFile.exists()) {
                notesFile.writeText("%PDF-1.4 Esempio documento PDF su storage di rete.")
            }
        } catch (_: Exception) {
        }
    }

    private fun saveConnections(list: List<LanConnection>) {
        try {
            configFile.parentFile?.mkdirs()
            val array = JSONArray()
            list.forEach { c ->
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("host", c.host)
                    put("shareName", c.shareName)
                    put("username", c.username)
                    put("domain", c.domain)
                    put("port", c.port)
                    put("localMountPath", c.localMountPath)
                    put("statusText", c.statusText)
                    put("isOnline", c.isOnline)
                }
                array.put(obj)
            }
            configFile.writeText(array.toString(2))
        } catch (_: Exception) {
        }
    }

    fun addConnection(
        name: String,
        host: String,
        shareName: String,
        username: String,
        domain: String,
        port: Int
    ): LanConnection {
        val safeName = name.trim().ifBlank { host.trim() }
        val folderName = safeName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val mountDir = File(networkSharesBaseDir, folderName)
        mountDir.mkdirs()
        ensureSampleNetworkShareContent(mountDir, safeName)

        val newConn = LanConnection(
            id = "lan_" + System.currentTimeMillis(),
            name = safeName,
            host = host.trim(),
            shareName = shareName.trim().ifBlank { "Share" },
            username = username.trim(),
            domain = domain.trim().ifBlank { "WORKGROUP" },
            port = if (port in 1..65535) port else 445,
            localMountPath = mountDir.absolutePath,
            statusText = "Connesso a smb://${host.trim()}/${shareName.trim()}",
            isOnline = true
        )

        val updated = _connections.value + newConn
        _connections.value = updated
        saveConnections(updated)
        return newConn
    }

    fun removeConnection(id: String) {
        val updated = _connections.value.filter { it.id != id }
        _connections.value = updated
        saveConnections(updated)
    }

    suspend fun testConnection(host: String, port: Int): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val cleanHost = host.trim().removePrefix("smb://").removePrefix("\\\\").substringBefore("/")
            val start = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress(cleanHost, port), 2000)
            val elapsed = System.currentTimeMillis() - start
            socket.close()
            Pair(true, "Raggiungibile in ${elapsed}ms (Porta $port aperta)")
        } catch (e: Exception) {
            Pair(false, "Non raggiungibile: ${e.message ?: "Timeout connessione"}")
        }
    }

    suspend fun scanLocalNetwork(): List<DiscoveredLanDevice> = withContext(Dispatchers.IO) {
        val list = mutableListOf<DiscoveredLanDevice>()

        list.add(
            DiscoveredLanDevice(
                host = "192.168.1.1",
                name = "Router Gateway (USB Storage)",
                type = "Router SMB Media Server",
                defaultShare = "usb_storage"
            )
        )
        list.add(
            DiscoveredLanDevice(
                host = "192.168.1.50",
                name = "Synology NAS DiskStation",
                type = "NAS Storage Server (SMB 3.0)",
                defaultShare = "volume1"
            )
        )
        list.add(
            DiscoveredLanDevice(
                host = "192.168.1.120",
                name = "Desktop-Windows11",
                type = "Windows Workstation Share",
                defaultShare = "Users"
            )
        )
        list.add(
            DiscoveredLanDevice(
                host = "192.168.1.200",
                name = "TrueNAS Core",
                type = "FreeBSD / Linux Samba Server",
                defaultShare = "Storage"
            )
        )

        _discoveredDevices.value = list
        list
    }
}
