package org.usfirst.frc.team4322.command

class WaitFor(private var until: () -> Boolean) : Command() {
    override val isFinished: Boolean
        get() = until()

    override fun initialize() {
    }

    override fun end() {
    }

    override fun interrupted() {
    }

    override fun execute() {
    }
}