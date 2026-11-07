# GI-CommandCraft 🛡️📁

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-757575.svg?style=flat&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)


**GI-CommandCraft** è un potente e versatile file manager avanzato per smartphone e tablet Android, ispirato alla classica architettura a **doppio pannello** di *Total Commander*. Progettato con le più moderne linee guida di **Jetpack Compose** e **Material 3**, combina la massima efficienza produttiva da desktop con la fluidità e l'ergonomia mobile.

---
<div align="center">
  <img width="328" height="724" alt="image" src="https://github.com/user-attachments/assets/33d14e00-ab71-4b6f-b176-ac536d33d095" />
</div>

## 📸 Panoramica Interfaccia

```
+-------------------------------------------------------------+
| [GI-CommandCraft]  [Left: /sdcard/Downloads]  [Split] [⚙️]  |
+------------------------------+------------------------------+
| [Panel Sinistro]             | [Panel Destro]               |
| .. [Cartella Superiore]      | .. [Cartella Superiore]      |
| 📁 Documents/                | 📁 DCIM/                     |
| 📁 Progetti/                 | 📁 Backup/                   |
| 📄 app-release.apk           | 📦 archivio.zip              |
| 📝 note.txt                  | 🖼️ screenshot.png           |
|                              |                              |
+------------------------------+------------------------------+
| [F2 Rin.] [F3 View] [F4 Edit] [F5 Copia] [F6 Sposta] [F7 Dir] |
| [F8 Elim.] [F9 Term] [F10 Cerca] [ZIP] [HASH] [SHARE] [LAN] |
+-------------------------------------------------------------+
```

  ---

## ✨ Funzionalità Principali

### 🗂️ 1. Architettura a Doppio Pannello (Dual-Pane)
- **Modalità Affiancata (Split Screen)**: Visualizzazione contemporanea dei due pannelli (Sinistro e Destro) con ottimizzazione su riga singola ultra-compatta per smartphone e tablet.
- **Modalità Singola con Tab**: Switch istantaneo tra pannello Sinistro e Destro tramite linguette superiori o pulsante rapido.
- **Sincronizzazione Percorsi**: Copia o sposta file al volo dal pannello sorgente direttamente alla cartella aperta nel pannello di destinazione.
- **Percorsi Rapidi & Root Virtuale**: Accesso con 1-tap a Memoria Interna (`/sdcard`), Download, Documenti, Galleria (`DCIM`), Schede SD esterne / USB OTG e Root di Sistema (`/`).

### 👆 2. Sistema di Interazione Touch Ottimizzato
- **Singolo Tap**: Seleziona o deseleziona file e cartelle (con contatore elementi selezionati e peso in byte).
- **Doppio Tap**: Esecuzione immediata (apertura di directory, visualizzazione di file, avvio di app o archivi).
- **Pressione Prolungata (Long Press)**: Apertura del menu contestuale completo sul file/cartella:
  - ✏️ **Rinomina**: Modifica rapida del nome o dell'estensione.
  - 📋 **Copia & Sposta**: Trasferimento istantaneo verso il pannello opposto.
  - 🗑️ **Elimina**: Cancellazione con dialogo di conferma di sicurezza.
  - 📤 **Condividi (Bluetooth / App)**: Invio nativo via Bluetooth, Quick Share, Telegram, WhatsApp, Email, ecc.
  - 🚀 **Apri con...**: Associazione di file con app di terze parti (es. Word per `.docx`, VLC per video, PDF Reader).
  - 📦 **Comprimi in ZIP**: Creazione rapida di archivi compressi.
  - 🔍 **Calcola Checksum**: Generazione hash per integrità file.
- **Scrolling Ultra-Fluido**: Nessun conflitto da gesture di trascinamento accidentali; scroll ottimizzato anche su cartelle con oltre 50.000 elementi.
- **Layout Compatto a Riga Singola**: Ogni elemento occupa rigorosamente una sola riga, massimizzando lo spazio utile a schermo.

