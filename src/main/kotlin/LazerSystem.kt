import java.io.File
import java.util.Scanner

class LazerSystem {
    val history = mutableListOf<Command>()
    var assembledCount = 0
    val logo =  "██▓    ▄▄▄      ▒███████▒▓█████  ██▀███   ▓█████▄  ██▓ ███▄ ▄███▓  ▒█████    ██████ \n" +
    "▓██▒   ▒████▄    ▒ ▒ ▒ ▄▀░▓█   ▀ ▓██ ▒ ██▒ ▒██▀ ██▌▓██▒▓██▒▀█▀ ██▒ ▒██▒  ██▒▒██    ▒ \n" +
    "▒██░   ▒██  ▀█▄  ░ ▒ ▄▀▒░ ▒███   ▓██ ░▄█ ▒ ░██   █▌▒██▒▓██    ▓██░ ▒██░  ██▒░ ▓██▄   \n" +
    "▒██░   ░██▄▄▄▄██   ▄▀▒   ░▒▓█  ▄ ▒██▀▀█▄   ░▓█▄   ▌░██░▒██    ▒██  ▒██   ██░  ▒   ██▒\n" +
    "░██████▒▓█   ▓██▒▒███████▒░▒████▒░██▓ ▒██▒ ░▒████▓ ░██░▒██▒   ░██▒  ░████▓▒░▒██████▒▒\n" +
    "░ ▒░▓  ░▒▒   ▓▒█░░▒▒ ▓░▒░▒░░ ▒░ ░░ ▒▓ ░▒▓░  ▒▒▓  ▒ ░▓  ░ ▒░   ░  ░░  ▒░▒░▒░ ▒ ▒▓▒ ▒ ░\n" +
    "░ ░ ▒  ░ ▒   ▒▒ ░░░▒ ▒ ░ ▒ ░ ░  ░  ░▒ ░ ▒░  ░ ▒  ▒  ▒ ░░  ░      ░   ░ ▒ ▒░ ░ ░▒  ░ ░\n" +
    "  ░ ░    ░   ▒   ░ ░ ░ ░ ░   ░     ░░   ░   ░ ░  ░  ▒ ░░      ░   ░  ░ ░ ▒  ░  ░  ░  \n" +
    "    ░  ░     ░  ░  ░ ░       ░  ░   ░         ░     ░         ░       ░ ░        ░  \n" +
    "                 ░                          ░\n" +
    "   __          _______   _____  ___      _____ _   ______\n" +
    "  / /  __ __  / __/ _ | / _ \\ \\/ / | /| / / _ | | / / __/\n" +
    " / _ \\/ // / / _// __ |/ , _/\\  /| |/ |/ / __ | |/ / _/  \n" +
    "/_.__/\\_, / /_/ /_/ |_/_/|_| /_/ |__/|__/_/ |_|___/___/  \n" +
    "     /___/ \n" +
            "VERSION: 3.4.7\n"


    companion object {
        const val DEFAULT_MEM_SIZE = 65536
        var runtimeMemory: IntArray? = null
        var runtimeRegs: IntArray? = null

        private fun initRuntime(memorySize: Int = DEFAULT_MEM_SIZE, regsSize: Int = 256) {
            runtimeMemory = IntArray(memorySize) { 0 }
            runtimeRegs = IntArray(regsSize) { 0 }
        }

        fun assemble(
            commands: List<Command> = emptyList(),
            testMode: Boolean = false,
            outputPath: String = "./output.bin"
        ) {
            if (runtimeMemory != null) throw Exception("LAZER SYSTEM ALREADY ACTIVE")
            val system = LazerSystem()
            println(system.logo)
            initRuntime()
            system.history.addAll(commands)
            system.printBytes()

            val output = File(outputPath)
            output.writeBytes(byteArrayOf())
            system.printAssembled()

            commands.forEach { output.appendBytes(it.asBytes()) }

            val sc = Scanner(System.`in`)
            while (true) {
                val input = sc.nextLine().trim()
                if (input.isEmpty()) continue
                val line = if (input.contains(':')) input else "$input: []"
                val command = try {
                    Command.fromString(line)
                } catch (e: Exception) {
                    println("Parse error: ${e.message}")
                    continue
                }
                when (command) {
                    is Command.History -> system.printHistory()
                    is Command.Assembled -> system.printAssembled()
                    is Command.Bytes -> system.printBytes()
                    is Command.Null -> println(command.message)
                    is Command.Exit -> {
                        println("Exiting assemble REPL.")
                        break
                    }

                    else -> {
                        try {
                            command.invoke()
                            system.history.add(command)
                            output.appendBytes(command.asBytes())
                            system.assembledCount++
                        } catch (e: Exception) {
                            println("Runtime error during invoke(): ${e.message}")
                        }
                    }
                }
            }
        }

        fun interpret(inputPath: String, outputPath: String, range: String, regsSize: Int = 256) {
            if (runtimeMemory != null) throw Exception("LAZER SYSTEM ALREADY ACTIVE")
            val system = LazerSystem()
            println(system.logo)
            initRuntime()
            runtimeMemory = IntArray(DEFAULT_MEM_SIZE) { 0 }
            runtimeRegs = IntArray(regsSize) { 0 }

            val program = File(inputPath).readBytes()
            var pc = 0

            while (pc < program.size) {
                val (cmd, size) = Command.decodeAt(program, pc)
                println("PC=$pc size=$size -> $cmd")
                cmd.invoke()
                pc += size
            }

            val (start, end) = range.split(':').map { it.toInt() }
            dumpMemoryToCsv(runtimeMemory!!, start, end, outputPath)
            println("Interpret finished. Dump written to $outputPath")
            runtimeMemory = null
            runtimeRegs = null
        }
    }

    fun printHistory() {
        history.forEach { println(it.toString()) }
    }

    fun printAssembled() {
        println("ASSEMBLED $assembledCount COMMANDS")
    }

    fun printBytes() {
        var cnt = 0
        history.forEach { command ->
            val bytes = command.asBytes()
            if (bytes.isNotEmpty()) {
                println(bytes.joinToString(", ") { "0x%02X".format(it) })
                cnt++
            }
        }
        assembledCount = cnt
    }
}

fun dumpMemoryToCsv(
    memory: IntArray,
    from: Int,
    to: Int,
    outputPath: String
) {
    val sb = StringBuilder()
    sb.append("address,value\n")

    for (i in from..to) {
        sb.append(i)
        sb.append(",")
        sb.append(memory[i])
        sb.append("\n")
    }

    File(outputPath).writeText(sb.toString())
}