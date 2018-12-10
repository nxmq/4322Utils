package org.usfirst.frc.team4322.commandv2

import edu.wpi.first.wpilibj.DriverStation
import org.usfirst.frc.team4322.logging.RobotPerformanceData

/*
 * Runs the lööps
 * Just make sure to call Scheduler.update()
 * in periodic modes.
 */
object Scheduler {
    val subsystems = mutableListOf<Subsystem>()

    @JvmStatic
    fun update() {
        if (DriverStation.getInstance().isTest) {
            subsystems.forEach { it.periodic() }
        } else {
            subsystems.forEach { it.pump(); it.periodic() }
        }
        Trigger.updateTriggers()
        RobotPerformanceData.update()
    }

    @JvmStatic
    fun killAllCommands() {
        subsystems.forEach { it.resetCommandQueue() }
    }
}