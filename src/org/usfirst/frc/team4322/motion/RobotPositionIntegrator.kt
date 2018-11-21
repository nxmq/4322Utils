package org.usfirst.frc.team4322.motion

import org.usfirst.frc.team4322.math.Rotation
import org.usfirst.frc.team4322.math.TemporalLerpMap
import org.usfirst.frc.team4322.math.Transform
import org.usfirst.frc.team4322.math.Twist

object RobotPositionIntegrator
{

    private val posTracker = TemporalLerpMap<Transform>()
    var distanceDriven : Double = 0.0
    var lastVelocity: Twist = Twist.identity
    var lastUpdate : Transform = Transform()

    fun update(timestamp: Double, leftEncoderDeltaDistance: Double, rightEncoderDeltaDistance: Double, currentGyroAngle: Rotation) {
        val delta = Twist(((leftEncoderDeltaDistance + rightEncoderDeltaDistance) / 2.0), 0.0, currentGyroAngle.radians())
        distanceDriven += delta.dx
        lastVelocity = delta
        lastUpdate = lastUpdate.transformBy(Transform.fromArc(delta))
        lastUpdate = Transform(lastUpdate.translation, currentGyroAngle)
        posTracker[timestamp] = lastUpdate
    }

    fun getPoseAtTime(time: Double): Transform {
        return posTracker[time]
    }

    fun getCurrentPose() : Transform {
        return lastUpdate
    }

    fun reset() {
        lastUpdate = Transform()
        lastVelocity = Twist.identity
    }

    fun getFuturePose(lookahead : Double) : Transform {
        return lastUpdate.transformBy(Transform.fromArc(lastVelocity.scale(lookahead)))
    }

}