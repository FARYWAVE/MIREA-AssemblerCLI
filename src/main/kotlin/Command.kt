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

                    "ASSEMBLED" -> Assembled()

                    "BYTES" -> Bytes()

                    "EXIT" -> Exit()

                    else -> Null("COMMAND NOT FOUND")
                }
            } catch (_: IndexOutOfBoundsException) {
                return Null("NOT ENOUGH ARGUMENTS GIVEN")
            }
        }

    }

    abstract fun invoke()
    abstract fun asBytes(): ByteArray

    data class LoadConstant(val A: Int, val B: Int, val C: Int) : Command() {
        override fun toString() = "A=$A B=$B C=$C"

        override fun invoke() {
            //TODO
        }

        override fun asBytes(): ByteArray {
            val commandValue = A or (B shl 6) or (C shl 12)
            val bytes = ByteArray(4)
            bytes[0] = (commandValue and 0xFF).toByte()
            bytes[1] = ((commandValue shr 8) and 0xFF).toByte()
            bytes[2] = ((commandValue shr 16) and 0xFF).toByte()
            bytes[3] = ((commandValue shr 24) and 0xFF).toByte()
            return bytes
        }
    }
    data class Read(val A: Int, val B: Int, val C: Int) : Command() {
        override fun toString() = "A=$A B=$B C=$C"

        override fun invoke() {
            //TODO
        }

        override fun asBytes(): ByteArray {
            val commandValue = A or (B shl 6) or (C shl 12)
            val bytes = ByteArray(3)
            bytes[0] = (commandValue and 0xFF).toByte()
            bytes[1] = ((commandValue shr 8) and 0xFF).toByte()
            bytes[2] = ((commandValue shr 16) and 0xFF).toByte()
            return bytes
        }
    }
    data class Write(val A: Int, val B: Int, val C: Int, val D: Int) : Command() {
        override fun toString() = "A=$A B=$B C=$C D=$D"

        override fun invoke() {
            //TODO
        }

        override fun asBytes(): ByteArray {
            val commandValue = A or (B shl 6) or (C shl 12) or (D shl 24)
            val bytes = ByteArray(4)
            bytes[0] = (commandValue and 0xFF).toByte()
            bytes[1] = ((commandValue shr 8) and 0xFF).toByte()
            bytes[2] = ((commandValue shr 16) and 0xFF).toByte()
            bytes[3] = ((commandValue shr 24) and 0xFF).toByte()
            return bytes
        }
    }
    data class Negative(val A: Int, val B: Int, val C: Int) : Command() {
        override fun toString() = "A=$A B=$B C=$C"

        override fun invoke() {
            //TODO
        }

        override fun asBytes(): ByteArray {
            val commandValue = A or (B shl 6) or (C shl 12)
            val bytes = ByteArray(5)
            bytes[0] = (commandValue and 0xFF).toByte()
            bytes[1] = ((commandValue shr 8) and 0xFF).toByte()
            bytes[2] = ((commandValue shr 16) and 0xFF).toByte()
            bytes[3] = ((commandValue shr 24) and 0xFF).toByte()
            bytes[4] = ((commandValue shr 32) and 0xFF).toByte()
            return bytes
        }
    }

    class History(): Command() {
        override fun invoke() {}
        override fun asBytes() = ByteArray(0)
    }

    class Assembled(): Command() {
        override fun invoke() {}
        override fun asBytes() = ByteArray(0)
    }

    class Bytes(): Command() {
        override fun invoke() {}
        override fun asBytes() = ByteArray(0)
    }

    class Exit(): Command() {
        override fun invoke() {
            exitProcess(0)
        }
        override fun asBytes() = ByteArray(0)

    }

    class Null(val message: String): Command() {
        override fun invoke() {}
        override fun asBytes() = ByteArray(0)
    }
}