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
        var isActive = false
        fun boot(commands: List<Command> = emptyList(), testMode: Boolean = false, outputPath: String = "./output.bin") {
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