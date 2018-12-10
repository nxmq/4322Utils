package org.usfirst.frc.team4322.commandv2

import kotlinx.coroutines.Deferred
import java.util.concurrent.ConcurrentLinkedDeque


open class Subsystem {
    val commandStack: ConcurrentLinkedDeque<Deferred<Unit>> = ConcurrentLinkedDeque()
    internal var defaultCommand: Command? = null

    init {
        Scheduler.subsystems.add(this)
    }

    fun resetCommandQueue() {
        commandStack.forEach { it.cancel() }
        commandStack.clear()
    }

    open fun initDefaultCommand() {

    }

    open fun periodic() {

    }

    internal fun pump() {
        if (commandStack.isEmpty()) {
            defaultCommand?.start()
        }
    }
}