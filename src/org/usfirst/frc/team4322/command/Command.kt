package org.usfirst.frc.team4322.command

import java.util.*
import java.util.concurrent.ScheduledFuture

/**
 * Created by nicolasmachado on 4/20/16.
 */
abstract class Command : Runnable {

    var isStarted = false
        private set
    private lateinit var future : ScheduledFuture<*>
    var hasRun = false
    var isDone = false
    var startTime: Long = 0
        private set
    private var timeout: Long = 0
    val subsystems : ArrayList<Subsystem>

    protected abstract val isFinished: Boolean

    constructor(subsystems: ArrayList<Subsystem>, timeout: Long) {
        this.timeout = timeout
        this.subsystems = subsystems
    }

    constructor() {
       subsystems = ArrayList()
    }

    protected fun setTimeout(millis: Long) {
        this.timeout = millis
    }

    protected abstract fun initialize()

    fun require(s: Subsystem) {
        subsystems.add(s)
    }

    fun require(vararg s: Subsystem) {
        subsystems += s
    }

    fun cancel() {
        if(!isDone) {
            end()
            isStarted = false
            isDone = true
            future.cancel(true)
        }
    }

    protected abstract fun end()

    protected abstract fun interrupted()

    open fun start() {
        if(!isStarted) {
            initialize()
            future = Scheduler.add(this)
            startTime = System.currentTimeMillis()
            isStarted = true
            hasRun = false
            isDone = false
        }
    }

    fun runTimeMillis(): Long {
        return System.currentTimeMillis() - startTime
    }

    fun shouldRun(): Boolean {
        return if (timeout > 0) {
            !isFinished && timeout < runTimeMillis()
        } else !isFinished
    }

    protected abstract fun execute()

    override fun run() {
        if (shouldRun()) {
            execute()
            if (!hasRun) {
                hasRun = true
            }
        } else {
            //All commands must run once.
            if (!hasRun) {
                execute()
                hasRun = true
            }
            cancel()
        }

    }

    internal fun interrupt() {
        interrupted()
    }
}
