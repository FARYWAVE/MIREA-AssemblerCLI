import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.system.exitProcess

sealed class Command() {
    companion object {
        fun fromFile(path: String): List<Command> {
            val yaml = Yaml()
            val data = yaml.load<List<Map<String, List<Int>>>>(File(path).readText())

            return data.map { entry -> getCommand(entry)}
        }

        fun fromString(s: String): Command {
            val yaml = Yaml()
            val data = yaml.load<Map<String, List<Int>>>(s)

            return getCommand(data)
        }

        private fun getCommand(command: Map<String, List<Int>>): Command {
            val (cmd, args) = command.entries.first()
            try {
                return when (cmd) {
                    "LOAD" -> LoadConstant(
                        A = args[0],
                        B = args[1],
                        C = args[2]
                    )

                    "READ" -> Read(
                        A = args[0],
                        B = args[1],
                        C = args[2]
                    )

                    "WRITE" -> Write(
                        A = args[0],
                        B = args[1],
                        C = args[2],
                        D = args[3]
                    )

                    "NEGATIVE" -> Negative(
                        A = args[0],
                        B = args[1],
                        C = args[2]
                    )

                    "HISTORY" -> History()

                    "EXIT" -> Exit()

                    else -> Null("COMMAND NOT FOUND")
                }
            } catch (_: IndexOutOfBoundsException) {
                return Null("NOT ENOUGH ARGUMENTS GIVEN")
            }
        }

    }

    abstract fun invoke()

    data class LoadConstant(val A: Int, val B: Int, val C: Int) : Command() {
        override fun toString() = "A=$A B=$B C=$C"

        override fun invoke() {
            //TODO
        }
    }
    data class Read(val A: Int, val B: Int, val C: Int) : Command() {
        override fun toString() = "A=$A B=$B C=$C"

        override fun invoke() {
            //TODO
        }
    }
    data class Write(val A: Int, val B: Int, val C: Int, val D: Int) : Command() {
        override fun toString() = "A=$A B=$B C=$C D=$D"

        override fun invoke() {
            //TODO
        }
    }
    data class Negative(val A: Int, val B: Int, val C: Int) : Command() {
        override fun toString() = "A=$A B=$B C=$C"

        override fun invoke() {
            //TODO
        }
    }

    class History(): Command() {
        override fun invoke() {}
    }

    class Exit(): Command() {
        override fun invoke() {
            exitProcess(0)
        }

    }

    class Null(val message: String): Command() {
        override fun invoke() {}
    }
}