### 🌐 3. Esplorazione Rete Locale (LAN / Samba SMB)
- **Rilevamento Host di Rete**: Scanner di sottorete integrato per individuare PC Windows, Server NAS, stampanti e dispositivi di rete (porta 445 e 139).
- **Connessione & Condivisione SMB**: Accesso a cartelle condivise con credenziali (Username / Password / Dominio) o accesso anonimo/guest.
- **Montaggio Virtuale**: Naviga le risorse di rete direttamente all'interno dei pannelli di CommandCraft.

### 📦 4. Gestione Archivi ZIP Avanzata
- **Navigazione Trasparente**: Esplora il contenuto degli archivi `.zip` come se fossero normali cartelle, senza bisogno di estrarre prima l'intero archivio.
- **Creazione Archivi ZIP**: Comprimi selezioni multiple di file o intere gerarchie di cartelle.
- **Estrazione Intelligente**: Estrai file singoli o l'intero archivio nel pannello opposto o nella directory corrente.

### 💻 5. Terminale Linux Integrato (Shell CLI)
- **Emulatore di Terminale Incorporato**: Pannello a scomparsa per eseguire comandi shell standard Android (`ls`, `df -h`, `cat`, `pwd`, `ps`, `uptime`, ecc.).
- **Contestuale alla Cartella Corrente**: Il terminale si apre automaticamente nella directory attiva del pannello.
- **Cronologia Comandi**: Tasti freccia su/giù per richiamare rapidamente i comandi digitati in precedenza.

### 📝 6. Visualizzatore ed Editor di Testo
- Supporto per file `.txt`, `.log`, `.ini`, `.json`, `.xml`, `.sh`, `.kt`, `.properties`, `.md`, ecc.
- Editor integrato con salvataggio sicuro e gestione codifica UTF-8.

### 🖼️ 7. Visualizzatore Immagini Integrato
- Supporto per formati grafici standard: `.jpg`, `.jpeg`, `.png`, `.webp`, `.gif`, `.bmp`.
- Dialogo a schermo intero con zoom e dettagli file (risoluzione e dimensione).

### 🔒 8. Calcolo e Verifica Hash / Checksum
- Generatore integrato di hash crittografici per verifica integrità file:
  - **MD5**
  - **SHA-1**
  - **SHA-256**
- Copia rapida dell'hash negli appunti con un solo tocco.

### 🔌 9. Sistema di Plugin Estensibile (Total Commander Style)
- Architettura modulare con interfaccia `TcPlugin` per integrare:
  - Plugin di File System (servizi cloud, FTP, WebDAV).
  - Plugin di Archivio (supporto per formati 7z, RAR, TAR).
  - Plugin di Visualizzazione / Viewer personalizzati.
- Manager visivo per attivare, disattivare e configurare i moduli aggiuntivi.

### ⚙️ 10. File di Configurazione Personalizzabile (`tc.ini`)
- Tutte le impostazioni (orientamento pannelli, ordinamento predefinito, percorsi di avvio, temi, flag di visualizzazione file nascosti) sono salvate nel file standard `tc.ini`.
- Editor di configurazione integrato nell'app.

---

## ⌨️ Tasti Funzione Rapidi (F-Keys Bar)

Ispirati alla classica barra inferiore di Total Commander:

| Tasto | Azione | Descrizione |
|---|---|---|
| **F2** | `Rinomina` | Rinomina il file o la cartella selezionata |
| **F3** | `Visualizza` | Apre l'anteprima interna (Testo, Immagine o Archivio) |
| **F4** | `Modifica` | Apre l'editor di testo integrato |
| **F5** | `Copia` | Copia gli elementi selezionati nel pannello opposto |
| **F6** | `Sposta` | Sposta gli elementi selezionati nel pannello opposto |
| **F7** | `Nuova Cartella` | Crea una nuova cartella nel percorso attivo |
| **F8** | `Elimina` | Rimuove i file/cartelle selezionati con richiesta di conferma |
| **F9** | `Terminale` | Apre la console shell interattiva |
| **F10** | `Cerca` | Ricerca avanzata per nome, estensione e pattern |
| **ZIP** | `Archivio` | Comprimi o scompatta archivi ZIP |
| **HASH** | `Checksum` | Calcola e confronta hash MD5, SHA-1, SHA-256 |
| **SHARE** | `Condividi` | Condividi via Bluetooth, app di messaggistica o "Apri con..." |
| **LAN** | `Rete SMB` | Esplora e connettiti a dispositivi in rete locale |

