package org.usfirst.frc.team4322.commandv2

class Router(private val block: () -> Command) {
    fun route(): Command {
        return block()
    }
}

fun router(block: () -> Command): Router {
    return Router(block)
}