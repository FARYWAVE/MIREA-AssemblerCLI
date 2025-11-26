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

    override fun run() {
        validate()
        LazerSystem.boot(Command.fromFile(input), testMode)
    }

    private fun validate() {
        fun validateLocalPath(path: String) {
            val file = Path.of(path)
            if (!Files.exists(file))
                throw UsageError("PATH NOT FOUND: $path")
        }

        validateLocalPath(input)
        validateLocalPath(output)
    }
}