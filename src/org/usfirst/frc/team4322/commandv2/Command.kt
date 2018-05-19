package org.usfirst.frc.team4322.commandv2

import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.TimeUnit

abstract class Command() {
    var isStarted = false
        private set
    private var hasRun = false
    private var isDone = false
    private var timeout: Long = 0
    private var startTime: Long = 0
    private lateinit var subsystem: Subsystem
    private var job: Deferred<Unit>? = null

    constructor(timeout: Long) : this() {
        this.timeout = timeout
    }

    suspend operator fun invoke(periodMS: Long = 2): Deferred<Unit> {
        job = async(start = CoroutineStart.LAZY) {
            while (!isDone) {
                exec()
                delay(periodMS, TimeUnit.MILLISECONDS)
            }
        }
        job!!.start()
        return job!!
    }


    protected abstract fun initialize()

    protected abstract fun end()

    protected abstract fun interrupted()

    protected abstract fun resumed()

    protected abstract fun execute()

    protected abstract fun isFinished(): Boolean

    protected fun require(s: Subsystem) {
        subsystem = s
    }


    private suspend fun exec() {
        if (!isStarted) {
            subsystem.commandStack.push(job)
            if (subsystem.commandStack.size > 1)
                return
            startTime = System.currentTimeMillis()
            initialize()
            isStarted = true
        }
        val currentTop = subsystem.commandStack.peek()
        if (currentTop != job) {
            if (isStarted) {
                interrupted()
                currentTop.join()
                resumed()
            }

        }
        if (isFinished()) {
            if (!hasRun) {
                execute()
                hasRun = true
            }
            if (!isDone) {
                end()
                subsystem.commandStack.pop()
                job = null
            }
            isDone = true
        } else {
            execute()
            hasRun = true
        }
    }
}