package org.usfirst.frc.team4322.commandv2

class Router(private val block: () -> Command) {
    fun route(): Command {
        return block()
    }
}

/**
 * uses a block to determine what command to run. Easiest way to include conditionals into a [CommandSet].
 */
fun router(block: () -> Command): Router {
    return Router(block)
}