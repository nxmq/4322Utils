package org.usfirst.frc.team4322.motion

import org.usfirst.frc.team4322.math.Equality.epsilonEquals

class MotionSegment(var start: MotionState, var end: MotionState) {

    /**
     * Verifies that:
     *
     * 1. All segments have a constant acceleration.
     *
     * 2. All segments have monotonic position (sign of velocity doesn't change).
     *
     * 3. The time, position, velocity, and acceleration of the profile are consistent.
     */
    // Acceleration is not constant within the segment.
    // Velocity direction reverses within the segment.
    // A single segment is not consistent.
    // One allowed exception: If acc is infinite and dt is zero.
    val isValid: Boolean
        get() {
            if (!epsilonEquals(start.acc, end.acc, 1e-6)) {
                System.err.println(
                        "Segment acceleration not constant! Start acc: " + start.acc + ", End acc: " + end.acc)
                return false
            }
            if (Math.signum(start.vel) * Math.signum(end.vel) < 0.0 && !epsilonEquals(start.vel, 0.0, 1e-6)
                    && !epsilonEquals(end.vel, 0.0, 1e-6)) {
                System.err.println("Segment velocity reverses! Start vel: " + start.vel + ", End vel: " + end.vel)
                return false
            }
            if (start.extrapolate(end.t) != end) {
                if (epsilonEquals(start.t, end.t, 1e-6) && start.acc.isInfinite()) {
                    return true
                }
                System.err.println("Segment not consistent! Start: $start, End: $end")
                return false
            }
            return true
        }

    fun containsTime(t: Double): Boolean {
        return t >= start.t && t <= end.t
    }

    fun containsPos(pos: Double): Boolean {
        return pos >= start.pos && pos <= end.pos || pos <= start.pos && pos >= end.pos
    }

    override fun toString(): String {
        return "Start: $start, End: $end"
    }
}