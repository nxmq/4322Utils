package org.usfirst.frc.team4322.command


import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by nicolasmachado on 4/20/16.
 */
object Scheduler {
    private val core: ScheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors())
    private val systemMap: MutableMap<Subsystem, Command> = mutableMapOf()
    private var systems: ArrayList<Subsystem> = ArrayList()
    private var triggers: ArrayList<Trigger> = ArrayList()

    internal fun addSubsystem(system: Subsystem) {
        systems.add(system)
    }

    init {
        core.removeOnCancelPolicy = true
        core.continueExistingPeriodicTasksAfterShutdownPolicy = false
        core.executeExistingDelayedTasksAfterShutdownPolicy = false
        core.prestartAllCoreThreads()
    }

    @JvmOverloads
    fun add(c: Command, period: Long = 20): ScheduledFuture<*> {
        for (s in c.subsystems) {
            if (systemMap[s] != null) {
                System.err.println("Conflict!")
                val r = systemMap[s]
                if(r != null)
                {
                    r.interrupt()
                    core.remove(r)
                }
                core.purge()
                systemMap.values.removeIf { x -> x === r }
            }
            systemMap[s] = c
        }
        return core.scheduleAtFixedRate(c, 0, period, TimeUnit.MILLISECONDS)
    }

    fun addTrigger(tr: Trigger)
    {
        triggers.add(tr)
    }

    fun run() {
        for(tr in triggers) {
            tr.poll()
        }
        for (s in systems) {
            s.periodic()
            val c = s.defaultCommand
            if(c != null)
                systemMap.putIfAbsent(s,c)
            c?.start()
        }
    }

    fun remove(c: Command) {
        c.cancel()
        core.remove(c)
        systemMap.values.removeIf { x -> x === c }
    }

    fun shutdown() {
        core.shutdown()
    }

    fun reset() {
        core.shutdownNow()
        core.prestartAllCoreThreads()
    }

}
