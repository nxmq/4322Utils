package org.usfirst.frc.team4322.command

abstract class InstantCommand : Command() {
    override val isFinished: Boolean
        get() = true

    override fun initialize() {

    }

    override fun end() {
    }

    override fun interrupted() {
    }

    abstract override fun execute()
}