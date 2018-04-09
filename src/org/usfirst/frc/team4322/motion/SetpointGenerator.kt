package org.usfirst.frc.team4322.motion


class SetpointGenerator {

    /**
     * Get the full profile from the latest call to getSetpoint(). Useful to check estimated time or distance to goal.
     *
     * @return The profile from the latest call to getSetpoint(), or null if there is not yet a profile.
     */
    var profile: MotionProfile? = null
        private set
    private var goal: MotionProfileGoal? = null
    private var constraints: MotionProfileConstraints? = null

    /**
     * A Setpoint is just a MotionState and an additional flag indicating whether this is setpoint achieves the goal
     * (useful for higher-level logic to know that it is now time to do something else).
     */
    data class Setpoint(var motionState: MotionState, var finalSetpoint: Boolean)

    /**
     * Force a reset of the profile.
     */
    fun reset() {
        profile = null
        goal = null
        constraints = null
    }

    /**
     * Get a new Setpoint (and generate a new MotionProfile if necessary).
     *
     * @param constraints
     * The constraints to use.
     * @param goal
     * The goal to use.
     * @param prev_state
     * The previous setpoint (or measured state of the system to do a reset).
     * @param t
     * The time to generate a setpoint for.
     * @return The new Setpoint at time t.
     */
    @Synchronized
    fun getSetpoint(constraints: MotionProfileConstraints, goal: MotionProfileGoal,
                    prev_state: MotionState,
                    t: Double): Setpoint {
        var regenerate = (this.constraints == null || this.constraints != constraints || this.goal == null
                || this.goal != goal || profile == null)
        if (!regenerate && !profile!!.isEmpty) {
            val expectedState = profile!!.stateByTime(prev_state.t)
            regenerate = !expectedState.isPresent || expectedState.get() != prev_state
        }
        if (regenerate) {
            // Regenerate the profile, as our current profile does not satisfy the inputs.
            this.constraints = constraints
            this.goal = goal
            profile = MotionProfileGenerator.generateProfile(constraints, goal, prev_state)
        }

        // Sample the profile at time t.
        var rv: Setpoint? = null
        if (!profile!!.isEmpty && profile!!.isValid) {
            val setpoint: MotionState
            if (t > profile!!.endTime()) {
                setpoint = profile!!.endState()
            } else if (t < profile!!.startTime()) {
                setpoint = profile!!.startState()
            } else {
                setpoint = profile!!.stateByTime(t).get()
            }
            // Shorten the profile and return the new setpoint.
            profile!!.trimBeforeTime(t)
            rv = Setpoint(setpoint, profile!!.isEmpty || this.goal!!.atGoalState(setpoint))
        }

        // Invalid or empty profile - just output the same state again.
        if (rv == null) {
            rv = Setpoint(prev_state, true)
        }

        if (rv.finalSetpoint) {
            // Ensure the final setpoint matches the goal exactly.
            rv.motionState = MotionState(rv.motionState.t, this.goal!!.pos,
                    Math.signum(rv.motionState.vel) * Math.max(this.goal!!.maxAbsoluteVelocity, Math.abs(rv.motionState.vel)),
                    0.0)
        }

        return rv
    }
}