package org.usfirst.frc.team4322.motion


class ProfileFollower
(val p: Double, val i: Double, val velocity: Double, val velocityFeedForward: Double, val accelerationFeedForward: Double) {

    var minOutput = Double.NEGATIVE_INFINITY
    var maxOutput = Double.POSITIVE_INFINITY
    var latestRealState: MotionState = MotionState.invalidState
    var initialState: MotionState = MotionState.invalidState
    var posError: Double = 0.0
        private set
    var velError: Double = 0.0
        private set
    private var totalError: Double = 0.0

    private var goal: MotionProfileGoal = MotionProfileGoal()
    private lateinit var constraints: MotionProfileConstraints
    private var setpointGenerator = SetpointGenerator()
    private var latestSetpoint: SetpointGenerator.Setpoint? = null

    val setpoint: MotionState
        get() = if (latestSetpoint == null) MotionState.invalidState else latestSetpoint!!.motionState

    /**
     * We are finished the profile when the final setpoint has been generated. Note that this does not check whether we
     * are anywhere close to the final setpoint, however.
     *
     * @return True if the final setpoint has been generated for the current goal.
     */
    val isFinishedProfile: Boolean
        get() = latestSetpoint != null && latestSetpoint!!.finalSetpoint

    init {
        resetProfile()
    }

    /**
     * Completely clear all state related to the current profile (min and max outputs are maintained).
     */
    fun resetProfile() {
        totalError = 0.0
        initialState = MotionState.invalidState
        latestRealState = MotionState.invalidState
        posError = Double.NaN
        velError = Double.NaN
        setpointGenerator.reset()
        resetSetpoint()
    }

    /**
     * Specify a goal and constraints for achieving the goal.
     */
    fun setGoalAndConstraints(goal: MotionProfileGoal, constraints: MotionProfileConstraints) {
        if (this.goal != goal && latestSetpoint != null) {
            // Clear the final state bit since the goal has changed.
            latestSetpoint!!.finalSetpoint = false
        }
        this.goal = goal
        this.constraints = constraints
    }

    fun setConstraints(constraints: MotionProfileConstraints) {
        setGoalAndConstraints(goal, constraints)
    }

    /**
     * Reset just the setpoint. This means that the latest_state provided to update() will be used rather than feeding
     * forward the previous setpoint the next time update() is called. This almost always forces a MotionProfile update,
     * and may be warranted if tracking error gets very large.
     */
    fun resetSetpoint() {
        latestSetpoint = null
    }

    fun resetIntegral() {
        totalError = 0.0
    }

    /**
     * Update the setpoint and apply the control gains to generate a control output.
     *
     * @param latest_state
     * The latest *actual* state, used only for feedback purposes (unless this is the first iteration or
     * reset()/resetSetpoint() was just called, in which case this is the new start state for the profile).
     * @param t
     * The timestamp for which the setpoint is desired.
     * @return An output that reflects the control output to apply to achieve the new setpoint.
     */
    @Synchronized
    fun update(latest_state: MotionState, t: Double): Double {
        latestRealState = latest_state
        var previousState = latest_state
        if (latestSetpoint != null) {
            previousState = latestSetpoint!!.motionState
        } else {
            initialState = previousState
        }
        val dt = Math.max(0.0, t - previousState.t)
        latestSetpoint = setpointGenerator.getSetpoint(constraints, goal, previousState, t)

        // Update error.
        posError = latestSetpoint!!.motionState.pos - latest_state.pos
        velError = latestSetpoint!!.motionState.vel - latest_state.vel

        // Calculate the feedforward and proportional terms.
        var output = (p * posError + velocity * velError + velocityFeedForward * latestSetpoint!!.motionState.vel
                + if (java.lang.Double.isNaN(latestSetpoint!!.motionState.acc)) 0.0 else accelerationFeedForward * latestSetpoint!!.motionState.acc)
        if (output in minOutput..maxOutput) {
            // Update integral.
            totalError += posError * dt
            output += i * totalError
        } else {
            // Reset integral windup.
            totalError = 0.0
        }
        // Clamp to limits.
        output = Math.max(minOutput, Math.min(maxOutput, output))

        return output
    }


    /**
     * We are on target if our actual state achieves the goal (where the definition of achievement depends on the goal's
     * completion behavior).
     *
     * @return True if we have actually achieved the current goal.
     */
    fun onTarget(): Boolean {
        if (latestSetpoint == null) {
            return false
        }
        // For the options that don't achieve the goal velocity exactly, also count any instance where we have passed
        // the finish line.
        val goalToStart = goal.pos - initialState.pos
        val goalToActual = goal.pos - latestRealState.pos
        val passedGoalState = Math.signum(goalToStart) * Math.signum(goalToActual) < 0.0
        return goal.atGoalState(latestRealState) || goal.completionBehavior !== MotionProfileGoal.CompletionBehavior.OVERSHOOT && passedGoalState
    }
}