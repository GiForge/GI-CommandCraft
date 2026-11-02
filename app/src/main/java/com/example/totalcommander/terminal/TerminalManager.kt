package com.example.totalcommander.terminal

import com.example.totalcommander.fs.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TerminalLine(
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT
)

enum class TerminalLineType {
    COMMAND, OUTPUT, ERROR, SUCCESS, INFO
}

class TerminalManager(private val fileManager: FileManager) {

    var currentDir: File = fileManager.rootDir
        private set

    private val commandHistory = mutableListOf<String>()

    init {
        commandHistory.addAll(listOf("ls -la", "pwd", "cat Documents/Readme_TotalCommander.md", "help"))
    }

    fun getHistory(): List<String> = commandHistory.toList()

    fun setCurrentDirectory(dir: File) {
        if (dir.exists() && dir.isDirectory) {
            currentDir = dir
        }
    }

    suspend fun execute(commandLine: String): List<TerminalLine> = withContext(Dispatchers.IO) {
        val trimmed = commandLine.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        commandHistory.remove(trimmed)
        commandHistory.add(trimmed)

        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("tc@mobile:${getRelativePath(currentDir)}$ $trimmed", TerminalLineType.COMMAND))

        val parts = trimmed.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val cmd = parts[0].lowercase()
        val args = parts.drop(1)

