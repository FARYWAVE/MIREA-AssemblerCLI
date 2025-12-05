import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) LazerSystem.interpret("output.bin", "result.csv", "0:10")
    else ArgParser().main(args)
}