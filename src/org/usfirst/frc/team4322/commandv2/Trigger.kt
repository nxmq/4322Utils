package org.usfirst.frc.team4322.commandv2

abstract class Trigger {

    companion object {
        val triggers = mutableListOf<Trigger>()
        @JvmStatic
        fun updateTriggers() {
            triggers.forEach { it.poll() }
        }
    }

    init {
        triggers.add(this)
    }
    private val prevState: Boolean = false
    private lateinit var pressCmd: Command
    private lateinit var releaseCmd: Command
    private lateinit var holdCmd: Command
    private lateinit var cancelCmd: Command
    private lateinit var toggleCmd: Command
    private var holdStarted: Boolean = false
    private var toggleState: Boolean = false


    operator fun invoke(): Boolean {
        return get()
    }

    abstract fun get(): Boolean

    fun whenPressed(c: Command) {
        pressCmd = c
    }


    fun whileHeld(c: Command) {
        holdCmd = c
    }

    fun whenReleased(c: Command) {
        releaseCmd = c
    }

    fun cancelWhenPressed(c: Command) {
        cancelCmd = c
    }

    fun toggleWhenPressed(c: Command) {
        toggleCmd = c
    }

    fun poll() {
        if (get() && prevState) {
            if (!holdStarted) {
                holdStarted = true
                holdCmd()
            } else {
                if (!holdCmd.isRunning()) {
                    holdCmd()
                }
            }
        } else if (get() && !prevState) {
            pressCmd()
            if (toggleState) {
                toggleCmd.cancel()
            } else {
                toggleCmd()
            }
            toggleState = !toggleState
            cancelCmd.cancel()
        } else if (!get() && prevState) {
                holdStarted = false
                holdCmd.cancel()
                releaseCmd()
        }
    }
}