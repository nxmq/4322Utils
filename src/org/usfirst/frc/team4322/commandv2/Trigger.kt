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
    private var pressCmd: Command? = null
    private var releaseCmd: Command? = null
    private var holdCmd: Command? = null
    private var cancelCmd: Command? = null
    private var toggleCmd: Command? = null
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
                holdCmd?.invoke()
            } else {
                if (holdCmd?.isRunning() != true) {
                    holdCmd?.invoke()
                }
            }
        } else if (get() && !prevState) {
            pressCmd?.invoke()
            if (toggleState) {
                toggleCmd?.cancel()
            } else {
                toggleCmd?.invoke()
            }
            toggleState = !toggleState
            cancelCmd?.cancel()
        } else if (!get() && prevState) {
                holdStarted = false
            holdCmd?.cancel()
            releaseCmd?.invoke()
        }
    }
}