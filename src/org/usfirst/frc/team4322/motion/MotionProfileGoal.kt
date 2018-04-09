package org.usfirst.frc.team4322.motion

class MotionProfileGoal() {

    var pos: Double = 0.0
        protected set
    var maxAbsoluteVelocity: Double = 0.0
        protected set
    var completionBehavior = CompletionBehavior.OVERSHOOT
        protected set
    var positionTolerance = 1E-3
        protected set
    var velocityTolerance = 1E-2
        protected set

    /**
     * A goal consists of a desired position and specified maximum velocity magnitude. But what should we do if we would
     * reach the goal at a velocity greater than the maximum? This enum allows a user to specify a preference on
     * behavior in this case.
     *
     * Example use-cases of each:
     *
     * OVERSHOOT - Generally used with a goal maxAbsoluteVelocity of 0.0 to stop at the desired pos without violating any
     * constraints.
     *
     * VIOLATE_MAX_ACCEL - If we absolutely do not want to pass the goal and are unwilling to violate the maxAbsoluteVelocity
     * (for example, there is an obstacle in front of us - slam the brakes harder than we'd like in order to avoid
     * hitting it).
     *
     * VIOLATE_MAX_ABS_VEL - If the max velocity is just a general guideline and not a hard performance limit, it's
     * better to slightly exceed it to avoid skidding wheels.
     */
    enum class CompletionBehavior {
        // Overshoot the goal if necessary (at a velocity greater than maxAbsoluteVelocity) and come back.
        // Only valid if the goal velocity is 0.0 (otherwise VIOLATE_MAX_ACCEL will be used).
        OVERSHOOT,
        // If we cannot slow down to the goal velocity before crossing the goal, allow exceeding the max accel
        // constraint.
        VIOLATE_MAX_ACCEL,
        // If we cannot slow down to the goal velocity before crossing the goal, allow exceeding the goal velocity.
        VIOLATE_MAX_ABS_VEL
    }

    constructor(pos: Double) : this() {
        this.pos = pos
        this.maxAbsoluteVelocity = 0.0
        sanityCheck()
    }

    constructor(pos: Double, max_abs_vel: Double) : this() {
        this.pos = pos
        this.maxAbsoluteVelocity = max_abs_vel
        sanityCheck()
    }

    constructor(pos: Double, max_abs_vel: Double, completion_behavior: CompletionBehavior) : this() {
        this.pos = pos
        this.maxAbsoluteVelocity = max_abs_vel
        this.completionBehavior = completion_behavior
        sanityCheck()
    }

    constructor(pos: Double, max_abs_vel: Double, completion_behavior: CompletionBehavior,
                pos_tolerance: Double, vel_tolerance: Double) : this() {
        this.pos = pos
        this.maxAbsoluteVelocity = max_abs_vel
        this.completionBehavior = completion_behavior
        this.positionTolerance = pos_tolerance
        this.velocityTolerance = vel_tolerance
        sanityCheck()
    }

    constructor(other: MotionProfileGoal) : this(other.pos, other.maxAbsoluteVelocity, other.completionBehavior, other.positionTolerance, other.velocityTolerance)

    /**
     * @return A flipped MotionProfileGoal (where the position is negated, but all other attributes remain the same).
     */
    fun flipped(): MotionProfileGoal {
        return MotionProfileGoal(-pos, maxAbsoluteVelocity, completionBehavior, positionTolerance, velocityTolerance)
    }


    fun atGoalState(state: MotionState): Boolean {
        return atGoalPos(state.pos) && (Math.abs(state.vel) < maxAbsoluteVelocity + velocityTolerance || completionBehavior == CompletionBehavior.VIOLATE_MAX_ABS_VEL)
    }

    fun atGoalPos(pos: Double): Boolean {
        return (pos - positionTolerance <= this.pos) && (pos + positionTolerance >= this.pos)
    }

    /**
     * This method makes sure that the completion behavior is compatible with the max goal velocity.
     */
    protected fun sanityCheck() {
        if (maxAbsoluteVelocity > velocityTolerance && completionBehavior == CompletionBehavior.OVERSHOOT) {
            completionBehavior = CompletionBehavior.VIOLATE_MAX_ACCEL
        }
    }

    override fun toString(): String {
        return ("pos: " + pos + " (+/- " + positionTolerance + "), maxAbsoluteVelocity: " + maxAbsoluteVelocity + " (+/- " + velocityTolerance
                + "), completion behavior: " + completionBehavior.name)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MotionProfileGoal) {
            return false
        }
        val obj = other as MotionProfileGoal?
        return (obj!!.completionBehavior == completionBehavior && obj.pos == pos
                && obj.maxAbsoluteVelocity == maxAbsoluteVelocity && obj.positionTolerance == positionTolerance
                && obj.velocityTolerance == velocityTolerance)
    }
}