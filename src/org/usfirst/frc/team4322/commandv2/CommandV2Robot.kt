package org.usfirst.frc.team4322.commandv2

import edu.wpi.first.wpilibj.TimedRobot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.usfirst.frc.team4322.dashboard.MapSynchronizer
import org.usfirst.frc.team4322.logging.RobotLogger

open class CommandV2Robot : TimedRobot() {
    var fmsHasConnected = false
    open fun dsConnected() {}
    open fun fmsConnected() {}

    override fun robotInit() {
        super.robotInit()
        MapSynchronizer.link(RobotLogger.javaClass)
        RobotLogger.initialize()
        GlobalScope.async {
            m_ds.waitForData(0.0)
            dsConnected()
        }
    }

    override fun testInit() {
        super.testInit()
        Trigger.enabled = false
    }

    override fun teleopInit() {
        super.teleopInit()
        Trigger.enabled = true
    }

    override fun autonomousInit() {
        super.autonomousInit()
        Trigger.enabled = true
    }

    override fun disabledInit() {
        super.disabledInit()
        Trigger.enabled = true
    }

    override fun robotPeriodic() {
        super.robotPeriodic()
        if (!fmsHasConnected) {
            if (m_ds.isFMSAttached) {
                fmsHasConnected = true
                fmsConnected()
                RobotLogger.switchToMatchLogging()
            }
        }
        Scheduler.update()
    }
}