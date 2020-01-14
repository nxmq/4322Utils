package org.usfirst.frc.team4322.commandv2

import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.DriverStation

/*
 * Runs the lööps
 * Just make sure to call Scheduler.update()
 * in periodic modes.
 * and Scheduler.initialize() at the end of robotInit.
 */
object Scheduler {
    init {
        HAL.report(FRCNetComm.tResourceType.kResourceType_Command, FRCNetComm.tInstances.kCommand_Scheduler)
        HAL.report(FRCNetComm.tResourceType.kResourceType_Command, FRCNetComm.tInstances.kFramework_CommandControl)

    }
    val subsystems = mutableListOf<Subsystem>()
    var commandsChanged = true

    @JvmStatic
    fun update() {
        if (DriverStation.getInstance().isTest) {
            subsystems.forEach { it.periodic() }
        } else {
            subsystems.forEach { it.pump(); it.periodic() }
        }
    }

    @JvmStatic
    fun killAllCommands() {
        subsystems.forEach { it.resetCommandQueue() }
    }
}