        try {
            when (cmd) {
                "help" -> {
                    lines.add(TerminalLine("Total Commander Integrated Shell v1.2", TerminalLineType.INFO))
                    lines.add(TerminalLine("Available built-in commands:", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  pwd                  Print current working directory", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  ls [-l] [-a]         List directory contents", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  cd <path>            Change directory (.. for parent)", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  cat <file>           Display file contents", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  mkdir [-p] <dir>     Create new directory", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  touch <file>         Create an empty file", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  rm [-r] <file>       Remove file or directory", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  cp <src> <dst>       Copy file or directory", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  mv <src> <dst>       Move or rename file", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  grep <pat> <file>    Search pattern in file", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  find <name>          Find files matching name", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  stat <file>          Show file details and permissions", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  du [-h]              Show space usage", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  echo <text> [> f]    Display message or write to file", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  date                 Display current timestamp", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  whoami, uname        System information", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  history              Show previous commands", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("  clear                Clear the terminal output", TerminalLineType.OUTPUT))
                }

                "pwd" -> {
                    lines.add(TerminalLine(currentDir.absolutePath, TerminalLineType.OUTPUT))
                }

                "ls" -> {
                    val showAll = args.any { it.contains("a") }
                    val longFormat = args.any { it.contains("l") }
                    val targetDir = args.firstOrNull { !it.startsWith("-") }?.let { resolveFile(it) } ?: currentDir

                    if (!targetDir.exists()) {
                        lines.add(TerminalLine("ls: cannot access '${targetDir.name}': No such file or directory", TerminalLineType.ERROR))
                    } else if (targetDir.isFile) {
                        lines.add(TerminalLine(targetDir.name, TerminalLineType.OUTPUT))
                    } else {
                        val files = targetDir.listFiles() ?: emptyArray()
                        val filtered = files.filter { showAll || !it.name.startsWith(".") }
                            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

                        if (longFormat) {
                            val sdf = SimpleDateFormat("MMM dd HH:mm", Locale.US)
                            filtered.forEach { f ->
                                val perm = (if (f.isDirectory) "d" else "-") + (if (f.canRead()) "r" else "-") +
                                        (if (f.canWrite()) "w" else "-") + (if (f.canExecute()) "x" else "-") + "r--r--"
                                val sizeStr = if (f.isDirectory) "4096" else f.length().toString()
                                val dateStr = sdf.format(Date(f.lastModified()))
                                val typeColor = if (f.isDirectory) TerminalLineType.INFO else TerminalLineType.OUTPUT
                                lines.add(TerminalLine("%s %6s %s %s".format(perm, sizeStr, dateStr, f.name), typeColor))
                            }
                        } else {
                            val names = filtered.joinToString("  ") { f ->
                                if (f.isDirectory) "${f.name}/" else f.name
                            }
                            lines.add(TerminalLine(if (names.isEmpty()) "(empty directory)" else names, TerminalLineType.OUTPUT))
                        }
                    }
                }

                "cd" -> {
                    val target = args.firstOrNull() ?: "~"
                    val newDir = when (target) {
                        "~", "/" -> fileManager.rootDir
                        ".." -> currentDir.parentFile ?: currentDir
                        else -> resolveFile(target)
                    }

                    if (newDir.exists() && newDir.isDirectory) {
                        currentDir = newDir
                        lines.add(TerminalLine("Working directory changed to: ${getRelativePath(currentDir)}", TerminalLineType.SUCCESS))
                    } else {
                        lines.add(TerminalLine("cd: $target: No such directory", TerminalLineType.ERROR))
                    }
                }

                "cat" -> {
                    if (args.isEmpty()) {
                        lines.add(TerminalLine("cat: missing file operand", TerminalLineType.ERROR))
                    } else {
                        val f = resolveFile(args[0])
                        if (!f.exists()) {
                            lines.add(TerminalLine("cat: ${args[0]}: No such file", TerminalLineType.ERROR))
                        } else if (f.isDirectory) {
                            lines.add(TerminalLine("cat: ${args[0]}: Is a directory", TerminalLineType.ERROR))
                        } else {
                            f.readLines().take(200).forEach { line ->
                                lines.add(TerminalLine(line, TerminalLineType.OUTPUT))
                            }
                        }
                    }
                }

                "mkdir" -> {
                    val pFlag = args.contains("-p")
                    val dirName = args.firstOrNull { it != "-p" }
                    if (dirName == null) {
                        lines.add(TerminalLine("mkdir: missing operand", TerminalLineType.ERROR))
                    } else {
                        val newDir = resolveFile(dirName)
                        val ok = if (pFlag) newDir.mkdirs() else newDir.mkdir()
                        if (ok) {
                            lines.add(TerminalLine("Created directory: $dirName", TerminalLineType.SUCCESS))
                        } else {
                            lines.add(TerminalLine("mkdir: cannot create directory '$dirName'", TerminalLineType.ERROR))
                        }
                    }
                }

                "touch" -> {
                    if (args.isEmpty()) {
                        lines.add(TerminalLine("touch: missing file operand", TerminalLineType.ERROR))
                    } else {
                        val f = resolveFile(args[0])
                        if (f.exists()) {
                            f.setLastModified(System.currentTimeMillis())
                            lines.add(TerminalLine("Updated timestamp for ${args[0]}", TerminalLineType.SUCCESS))
                        } else {
                            f.parentFile?.mkdirs()
                            f.writeText("")
                            lines.add(TerminalLine("Created empty file: ${args[0]}", TerminalLineType.SUCCESS))
                        }
                    }
                }

                "rm" -> {
                    val recursive = args.contains("-r") || args.contains("-rf")
                    val targetName = args.firstOrNull { !it.startsWith("-") }
                    if (targetName == null) {
                        lines.add(TerminalLine("rm: missing operand", TerminalLineType.ERROR))
                    } else {
                        val f = resolveFile(targetName)
                        if (!f.exists()) {
                            lines.add(TerminalLine("rm: cannot remove '$targetName': No such file", TerminalLineType.ERROR))
                        } else if (f.isDirectory && !recursive) {
                            lines.add(TerminalLine("rm: cannot remove '$targetName': Is a directory (use -r)", TerminalLineType.ERROR))
                        } else {
                            val ok = if (f.isDirectory) f.deleteRecursively() else f.delete()
                            if (ok) {
                                lines.add(TerminalLine("Removed '$targetName'", TerminalLineType.SUCCESS))
                            } else {
                                lines.add(TerminalLine("rm: failed to remove '$targetName'", TerminalLineType.ERROR))
                            }
                        }
                    }
                }

                "cp" -> {
                    if (args.size < 2) {
                        lines.add(TerminalLine("cp: missing file operand. Usage: cp <src> <dst>", TerminalLineType.ERROR))
                    } else {
                        val src = resolveFile(args[0])
                        val dst = resolveFile(args[1])
                        if (!src.exists()) {
                            lines.add(TerminalLine("cp: cannot stat '${args[0]}': No such file", TerminalLineType.ERROR))
                        } else {
                            val target = if (dst.isDirectory) File(dst, src.name) else dst
                            if (src.isDirectory) {
                                src.copyRecursively(target, overwrite = true)
                            } else {
                                src.copyTo(target, overwrite = true)
                            }
                            lines.add(TerminalLine("Copied '${args[0]}' -> '${args[1]}'", TerminalLineType.SUCCESS))
                        }
                    }
                }

                "mv" -> {
                    if (args.size < 2) {
                        lines.add(TerminalLine("mv: missing operand. Usage: mv <src> <dst>", TerminalLineType.ERROR))
                    } else {
                        val src = resolveFile(args[0])
                        val dst = resolveFile(args[1])
                        if (!src.exists()) {
                            lines.add(TerminalLine("mv: cannot stat '${args[0]}': No such file", TerminalLineType.ERROR))
                        } else {
                            val target = if (dst.isDirectory) File(dst, src.name) else dst
                            if (src.renameTo(target)) {
                                lines.add(TerminalLine("Moved '${args[0]}' -> '${args[1]}'", TerminalLineType.SUCCESS))
                            } else {
                                src.copyTo(target, overwrite = true)
                                src.delete()
                                lines.add(TerminalLine("Moved '${args[0]}' -> '${args[1]}'", TerminalLineType.SUCCESS))
                            }
                        }
                    }
                }

                "grep" -> {
                    if (args.size < 2) {
                        lines.add(TerminalLine("grep: Usage: grep <pattern> <file>", TerminalLineType.ERROR))
                    } else {
                        val pattern = args[0]
                        val file = resolveFile(args[1])
                        if (!file.exists()) {
                            lines.add(TerminalLine("grep: ${args[1]}: No such file", TerminalLineType.ERROR))
                        } else {
                            var matchCount = 0
                            file.readLines().forEachIndexed { idx, line ->
                                if (line.contains(pattern, ignoreCase = true)) {
                                    matchCount++
                                    lines.add(TerminalLine("${idx + 1}: $line", TerminalLineType.OUTPUT))
                                }
                            }
                            lines.add(TerminalLine("Found $matchCount match(es)", TerminalLineType.INFO))
                        }
                    }
                }

                "find" -> {
                    val query = args.firstOrNull() ?: ""
                    var count = 0
                    currentDir.walkTopDown().forEach { f ->
                        if (query.isEmpty() || f.name.contains(query, ignoreCase = true)) {
                            count++
                            lines.add(TerminalLine(getRelativePath(f), TerminalLineType.OUTPUT))
                        }
                    }
                    lines.add(TerminalLine("Total found: $count", TerminalLineType.INFO))
                }

                "stat" -> {
                    val f = args.firstOrNull()?.let { resolveFile(it) } ?: currentDir
                    if (!f.exists()) {
                        lines.add(TerminalLine("stat: cannot stat '${args.firstOrNull()}': No such file", TerminalLineType.ERROR))
                    } else {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        lines.add(TerminalLine("  File: ${f.name}", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("  Size: ${f.length()} bytes    Type: ${if (f.isDirectory) "directory" else "regular file"}", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("  Path: ${f.absolutePath}", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("Access: (0755/rwxr-xr-x)  Writable: ${f.canWrite()}", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("Modify: ${sdf.format(Date(f.lastModified()))}", TerminalLineType.OUTPUT))
                    }
                }

                "du" -> {
                    var totalBytes = 0L
                    currentDir.walkTopDown().forEach { f ->
                        if (f.isFile) totalBytes += f.length()
                    }
                    val mb = totalBytes / (1024.0 * 1024.0)
                    lines.add(TerminalLine(String.format(Locale.US, "%.2f MB\t%s", mb, getRelativePath(currentDir)), TerminalLineType.OUTPUT))
                }

                "echo" -> {
                    val rawArgs = trimmed.removePrefix("echo").trim()
                    if (rawArgs.contains(">")) {
                        val echoParts = rawArgs.split(">", limit = 2)
                        val text = echoParts[0].trim().removeSurrounding("\"").removeSurrounding("'")
                        val fileName = echoParts[1].trim()
                        val f = resolveFile(fileName)
                        f.writeText(text + "\n")
                        lines.add(TerminalLine("Written to $fileName", TerminalLineType.SUCCESS))
                    } else {
                        val text = rawArgs.removeSurrounding("\"").removeSurrounding("'")
                        lines.add(TerminalLine(text, TerminalLineType.OUTPUT))
                    }
                }

                "date" -> {
                    lines.add(TerminalLine(Date().toString(), TerminalLineType.OUTPUT))
                }

                "whoami" -> {
                    lines.add(TerminalLine("tc_commander (uid=1000 gid=1000)", TerminalLineType.OUTPUT))
                }

                "uname" -> {
                    lines.add(TerminalLine("Linux totalcmd-mobile 6.1.0-android-arm64 #1 SMP PREEMPT 2026", TerminalLineType.OUTPUT))
                }

                "history" -> {
                    commandHistory.forEachIndexed { idx, h ->
                        lines.add(TerminalLine("%3d  %s".format(idx + 1, h), TerminalLineType.OUTPUT))
                    }
                }

                "clear" -> {
                    // special flag handled in caller
                }

                else -> {
                    lines.add(TerminalLine("tc: command not found: $cmd. Type 'help' for available commands.", TerminalLineType.ERROR))
                }
            }
        } catch (e: Exception) {
            lines.add(TerminalLine("Error executing command: ${e.message}", TerminalLineType.ERROR))
        }

        lines
    }

    private fun resolveFile(nameOrPath: String): File {
        return if (nameOrPath.startsWith("/")) {
            File(nameOrPath)
        } else {
            File(currentDir, nameOrPath)
        }
    }

    private fun getRelativePath(file: File): String {
        val rootPath = fileManager.rootDir.absolutePath
        val abs = file.absolutePath
        return if (abs.startsWith(rootPath)) {
            "~" + abs.removePrefix(rootPath)
        } else {
            abs
        }
    }
}
