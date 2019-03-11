package org.usfirst.frc.team4322.commandv2

import java.util.concurrent.CopyOnWriteArrayList

abstract class Trigger {
    companion object {
        val triggers = CopyOnWriteArrayList<Trigger>()
        var enabled = true

        @JvmStatic
        fun updateTriggers() {
            if (enabled)
                triggers.forEach { it.poll() }
        }

        @JvmStatic
        fun on(trigFun: () -> Boolean): Trigger {
            return object : Trigger() {
                override fun get() = trigFun()
            }
        }
    }

    init {
        triggers.add(this)
    }

    private var prevState: Boolean = false
    private var pressCmd: Command? = null
    private var releaseCmd: Command? = null
    private var holdCmd: Command? = null
    private var cancelCmd: Command? = null
    private var toggleCmd: Command? = null
    private var holdStarted: Boolean = false
    private var toggleState: Boolean = false
    private var holdRunsLimit: Int = 0
    private var holdRunsCount: Int = 0


    operator fun invoke(): Boolean {
        return get()
    }

    abstract fun get(): Boolean

    fun whenPressed(c: Command) {
        pressCmd = c
    }

    fun whileHeld(c: Command, runs: Int = 0) {
        holdCmd = c
        holdRunsLimit = runs
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
        val cur = get()
        if (cur && prevState) {
            if (!holdStarted) {
                holdStarted = true
                holdCmd?.start()
                holdRunsCount += 1
            } else {
                if (holdCmd?.isRunning() != true && (holdRunsLimit == 0 || holdRunsCount < holdRunsLimit)) {
                    holdCmd?.start()
                }
            }
        } else if (cur && !prevState) {
            pressCmd?.start()
            if (toggleState) {
                toggleCmd?.cancel()
            } else {
                toggleCmd?.start()
            }
            toggleState = !toggleState
            cancelCmd?.cancel()
        } else if (!cur && prevState) {
            holdStarted = false
            holdRunsCount = 0
            holdCmd?.cancel()
            releaseCmd?.start()
        }
        prevState = cur
    }
}