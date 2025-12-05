import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.min
import kotlin.system.exitProcess

sealed class Command {
    companion object {
        fun fromFile(path: String): List<Command> {
            val yaml = Yaml()
            val text = File(path).readText()
            val data = yaml.load<List<Map<String, List<Int>>>>(text)
            return data.map { entry -> getCommand(entry) }
        }

        fun fromString(s: String): Command {
            val yaml = Yaml()
            val data = yaml.load<Map<String, List<Int>>>(s)
            return getCommand(data)
        }

        private fun getCommand(command: Map<String, List<Int>>): Command {
            val (cmd, args) = command.entries.first()
            try {
                return when (cmd.uppercase()) {
                    "LOAD" -> {
                        if (args.size >= 2) {
                            val b = if (args.size == 2) args[0] else args[1]
                            val c = if (args.size == 2) args[1] else args[2]
                            Command.Load(B = b, C = c)
                        } else return Command.Null("LOAD expects 2 operands: B,C")
                    }

                    "READ" -> {
                        if (args.size >= 2) {
                            val b = if (args.size == 2) args[0] else args[1]
                            val c = if (args.size == 2) args[1] else args[2]
                            Command.Read(B = b, C = c) // A default = 0
                        } else return Command.Null("READ expects 2 operands: B,C")
                    }

                    "WRITE" -> {
                        if (args.size >= 3) {
                            val start = if (args.size == 3) 0 else 1
                            val b = args[start]
                            val c = args[start + 1]
                            val d = args[start + 2]
                            Command.Write(B = b, C = c, D = d) // A default = 30
                        } else return Command.Null("WRITE expects 3 operands: B,C,D")
                    }

                    "NEGATIVE" -> {
                        if (args.size >= 2) {
                            val b = if (args.size == 2) args[0] else args[1]
                            val c = if (args.size == 2) args[1] else args[2]
                            Command.Negative(B = b, C = c) // A default = 1
                        } else return Command.Null("NEGATIVE expects 2 operands: B,C")
                    }

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

        fun decodeAt(program: ByteArray, pc: Int): Pair<Command, Int> {
            val remaining = program.size - pc
            if (remaining <= 0) throw IllegalArgumentException("PC out of bounds")

            var value = 0L
            val readBytes = min(5, remaining)
            for (i in 0 until readBytes) {
                value = value or ((program[pc + i].toLong() and 0xFFL) shl (8 * i))
            }

            val opcode = (value and 0x3FL).toInt()
            return when (opcode) {
                16 -> {
                    if (remaining < 4) throw IllegalStateException("Truncated LOAD at $pc")
                    var v = 0L
                    for (i in 0 until 4) v = v or ((program[pc + i].toLong() and 0xFFL) shl (8 * i))
                    Pair(Load.decodeFrom(v), 4)
                }
                0 -> {
                    if (remaining < 3) throw IllegalStateException("Truncated READ at $pc")
                    var v = 0L
                    for (i in 0 until 3) v = v or ((program[pc + i].toLong() and 0xFFL) shl (8 * i))
                    Pair(Read.decodeFrom(v), 3)
                }
                30 -> {
                    if (remaining < 4) throw IllegalStateException("Truncated WRITE at $pc")
                    var v = 0L
                    for (i in 0 until 4) v = v or ((program[pc + i].toLong() and 0xFFL) shl (8 * i))
                    Pair(Write.decodeFrom(v), 4)
                }
                1 -> {
                    if (remaining < 5) throw IllegalStateException("Truncated NEGATIVE at $pc")
                    var v = 0L
                    for (i in 0 until 5) v = v or ((program[pc + i].toLong() and 0xFFL) shl (8 * i))
                    Pair(Negative.decodeFrom(v), 5)
                }
                else -> throw IllegalStateException("Unknown opcode $opcode at pc=$pc")
            }
        }
    }

    abstract fun invoke()
    abstract fun asBytes(): ByteArray

    data class Load(val A: Int = 16, val B: Int, val C: Int) : Command() {
        override fun toString() = "LOAD A=$A B=$B C=$C"
        override fun invoke() {
            val regs = LazerSystem.runtimeRegs ?: error("Runtime not initialized")
            regs[B] = C
        }

        override fun asBytes(): ByteArray {
            val commandValue = (A.toLong() and 0x3FL) or
                    ((B.toLong() and 0x3FL) shl 6) or
                    ((C.toLong() and 0x7FFFL) shl 12)
            val bytes = ByteArray(4)
            for (i in 0 until 4) bytes[i] = ((commandValue shr (8 * i)) and 0xFF).toByte()
            return bytes
        }

        companion object {
            fun decodeFrom(value: Long): Load {
                val a = (value and 0x3FL).toInt()
                val b = ((value shr 6) and 0x3FL).toInt()
                val c = ((value shr 12) and 0x7FFFL).toInt()
                return Load(a, b, c)
            }
        }
    }

    data class Read(val A: Int = 0, val B: Int, val C: Int) : Command() {
        override fun toString() = "READ A=$A B=$B C=$C"
        override fun invoke() {
            val regs = LazerSystem.runtimeRegs ?: error("Runtime not initialized")
            val mem = LazerSystem.runtimeMemory ?: error("Runtime not initialized")
            val addr = regs[C]
            if (addr < 0 || addr >= mem.size) error("Memory access out of bounds: $addr")
            regs[B] = mem[addr]
        }

        override fun asBytes(): ByteArray {
            val commandValue = (A.toLong() and 0x3FL) or
                    ((B.toLong() and 0x3FL) shl 6) or
                    ((C.toLong() and 0x3FL) shl 12)
            val bytes = ByteArray(3)
            for (i in 0 until 3) bytes[i] = ((commandValue shr (8 * i)) and 0xFF).toByte()
            return bytes
        }

        companion object {
            fun decodeFrom(value: Long): Read {
                val a = (value and 0x3FL).toInt()
                val b = ((value shr 6) and 0x3FL).toInt()
                val c = ((value shr 12) and 0x3FL).toInt()
                return Read(a, b, c)
            }
        }
    }

    data class Write(val A: Int = 30, val B: Int, val C: Int, val D: Int) : Command() {
        override fun toString() = "WRITE A=$A B=$B C=$C D=$D"
        override fun invoke() {
            val regs = LazerSystem.runtimeRegs ?: error("Runtime not initialized")
            val mem = LazerSystem.runtimeMemory ?: error("Runtime not initialized")
            val base = regs[B]
            val addr = base + C
            if (addr < 0) error("Negative memory address: $addr")
            if (addr >= mem.size) {
                val newSize = addr + 1
                val newMem = IntArray(newSize)
                mem.copyInto(newMem, 0, 0, mem.size)
                LazerSystem.runtimeMemory = newMem
            }
            LazerSystem.runtimeMemory!![addr] = regs[D]
        }

        override fun asBytes(): ByteArray {
            val commandValue = (A.toLong() and 0x3FL) or
                    ((B.toLong() and 0x3FL) shl 6) or
                    ((C.toLong() and 0xFFFL) shl 12) or
                    ((D.toLong() and 0x3FL) shl 24)
            val bytes = ByteArray(4)
            for (i in 0 until 4) bytes[i] = ((commandValue shr (8 * i)) and 0xFF).toByte()
            return bytes
        }

        companion object {
            fun decodeFrom(value: Long): Write {
                val a = (value and 0x3FL).toInt()
                val b = ((value shr 6) and 0x3FL).toInt()
                val c = ((value shr 12) and 0xFFFL).toInt()
                val d = ((value shr 24) and 0x3FL).toInt()
                return Write(a, b, c, d)
            }
        }
    }

    data class Negative(val A: Int = 1, val B: Int, val C: Int) : Command() {
        override fun toString() = "NEGATIVE A=$A B=$B C=$C"
        override fun invoke() {
            // Negative: memory[C] = - regs[B]
            val regs = LazerSystem.runtimeRegs ?: error("Runtime not initialized")
            var mem = LazerSystem.runtimeMemory ?: error("Runtime not initialized")
            val value = -regs[B]
            val addr = C
            if (addr < 0) error("Negative memory address: $addr")
            if (addr >= mem.size) {
                val newSize = addr + 1
                val newMem = IntArray(newSize)
                mem.copyInto(newMem, 0, 0, mem.size)
                LazerSystem.runtimeMemory = newMem
                mem = newMem
            }
            mem[addr] = value
        }

        override fun asBytes(): ByteArray {
            val commandValue = (A.toLong() and 0x3FL) or
                    ((B.toLong() and 0x3FL) shl 6) or
                    ((C.toLong() and 0x1FFFFFL) shl 12)
            val bytes = ByteArray(5)
            for (i in 0 until 5) bytes[i] = ((commandValue shr (8 * i)) and 0xFF).toByte()
            return bytes
        }

        companion object {
            fun decodeFrom(value: Long): Negative {
                val a = (value and 0x3FL).toInt()
                val b = ((value shr 6) and 0x3FL).toInt()
                val c = ((value shr 12) and 0x1FFFFFL).toInt()
                return Negative(a, b, c)
            }
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