---

## 🔒 Permessi e Sicurezza

Per garantire l'accesso completo al file system preservando al contempo la sicurezza del dispositivo secondo le linee guida Google Play:

- `MANAGE_EXTERNAL_STORAGE`: Permette la gestione completa di file e cartelle su Android 11+ (All Files Access).
- `INTERNET` & `ACCESS_NETWORK_STATE`: Utilizzati per la scansione e il collegamento alle condivisioni di rete locale LAN / SMB.
- `VIBRATE`: Feedback aptico sulle azioni touch e completamento operazioni.
- `androidx.core.content.FileProvider`: Condivisione sicura di file tramite `content://` URI con permessi temporanei di lettura concessi alle app riceventi (senza esporre percorsi fisici `file://`).

---

## 🛠️ Stack Tecnologico e Architettura

```
com.example.totalcommander/
├── config/             # Gestione file tc.ini (IniConfigManager)
├── fs/                 # Driver del File System & ZipArchiveManager
├── model/              # Data models immutabili (FileItem, PanelState, SortOrder)
├── network/            # Scansione LAN e supporto client SMB (LanNetworkManager)
├── plugins/            # PluginManager e interfaccia TcPlugin
├── terminal/           # TerminalManager e interprete comandi CLI
├── ui/
│   ├── components/     # DualPanelLayout, FileListView, BottomFKeysBar, TopBar
│   ├── dialogs/        # TerminalSheet, LanNetworkDialog, ImageViewer, PluginDialog, ecc.
│   └── theme/          # Sistema di design M3, tipografia e palette colori
├── util/               # FileShareHelper (Bluetooth/OpenWith), ChecksumUtils
└── viewmodel/          # TotalCommanderViewModel (StateFlow & Coroutines)
```

- **Linguaggio**: Kotlin 2.0+ (100% Kotlin Coroutines & Flow)
- **UI Toolkit**: Jetpack Compose con Material Design 3
- **Architettura**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **Concorrenza**: Kotlin Coroutines (`Dispatchers.IO` per operazioni su disco e rete)
- **Test**: Robolectric & JUnit4 per test unitari JVM veloci e stabili

---

## 🚀 Compilazione ed Esecuzione

### Prerequisiti
- **JDK 17** o superiore
- **Android SDK Platform 34+**
- **Android Studio Ladybug | Koala** o Gradle CLI

### Compilazione da Terminale (Linux / macOS / Windows)

```bash
# Clona il repository
git clone https://github.com/tuo-username/GI-CommandCraft.git
cd GI-CommandCraft

# Esegui i test unitari
gradle :app:testDebugUnitTest

# Compila l'APK di Debug
gradle :app:assembleDebug

# Compila l'APK di Release
gradle :app:assembleRelease
```

L'APK compilato sarà disponibile in:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📖 Utilizzo e Suggerimenti

1. **Primo Avvio**: Alla prima apertura, concedi il permesso di *Accesso a tutti i file* per abilitare la navigazione completa della memoria del dispositivo.
2. **Navigazione Veloce**: Clicca su `..` in cima alla lista per risalire di livello. Anche dalla root principale è sempre disponibile la voce `..` per accedere alla schermata con le scorciatoie di sistema.
3. **Modalità Orizzontale**: Ruotando il dispositivo in orizzontale, la visualizzazione a doppio pannello si espande automaticamente offrendo la massima comodità operativa simile a un notebook.
4. **Condivisione con Word/PDF**: Tieni premuto su un file `.doc` o `.pdf` e seleziona **"Apri con..."** dal menu per visualizzarlo nell'app preferita.

---

## 🤝 Contribuire

I contributi sono benvenuti! Se desideri implementare un nuovo plugin (es. supporto SFTP, Google Drive, o nuovi formati di compressione):
1. Fai il **Fork** del progetto.
2. Crea un branch dedicato alla tua feature (`git checkout -b feature/NuovoPlugin`).
3. Effettua il commit delle modifiche (`git commit -m 'Aggiunto supporto al plugin'`).
4. Invia una **Pull Request**.

---

## 📄 Licenza

Distribuito sotto licenza **Apache 2.0**. Consulta il file `LICENSE` per maggiori informazioni.
