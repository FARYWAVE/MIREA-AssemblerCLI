fun main(args: Array<String>) {
    if (args.isEmpty()) LazerSystem.assemble()
    else ArgParser().main(args)
}