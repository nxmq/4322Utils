package org.usfirst.frc.team4322.commandv2

import kotlinx.coroutines.Deferred
import java.util.concurrent.ConcurrentLinkedDeque


open class Subsystem {
    val commandStack: ConcurrentLinkedDeque<Deferred<Unit>> = ConcurrentLinkedDeque()
    var defaultCommand: Command? = null
        protected set
    var defaultCommandInitialized = false
    var currentCommandName: String = ""

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
            if (defaultCommand == null && !defaultCommandInitialized) {
                initDefaultCommand()
                defaultCommandInitialized = true
            }
            defaultCommand?.start()
        }
    }

    /**
     * Returns the default command title, or empty string is there is none.
     *
     * @return the default command title
     */
    fun getDefaultCommandName(): String {
        val defaultCommand = this.defaultCommand
        return defaultCommand?.javaClass?.name ?: ""
    }
}