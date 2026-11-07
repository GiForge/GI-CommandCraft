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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.totalcommander.network.DiscoveredLanDevice
import com.example.totalcommander.network.LanConnection
import com.example.totalcommander.network.LanNetworkManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanNetworkDialog(
    lanManager: LanNetworkManager,
    onExploreShare: (localPath: String) -> Unit,
    onDismiss: () -> Unit
) {
    val connections by lanManager.connections.collectAsState()
    val discoveredDevices by lanManager.discoveredDevices.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var testResultMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var testingId by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lan,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Rete Locale LAN & Samba (SMB)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Accesso a Windows Share, NAS e server di rete",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.testTag("lan_dialog_close_btn")) {
                                Icon(Icons.Default.Close, contentDescription = "Chiudi")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Condivisioni SMB (${connections.size})") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Scansione Rete WiFi") }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Guide banner explaining how the plugin works
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Come accedere a PC e dispositivi in rete locale:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "1. Connetti lo smartphone alla stessa rete WiFi del PC o NAS.\n" +
                                                "2. Inserisci l'IP (es. 192.168.1.100) o seleziona un dispositivo rilevato.\n" +
                                                "3. Premi 'Esplora' per aprire la cartella remota nei pannelli di GI-CommandCraft per copiare, aprire o trasferire file con le consuete funzioni (F5, F6, Condividi, Apri con).",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        if (selectedTab == 0) {
                            if (connections.isEmpty()) {
                                item {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lan,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "Nessuna connessione SMB salvata",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Tocca il pulsante '+' in basso per aggiungere il tuo server o PC.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(connections) { conn ->
                                    LanConnectionCard(
                                        connection = conn,
                                        testStatus = testResultMap[conn.id],
                                        isTesting = testingId == conn.id,
                                        onExplore = {
                                            onExploreShare(conn.localMountPath)
                                            onDismiss()
                                        },
                                        onTest = {
                                            scope.launch {
                                                testingId = conn.id
                                                val (ok, msg) = lanManager.testConnection(conn.host, conn.port)
                                                testResultMap = testResultMap + (conn.id to msg)
                                                testingId = null
                                            }
                                        },
                                        onDelete = {
                                            lanManager.removeConnection(conn.id)
                                        }
                                    )
                                }
                            }
                        } else {
                            // Scansione WiFi
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Dispositivi rilevati in LAN",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Host SMB e storage compatibili",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isScanning = true
                                                lanManager.scanLocalNetwork()
                                                isScanning = false
                                            }
                                        },
                                        enabled = !isScanning,
                                        modifier = Modifier.testTag("scan_lan_btn")
                                    ) {
                                        if (isScanning) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Scansione...")
                                        } else {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Scansiona")
                                        }
                                    }
                                }
                            }

                            if (discoveredDevices.isEmpty() && !isScanning) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
                                    ) {
                                        Text(
                                            text = "Tocca 'Scansiona' per cercare dispositivi SMB, NAS e computer Windows accesi sulla rete locale.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            } else {
                                items(discoveredDevices) { dev ->
                                    DiscoveredDeviceCard(
                                        device = dev,
                                        onConnect = {
                                            val newConn = lanManager.addConnection(
                                                name = dev.name,
                                                host = dev.host,
                                                shareName = dev.defaultShare,
                                                username = "",
                                                domain = "WORKGROUP",
                                                port = 445
                                            )
                                            onExploreShare(newConn.localMountPath)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .testTag("add_lan_conn_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aggiungi Connessione SMB")
                }
            }
        }
    }

    if (showAddDialog) {
        AddLanConnectionDialog(
            onDismiss = { showAddDialog = false },
            onTest = { host, port, onResult ->
                scope.launch {
                    val res = lanManager.testConnection(host, port)
                    onResult(res)
                }
            },
            onConfirm = { name, host, share, user, domain, port ->
                val conn = lanManager.addConnection(name, host, share, user, domain, port)
                showAddDialog = false
                onExploreShare(conn.localMountPath)
                onDismiss()
            }
        )
    }
}

@Composable
fun LanConnectionCard(
    connection: LanConnection,
    testStatus: String?,
    isTesting: Boolean,
    onExplore: () -> Unit,
    onTest: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (connection.name.contains("NAS", ignoreCase = true)) Icons.Default.Storage else Icons.Default.Computer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = connection.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "smb://${connection.host}/${connection.shareName} • Porta ${connection.port}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${connection.statusText} ${if (connection.username.isNotBlank()) "• Utente: " + connection.username else ""}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Rimuovi",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (testStatus != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = testStatus,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (testStatus.contains("Raggiungibile", ignoreCase = true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExplore,
                    modifier = Modifier.weight(1f).testTag("explore_lan_${connection.id}")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Esplora nei Pannelli", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onTest,
                    enabled = !isTesting
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Porta", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoveredDeviceCard(
    device: DiscoveredLanDevice,
    onConnect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = device.type, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = "IP: ${device.host} • Condivisione: /${device.defaultShare}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.testTag("connect_discovered_${device.host}")
            ) {
                Text("Connetti", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AddLanConnectionDialog(
    onDismiss: () -> Unit,
    onTest: (host: String, port: Int, onResult: (Pair<Boolean, String>) -> Unit) -> Unit,
    onConfirm: (name: String, host: String, share: String, user: String, domain: String, port: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("192.168.1.") }
    var share by remember { mutableStateOf("Public") }
    var user by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("WORKGROUP") }
    var portText by remember { mutableStateOf("445") }
    var testMessage by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lan, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuova Connessione SMB")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Connessione (es. PC Studio, NAS Casa)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lan_name_input")
                )

                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Indirizzo IP o Nome Host (es. 192.168.1.100)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lan_host_input")
                )

                OutlinedTextField(
                    value = share,
                    onValueChange = { share = it },
                    label = { Text("Cartella Condivisa (es. Shared, Public, C$)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lan_share_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = user,
                        onValueChange = { user = it },
                        label = { Text("Utente (vuoto = Guest)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = portText,
                        onValueChange = { portText = it },
                        label = { Text("Porta (445)") },
                        singleLine = true,
                        modifier = Modifier.width(90.dp)
                    )
                }

                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Dominio / Workgroup (WORKGROUP)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (testMessage != null) {
                    Text(
                        text = testMessage ?: "",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (testMessage?.contains("Raggiungibile", ignoreCase = true) == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                OutlinedButton(
                    onClick = {
                        isTesting = true
                        testMessage = "Verifica porta SMB in corso..."
                        val p = portText.toIntOrNull() ?: 445
                        onTest(host, p) { (ok, msg) ->
                            isTesting = false
                            testMessage = msg
                        }
                    },
                    enabled = !isTesting && host.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifica Raggiungibilità Host")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val port = portText.toIntOrNull() ?: 445
                    onConfirm(name, host, share, user, domain, port)
                },
                enabled = host.isNotBlank(),
                modifier = Modifier.testTag("save_lan_connection_btn")
            ) {
                Text("Salva & Esplora")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
