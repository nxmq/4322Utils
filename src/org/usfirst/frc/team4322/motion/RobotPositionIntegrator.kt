package org.usfirst.frc.team4322.motion

import org.usfirst.frc.team4322.math.Interpolable
import org.usfirst.frc.team4322.math.TemporalLerpMap

object RobotPositionIntegrator
{

    class RobotPose(val x: Double, val y: Double, val theta: Double) : Interpolable<RobotPose> {
        override fun lerp(other: RobotPose, diff: Double): RobotPose {
            return RobotPose(x * (1 - diff) + other.x * diff, y * (1 - diff) + other.y * diff, theta * (1 - diff) + other.theta * diff)
        }
    }

    private val posTracker = TemporalLerpMap<RobotPose>()
    var lastPose: RobotPose = RobotPose(0.0, 0.0, 0.0)

    fun updateWithoutGyro(timestamp: Double, leftEncoderVelocity: Double, rightEncoderVelocity: Double, wheelbase: Double) {
        val deltaT = timestamp - posTracker.getLastKey()
        var vx = (rightEncoderVelocity + leftEncoderVelocity) / 2.0
        var omega = (rightEncoderVelocity - leftEncoderVelocity) / wheelbase
        val deltaX = vx * Math.cos((lastPose.theta)) * deltaT
        val deltaY = vx * Math.sin((lastPose.theta)) * deltaT
        val newPose = RobotPose(lastPose.x + deltaX, lastPose.y + deltaY, (lastPose.theta + omega * deltaT))
        lastPose = newPose
        posTracker[timestamp] = newPose
    }

    fun updateWithGyro(timestamp: Double, leftEncoderVelocity: Double, rightEncoderVelocity: Double, gyroAngularVelocity: Double) {
        val deltaT = timestamp - posTracker.getLastKey()
        var vx = (rightEncoderVelocity + leftEncoderVelocity) / 2.0
        val deltaX = vx * Math.cos((lastPose.theta)) * deltaT
        val deltaY = vx * Math.sin((lastPose.theta)) * deltaT
        val newPose = RobotPose(lastPose.x + deltaX, lastPose.y + deltaY, (lastPose.theta + gyroAngularVelocity * deltaT))
        lastPose = newPose
        posTracker[timestamp] = newPose
    }

    fun getPoseAtTime(time: Double): RobotPose {
        return posTracker[time]
    }

    fun getCurrentPose(): RobotPose {
        return lastPose
    }

    fun reset() {
        lastPose = RobotPose(0.0, 0.0, 0.0)
    }
}