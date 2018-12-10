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

    @JvmStatic
    fun update(timestamp: Double, leftEncoderDeltaDistance: Double, rightEncoderDeltaDistance: Double, currentGyroAngle: Double) {
        val localDX = (leftEncoderDeltaDistance + rightEncoderDeltaDistance) / 2
        val deltaTheta = (currentGyroAngle - lastPose.theta) % (2 * Math.PI)
        val deltaX = (localDX * Math.cos(lastPose.theta) * Math.sin(deltaTheta) - localDX * Math.sin(lastPose.theta) +
                localDX * Math.cos(deltaTheta) * Math.sin(lastPose.theta)) / deltaTheta
        val deltaY = (localDX * Math.cos(lastPose.theta) - localDX * Math.cos(deltaTheta) * Math.cos(lastPose.theta) +
                localDX * Math.sin(deltaTheta) * Math.sin(lastPose.theta)) / deltaTheta
        val newPose = RobotPose(lastPose.x + deltaX, lastPose.y + deltaY, lastPose.theta + deltaTheta)
        lastPose = newPose
        posTracker[timestamp] = newPose
    }

    @JvmStatic
    fun getPoseAtTime(time: Double): RobotPose {
        return posTracker[time]
    }

    @JvmStatic
    fun getCurrentPose(): RobotPose {
        return lastPose
    }

    @JvmStatic
    fun reset() {
        lastPose = RobotPose(0.0, 0.0, 0.0)
    }
}