package com.example.totalcommander.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.totalcommander.terminal.TerminalLine
import com.example.totalcommander.terminal.TerminalLineType
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalSheet(
    terminalLines: List<TerminalLine>,
    currentDirectory: File,
    commandHistory: List<String>,
    onExecuteCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputCommand by remember { mutableStateOf("") }
    var historyIndex by remember { mutableIntStateOf(-1) }
    val listState = rememberLazyListState()

    LaunchedEffect(terminalLines.size) {
        if (terminalLines.isNotEmpty()) {
            listState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0D1117),
        contentColor = Color(0xFFC9D1D9),
        dragHandle = null,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Terminal Header
            Surface(
                color = Color(0xFF161B22),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = Color(0xFF58A6FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Terminal Console",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF0F6FC),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "[${currentDirectory.name}]",
                            color = Color(0xFF8B949E),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row {
                        IconButton(onClick = { onExecuteCommand("clear") }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear",
                                tint = Color(0xFF8B949E)
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("terminal_close_btn")) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF8B949E)
                            )
                        }
                    }
                }
            }

            // Quick command chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B22).copy(alpha = 0.5f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val chips = listOf("ls -la", "pwd", "du -h", "help", "whoami", "date", "cat Documents/Readme_TotalCommander.md")
                chips.forEach { chipText ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF21262D),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = chipText,
                            color = Color(0xFF58A6FF),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable {
                                    onExecuteCommand(chipText)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Divider(color = Color(0xFF30363D))

            // Output Terminal Area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(terminalLines) { line ->
                    val color = when (line.type) {
                        TerminalLineType.COMMAND -> Color(0xFF7EE787) // Green
                        TerminalLineType.OUTPUT -> Color(0xFFE6EDF3) // White/Light
                        TerminalLineType.ERROR -> Color(0xFFFF7B72) // Red
                        TerminalLineType.SUCCESS -> Color(0xFF3FB950) // Bright Green
                        TerminalLineType.INFO -> Color(0xFF79C0FF) // Cyan/Blue
                    }
                    Text(
                        text = line.text,
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }

            Divider(color = Color(0xFF30363D))

            // Input Bar
            Surface(
                color = Color(0xFF161B22),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // History Up button
                    IconButton(
                        onClick = {
                            if (commandHistory.isNotEmpty()) {
                                if (historyIndex == -1) {
                                    historyIndex = commandHistory.size - 1
                                } else if (historyIndex > 0) {
                                    historyIndex--
                                }
                                inputCommand = commandHistory.getOrElse(historyIndex) { "" }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "History Previous",
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // History Down button
                    IconButton(
                        onClick = {
                            if (commandHistory.isNotEmpty() && historyIndex != -1) {
                                if (historyIndex < commandHistory.size - 1) {
                                    historyIndex++
                                    inputCommand = commandHistory[historyIndex]
                                } else {
                                    historyIndex = -1
                                    inputCommand = ""
                                }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "History Next",
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "$ ",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7EE787),
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = inputCommand,
                        onValueChange = { inputCommand = it },
                        placeholder = {
                            Text("Enter command...", color = Color(0xFF484F58), fontSize = 12.sp)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputCommand.isNotBlank()) {
                                    onExecuteCommand(inputCommand)
                                    inputCommand = ""
                                    historyIndex = -1
                                }
                            }
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color(0xFFF0F6FC)
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF58A6FF),
                            unfocusedBorderColor = Color(0xFF30363D),
                            focusedContainerColor = Color(0xFF0D1117),
                            unfocusedContainerColor = Color(0xFF0D1117)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("terminal_input_field")
                    )

                    IconButton(
                        onClick = {
                            if (inputCommand.isNotBlank()) {
                                onExecuteCommand(inputCommand)
                                inputCommand = ""
                                historyIndex = -1
                            }
                        },
                        modifier = Modifier.testTag("terminal_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Execute",
                            tint = Color(0xFF58A6FF)
                        )
                    }
                }
            }
        }
    }
}
