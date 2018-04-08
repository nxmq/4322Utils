package org.usfirst.frc.team4322.motion

import org.tenkiv.physikal.core.convertTo
import org.tenkiv.physikal.core.div
import org.tenkiv.physikal.core.plus
import org.usfirst.frc.team4322.math.Arc
import org.usfirst.frc.team4322.math.Rotation
import org.usfirst.frc.team4322.math.TemporalLerpMap
import org.usfirst.frc.team4322.math.Transform
import systems.uom.common.Imperial.INCH
import javax.measure.Quantity
import javax.measure.quantity.Length

class RobotPositionIntegrator
{

    private val posTracker = TemporalLerpMap<Transform>()
    var distanceDriven : Double = 0.0
    var lastVelocity : Arc = Arc.identity
    var lastUpdate : Transform = Transform()

    fun update(timestamp : Double, leftEncoderDeltaDistance : Quantity<Length>, rightEncoderDeltaDistance : Quantity<Length>, currentGyroAngle : Rotation) {
        val delta = Arc(((leftEncoderDeltaDistance + rightEncoderDeltaDistance) / 2.0).convertTo(INCH).value.toDouble(),0.0, lastUpdate.rotation.inverse().rotateBy(currentGyroAngle).radians())
        distanceDriven += delta.dx
        lastVelocity = delta
        lastUpdate = lastUpdate.transformBy(Transform.fromArc(delta))
        posTracker[timestamp] = lastUpdate
    }

    fun getCurrentPose() : Transform {
        return lastUpdate.transformBy(Transform.fromArc(lastVelocity))
    }

    fun getFuturePose(lookahead : Double) : Transform {
        return lastUpdate.transformBy(Transform.fromArc(lastVelocity.scale(lookahead)))
    }

}