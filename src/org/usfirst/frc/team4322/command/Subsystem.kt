package org.usfirst.frc.team4322.command

/**
 * Created by nicolasmachado on 4/20/16.
 */
abstract class Subsystem {
    var defaultCommand: Command? = null
        protected set

    init {
        Scheduler.addSubsystem(this)
    }

    internal abstract fun initDefaultCommand()

    fun periodic() {}
}
