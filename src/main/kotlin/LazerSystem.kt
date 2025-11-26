import java.util.Scanner

class LazerSystem {
    val history = mutableListOf<Command>()
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
        fun boot(commands: List<Command> = emptyList(), testMode: Boolean = false) {
            if (isActive) throw Exception("LAZER SYSTEM ALREADY ACTIVE")
            val system = LazerSystem()
            println(system.logo)
            isActive = true
            system.history.addAll(commands)
            if (testMode) commands.forEach { println(it) }

            val sc = Scanner(System.`in`)
            while (true) {
                val input = sc.nextLine()
                val command = Command.fromString(input + (if (input.contains(':')) "" else ": [ ]"))
                when (command) {
                    is Command.History -> system.printHistory()
                    is Command.Null -> println(command.message)
                    else -> {
                        command.invoke()
                        system.history.add(command)
                    }
                }
            }
        }
    }
    fun printHistory() {
        history.forEach { println(it.toString()) }
    }
}