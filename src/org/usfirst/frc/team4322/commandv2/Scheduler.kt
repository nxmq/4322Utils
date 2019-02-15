package org.usfirst.frc.team4322.commandv2

import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.SendableBase
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder

/*
 * Runs the lööps
 * Just make sure to call Scheduler.update()
 * in periodic modes.
 * and Scheduler.initialize() at the end of robotInit.
 */
object Scheduler : SendableBase() {
    init {
        HAL.report(FRCNetComm.tResourceType.kResourceType_Command, FRCNetComm.tInstances.kCommand_Scheduler)
        HAL.report(FRCNetComm.tResourceType.kResourceType_Command, FRCNetComm.tInstances.kFramework_CommandControl)

    }
    val subsystems = mutableListOf<Subsystem>()
    val runningCommands = mutableListOf<Command>()
    var commandsChanged = true

    @JvmStatic
    fun update() {
        if (DriverStation.getInstance().isTest) {
            subsystems.forEach { it.periodic() }
        } else {
            subsystems.forEach { it.pump(); it.periodic() }
        }
        Trigger.updateTriggers()
    }

    @JvmStatic
    fun initialize() {
        for (subsystem in subsystems) {
            subsystem.initDefaultCommand()
        }
    }

    @JvmStatic
    fun killAllCommands() {
        subsystems.forEach { it.resetCommandQueue() }
    }

    override fun initSendable(builder: SendableBuilder) {
        builder.setSmartDashboardType("Scheduler")
        val namesEntry: NetworkTableEntry = builder.getEntry("Names")
        val idsEntry: NetworkTableEntry = builder.getEntry("Ids")
        val cancelEntry: NetworkTableEntry = builder.getEntry("Cancel")
        builder.setUpdateTable {
            // Get the commands to cancel
            val toCancel = cancelEntry.getDoubleArray(DoubleArray(0))
            if (toCancel.isNotEmpty()) {
                synchronized(runningCommands) {
                    for (c in runningCommands) {
                        for (d in toCancel) {
                            if (c.hashCode() == d.toInt()) {
                                c.cancel()
                            }
                        }
                    }
                }
                cancelEntry.setDoubleArray(DoubleArray(0))
            }
            if (commandsChanged) {
                val commands = runningCommands.map { command ->
                    command.name
                }.toTypedArray()
                val ids = runningCommands.map { command ->
                    command.hashCode().toDouble()
                }.toTypedArray().toDoubleArray()
                namesEntry.setStringArray(commands)
                idsEntry.setDoubleArray(ids)
            }
        }
    }
}