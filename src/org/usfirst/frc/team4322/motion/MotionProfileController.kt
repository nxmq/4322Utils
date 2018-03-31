package org.usfirst.frc.team4322.motion

import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX
import edu.wpi.first.wpilibj.Notifier

/*
* Class used to feed motion profile trajectories to Talon SRX using ControlMode.MotionProfile
* @author garrettluu
*/



class MotionProfileController(private val talon: WPI_TalonSRX, private val points: Array<DoubleArray>) {
    private val motionProfileStatus: MotionProfileStatus = MotionProfileStatus()
    private var position = 0.0
    private var velocity = 0.0
    private var heading = 0.0
    private var state = 0
    private var timeout = 0
    private var start = false
    var setValue = SetValueMotionProfile.Disable
        private set
    private var notifier = Notifier(PeriodicRunnable())

    internal inner class PeriodicRunnable : java.lang.Runnable {
        override fun run() {
            talon.processMotionProfileBuffer()
        }
    }

    init {
        talon.changeMotionControlFramePeriod(5)
        notifier.startPeriodic(0.05)

    }

    fun reset() {
        talon.clearMotionProfileTrajectories()
        setValue = SetValueMotionProfile.Disable
        state = 0
        timeout = -1
        start = false
    }

    fun control() {
        talon.getMotionProfileStatus(motionProfileStatus)
        when (state) {
            0 -> if (start) {
                start = false
                setValue = SetValueMotionProfile.Disable
                startFilling()
                state = 1
            }
            1 -> if (motionProfileStatus.btmBufferCnt > minBufferSize) {
                setValue = SetValueMotionProfile.Enable
                state = 2
            }
            2 -> if (motionProfileStatus.activePointValid && motionProfileStatus.isLast) {
                setValue = SetValueMotionProfile.Hold
            }
        }
        talon.getMotionProfileStatus(motionProfileStatus)
        heading = talon.activeTrajectoryHeading
        position = talon.activeTrajectoryPosition.toDouble()
        velocity = talon.activeTrajectoryVelocity.toDouble()
    }

    private fun GetTrajectoryDuration(durationMs: Int): TrajectoryPoint.TrajectoryDuration {
        /* create return value */
        var retval = TrajectoryPoint.TrajectoryDuration.Trajectory_Duration_0ms
        /* convert duration to supported type */
        retval = retval.valueOf(durationMs)
        /* check that it is valid */
        if (retval.value != durationMs) {
            println("Trajectory Duration not supported - use configMotionProfileTrajectoryPeriod instead")
        }
        /* pass to caller */
        return retval
    }

    private fun startFilling(profile: Array<DoubleArray> = points, total: Int = points.size) {
        val point = TrajectoryPoint()
        if (motionProfileStatus.hasUnderrun) {
            talon.clearMotionProfileHasUnderrun(0)
        }
        talon.clearMotionProfileTrajectories()
        talon.configMotionProfileTrajectoryPeriod(0, 10)
        for (i in 0 until total) {
            val positionRot = profile[i][0]
            val velocityRPM = profile[i][1]

            point.position = positionRot * 1024
            point.velocity = velocityRPM * 1024 / 600
            point.headingDeg = 0.0
            point.profileSlotSelect0 = 0
            point.profileSlotSelect1 = 0
            point.timeDur = GetTrajectoryDuration(profile[i][2].toInt())
            point.zeroPos = false
            if (i == 0)
                point.zeroPos = true /* set this to true on the first point */

            point.isLastPoint = false
            if (i + 1 == total)
                point.isLastPoint = true /* set this to true on the last point  */

            talon.pushMotionProfileTrajectory(point)
        }
    }

    fun startMotionProfile() {
        start = true
    }

    fun configure() {
        talon.config_kF(0, .076, 10)
        talon.config_kP(0, 2.0, 10)
        talon.config_kI(0, 0.0, 10)
        talon.config_kD(0, 20.0, 10)
        talon.configMotionAcceleration(10, 10)
        talon.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, 10)
        talon.set(ControlMode.MotionProfile, setValue.value.toDouble())
    }

    companion object {

        private val minBufferSize = 5
        private val timeoutLoops = 10
    }
}
