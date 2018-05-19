package org.usfirst.frc.team4322.commandv2

import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.TimeUnit

abstract class Command() {
    enum class InterruptBehavior {
        Suspend,
        Terminate
    }

    private var cancelled = false
    protected var interruptBehavior = InterruptBehavior.Terminate
    private var timeout: Long = 0
    private var startTime: Long = 0
    private lateinit var subsystem: Subsystem
    private var job: Deferred<Unit>? = null

    constructor(timeout: Long) : this() {
        this.timeout = timeout
    }

    fun isRunning(): Boolean {
        return job?.isActive ?: false
    }

    fun cancel() {
        job?.cancel()
    }


    operator fun invoke(periodMS: Long = 2): Deferred<Unit> {
        job = async(start = CoroutineStart.LAZY) {
            /*******************/
            /**** INIT CODE ****/
            /*******************/
            subsystem.commandStack.push(job)
            startTime = System.currentTimeMillis()
            initialize()
            /*******************/
            /**** LOOP CODE ****/
            /*******************/
            do {
                val currentTop = subsystem.commandStack.peek()
                if (currentTop != null && currentTop != job) {
                    if (interruptBehavior == InterruptBehavior.Terminate) {
                        cancelled = true
                    } else if (interruptBehavior == InterruptBehavior.Suspend) {
                        interrupted()
                        currentTop.join()
                        resumed()
                        execute()
                    }
                } else {
                    execute()
                }
                delay(periodMS, TimeUnit.MILLISECONDS)
            } while (!isFinished() && !cancelled)
            /*******************/
            /**** END CODE ****/
            /*******************/
            end()
            subsystem.commandStack.remove(job)
            job = null
        }
        return job!!
    }


    protected open fun initialize() {

    }

    protected open fun end() {

    }

    protected open fun interrupted() {

    }

    protected open fun resumed() {

    }

    protected abstract fun execute()

    protected abstract fun isFinished(): Boolean

    protected fun require(s: Subsystem) {
        subsystem = s
    }

}