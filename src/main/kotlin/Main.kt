fun main(args: Array<String>) {
    if (args.isEmpty()) LazerSystem.boot()
    else ArgParser().main(args)
}