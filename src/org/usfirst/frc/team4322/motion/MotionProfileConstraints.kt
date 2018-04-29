package org.usfirst.frc.team4322.motion

class MotionProfileConstraints(max_vel: Double, max_acc: Double) {
    var maxAbsoluteVelocity = Double.POSITIVE_INFINITY
    var maxAbsoluteAcceleration = Double.POSITIVE_INFINITY

    init {
        this.maxAbsoluteVelocity = Math.abs(max_vel)
        this.maxAbsoluteAcceleration = Math.abs(max_acc)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MotionProfileConstraints) {
            return false
        }
        val obj = other as MotionProfileConstraints?
        return obj!!.maxAbsoluteAcceleration == maxAbsoluteAcceleration
                && obj.maxAbsoluteVelocity == maxAbsoluteVelocity
    }
}