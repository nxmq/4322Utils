package org.usfirst.frc.team4322.commandv2

import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.TimeUnit

/**
 * The base unit of work in this library. Commands are periodic tasks which run until their [isFinished] method returns true.
 */
abstract class Command() {
    enum class InterruptBehavior {
        Suspend,
        Terminate
    }

    private var cancelled = false
    private var periodMS: Long = 2
    protected var interruptBehavior = InterruptBehavior.Terminate
    private var timeout: Long = 0
    private var startTime: Long = 0
    private lateinit var subsystem: Subsystem
    private var job: Deferred<Unit>? = null


    /**
     * Create command with timeout. Timeouts are incompatible with commands that do not terminate on suspension, and will be ignored in this case.
     */
    constructor(timeout: Long) : this() {
        this.timeout = timeout
    }

    /**
     * Returns true if the command is currently scheduled for execution.
     */
    fun isRunning(): Boolean {
        return job?.isActive ?: false
    }

    /**
     * Cancels the command's execution if it is running.
     * @return true if the command was successfully cancelled or was not running, false if the command couldnt be cancelled.
     */
    fun cancel(): Boolean {
        return job?.cancel() ?: true
    }

    /**
     * This method is called when the command finishes running.
     */
    operator fun invoke(): Deferred<Unit> {
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
            } while (!isFinished() && !cancelled && !(interruptBehavior == InterruptBehavior.Suspend || startTime + timeout > System.currentTimeMillis()))
            /*******************/
            /**** END CODE ****/
            /*******************/
            end()
            subsystem.commandStack.remove(job)
            job = null
        }
        return job!!
    }

    /**
     * This method is called when the command starts running.
     */
    protected open fun initialize() {

    }

    /**
     * This method is called when the command finishes running.
     */
    protected open fun end() {

    }

    /**
     * This method is called when another command acquires the subsystem associated with this command
     * to allow for pre-transition cleanup.
     */
    protected open fun interrupted() {

    }

    /**
     * If a command has it's [interruptBehavior] set to [InterruptBehavior.Suspend],
     * this method is called when the command is brought back to the foreground.
     */
    protected open fun resumed() {

    }

    /**
     * Main loop method for command. Runs every [periodMS] milliseconds while [isFinished] is false.
     */
    protected abstract fun execute()

    /**
     * Returns true when the Command is ready to terminate.
     */
    protected abstract fun isFinished(): Boolean

    /**
     * Sets the [Subsystem] associated with a Command. All commands _must_ have an associated [Subsystem].
     * @param [s] Subsystem to be associated with this Command.
     */
    protected fun require(s: Subsystem) {
        subsystem = s
    }

}