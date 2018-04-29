package org.usfirst.frc.team4322.motion

class Lookahead(val minDistance: Double, val maxDistance: Double, val minSpeed: Double, val maxSpeed: Double) {

    protected val deltaDistance: Double = maxDistance - minDistance
    protected val deltaSpeed: Double = maxSpeed - minSpeed

    fun getLookaheadForSpeed(speed: Double): Double {
        val lookahead = deltaDistance * (speed - minSpeed) / deltaSpeed + minDistance
        return if (lookahead.isNaN()) minDistance else Math.max(minDistance, Math.min(maxDistance, lookahead))
    }
}