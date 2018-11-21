package org.usfirst.frc.team4322.math

class Twist(val dx: Double, val dy: Double, val dθ: Double) {

    companion object {
        val identity: Twist = Twist(0.0, 0.0, 0.0)
    }

    fun scale(scale: Double): Twist {
        return Twist(dx * scale, dy * scale, dθ * scale)
    }
}
