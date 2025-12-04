import java.io.File
import java.util.Scanner

class LazerSystem {
    val history = mutableListOf<Command>()
    var assembledCount = 0;
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
        const val MEMORY_SIZE = 65536
        var isActive = false
        fun assemble(commands: List<Command> = emptyList(), testMode: Boolean = false, outputPath: String = "./output.bin") {
            if (isActive) throw Exception("LAZER SYSTEM ALREADY ACTIVE")
            val system = LazerSystem()
            println(system.logo)
            isActive = true
            system.history.addAll(commands)
            system.printBytes()

            val output = File(outputPath)
            system.printAssembled()
            commands.forEach { output.appendBytes(it.asBytes()) }
            val sc = Scanner(System.`in`)
            while (true) {
                val input = sc.nextLine()
                val command = Command.fromString(input + (if (input.contains(':')) "" else ": [ ]"))
                when (command) {
                    is Command.History -> system.printHistory()
                    is Command.Assembled -> system.printAssembled()
                    is Command.Bytes -> system.printBytes()
                    is Command.Null -> println(command.message)
                    else -> {
                        command.invoke()
                        system.history.add(command)
                        output.appendBytes(command.asBytes())
                        system.assembledCount++
                    }
                }
            }
        }

        fun interpret(inputPath: String, outputPath: String, range: String) {
            if (isActive) throw Exception("LAZER SYSTEM ALREADY ACTIVE")
            val system = LazerSystem()
            println(system.logo)
            isActive = true

            val program = File(inputPath).readBytes()
            val memory = IntArray(MEMORY_SIZE)
            var pc = 0

            while (pc < program.size) {
                val remaining = program.size - pc

                // Determine command type by size
                val commandSize = if (remaining >= 4) 4 else if (remaining >= 3) 3 else error("UNEXPECTED COMMAND AT PC=$pc")
                val value = when (commandSize) {
                    3 -> { // LOAD or STORE
                        val b0 = program[pc].toInt() and 0xFF
                        val b1 = program[pc + 1].toInt() and 0xFF
                        val b2 = program[pc + 2].toInt() and 0xFF
                        b0 or (b1 shl 8) or (b2 shl 16)
                    }
                    4 -> { // LOAD_CONST
                        val b0 = program[pc].toInt() and 0xFF
                        val b1 = program[pc + 1].toInt() and 0xFF
                        val b2 = program[pc + 2].toInt() and 0xFF
                        val b3 = program[pc + 3].toInt() and 0xFF
                        b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
                    }
                    else -> error("Invalid command size")
                }

                val A = value and 0b111111
                val B = (value shr 6) and 0b111111
                val C = if (commandSize == 4) (value shr 12) and 0x7FFF else (value shr 12) and 0b111111

                // Execute command
                if (commandSize == 3) {
                    // Convention: first 3-byte command is LOAD, second is STORE, etc.
                    // Here you decide how to distinguish LOAD vs STORE, e.g., by YAML order or external mapping
                    memory[B] = memory[C] // treat as LOAD
                    // memory[C] = memory[B] // treat as STORE if needed
                } else if (commandSize == 4) {
                    memory[B] = C // LOAD_CONST
                }

                pc += commandSize
            }

            val (start, end) = range.split(':').map { it.toInt() }
            dumpMemoryToCsv(memory, start, end, outputPath)
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
    sb.append("address,value\n") // заголовок

    for (i in from..to) {
        sb.append(i)
        sb.append(",")
        sb.append(memory[i])
        sb.append("\n")
    }

    File(outputPath).writeText(sb.toString())
}