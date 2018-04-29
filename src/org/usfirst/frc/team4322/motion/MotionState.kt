package org.usfirst.frc.team4322.motion

class MotionState(val t: Double, val pos: Double, val vel: Double, val acc: Double) {

    companion object {
        val invalidState: MotionState = MotionState(Double.NaN, Double.NaN, Double.NaN, Double.NaN)
    }


    constructor(state: MotionState) : this(state.t, state.pos, state.vel, state.acc)

    /**
     * Extrapolates this MotionState to the specified time by applying this MotionState's acceleration.
     *
     * @param t
     *            The time of the new MotionState.
     * @return A MotionState that is a valid predecessor (if t<=0) or successor (if t>=0) of this state.
     */
    fun extrapolate(t: Double): MotionState {
        return extrapolate(t, acc)
    }

    /**
     *
     * Extrapolates this MotionState to the specified time by applying a given acceleration to the (t, pos, vel) portion
     * of this MotionState.
     *
     * @param t
     *            The time of the new MotionState.
     * @param acc
     *            The acceleration to apply.
     * @return A MotionState that is a valid predecessor (if t<=0) or successor (if t>=0) of this state (with the
     *         specified accel).
     */
    fun extrapolate(t: Double, acc: Double): MotionState {
        val dt = t - this.t
        return MotionState(t, pos + vel * dt + .5 * acc * dt * dt, vel + acc * dt, acc)
    }

    /**
     * Find the next time (first time > MotionState.t()) that this MotionState will be at pos. This is an inverse of the
     * extrapolate() method.
     *
     * @param pos
     *            The position to query.
     * @return The time when we are next at pos() if we are extrapolating with a positive dt. NaN if we never reach pos.
     */
    fun nextTimeAtPos(pos: Double): Double {
        if ((this.pos - 1e-6 <= pos) && (this.pos + 1e-6 >= pos)) {
            return t
        }
        if ((acc - 1e-6 <= 0) && (acc + 1e-6 >= 0)) {
            // Zero acceleration case.
            val deltaPos = pos - this.pos
            if ((vel - 1e-6 <= 0) && (vel + 1e-6 >= 0) && Math.signum(deltaPos) == Math.signum(vel)) {
                // Constant velocity heading towards pos.
                return deltaPos / vel + t
            }
            return Double.NaN
        }

        // Solve the quadratic formula.
        // ax^2 + bx + c == 0
        // x = dt
        // a = .5 * acc
        // b = vel
        // c = this.pos - pos
        val disc = vel * vel - 2.0 * acc * (this.pos - pos)
        if (disc < 0.0) {
            // Extrapolating this MotionState never reaches the desired pos.
            return Double.NaN
        }
        val sqrtDisc = Math.sqrt(disc)
        val maxDt = (-vel + sqrtDisc) / acc
        val minDt = (-vel - sqrtDisc) / acc
        if (minDt >= 0.0 && (maxDt < 0.0 || minDt < maxDt)) {
            return t + minDt
        }
        if (maxDt >= 0.0) {
            return t + maxDt
        }
        // We only reach the desired pos in the past.
        return Double.NaN
    }


    override fun toString(): String {
        return "(t=$t, pos=$pos, vel=$vel, acc=$acc)"
    }

    /**
     * Checks if two MotionStates are epsilon-equals (all fields are equal within a nominal tolerance).
     */
    override fun equals(other: Any?): Boolean {
        return (other is MotionState) && equals(other, 1e-6)
    }

    /**
     * Checks if two MotionStates are epsilon-equals (all fields are equal within a specified tolerance).
     */
    fun equals(other: MotionState, epsilon: Double): Boolean {
        return coincident(other, epsilon) && (this.acc - epsilon <= other.acc) && (this.acc + epsilon >= other.acc)
    }

    /**
     * Checks if two MotionStates are coincident (t, pos, and vel are equal within a nominal tolerance, but acceleration
     * may be different).
     */
    fun coincident(other: MotionState): Boolean {
        return coincident(other, 1e-6)
    }

    /**
     * Checks if two MotionStates are coincident (t, pos, and vel are equal within a specified tolerance, but
     * acceleration may be different).
     */
    fun coincident(other: MotionState, epsilon: Double): Boolean {
        return (this.t - epsilon <= other.t) && (this.t + epsilon >= other.t) && (this.pos - epsilon <= other.pos) && (this.pos + epsilon >= other.pos)
                && (this.vel - epsilon <= other.vel) && (this.vel + epsilon >= other.vel)
    }

    /**
     * Returns a MotionState that is the mirror image of this one. Pos, vel, and acc are all negated, but time is not.
     */
    fun flipped(): MotionState {
        return MotionState(t, -pos, -vel, -acc)
    }
}