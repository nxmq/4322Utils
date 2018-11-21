package org.usfirst.frc.team4322.commandv2


/**
 * uses a block to determine what command to run. Easiest way to include conditionals into a [Group].
 */
class Router(private val block: () -> Command) {
    fun route(): Command {
        return block()
    }
}
