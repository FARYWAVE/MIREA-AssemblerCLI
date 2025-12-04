import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.nio.file.Files
import java.nio.file.Path

class ArgParser: CliktCommand(name="ASSEMBLER CLI") {
    private val input by argument(help = "PATH TO YAML SOURCE FILE")
    private val output by argument(help = "PATH TO OUTPUT BINARY FILE")
    private val testMode by option("-t", "--test", help = "TEST MODE").flag(default = false)
    private val range by option("-r", "--range", help = "RANGE OF COMMANDS TO ASSEMBLE IN <XX:XX> FORMAT")

    override fun run() {
        validate()
        if (input.endsWith(".txt") && output.endsWith(".bin") && range == null) {
            LazerSystem.assemble(Command.fromFile(input), testMode)
        } else if (input.endsWith(".bin") && output.endsWith(".csv")  && range != null) {
            LazerSystem.interpret(input, output, range!!)
        } else {
            throw UsageError("ARGUMENTS INCORRECTLY SPECIFIED")
        }
    }

    private fun validate() {
        fun validateLocalPath(path: String) {
            val file = Path.of(path)
            if (!Files.exists(file))
                throw UsageError("PATH NOT FOUND: $path")
        }

        validateLocalPath(input)
        validateLocalPath(output)

        if (range != null) {
            val (start, end) = range!!.split(":").map { it.toInt() }
            if (start > end) throw UsageError("INVALID RANGE")
            if (end > LazerSystem.MEMORY_SIZE) throw UsageError("RANGE OUT OF MEMORY BOUNDS")
        }
    }
}