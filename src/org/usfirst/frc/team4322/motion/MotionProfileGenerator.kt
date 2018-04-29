package org.usfirst.frc.team4322.motion

import org.usfirst.frc.team4322.motion.MotionProfileGoal.CompletionBehavior


object MotionProfileGenerator {

    fun generateFlippedProfile(constraints: MotionProfileConstraints,
                               goal_state: MotionProfileGoal, prev_state: MotionState): MotionProfile {
        val profile = generateProfile(constraints, goal_state.flipped(), prev_state.flipped())
        for (s in profile.segments()) {
            s.start = s.start.flipped()
            s.end = s.end.flipped()
        }
        return profile
    }

    /**
     * Generate a motion profile.
     *
     * @param constraints
     * The constraints to use.
     * @param goal_state
     * The goal to use.
     * @param prev_state
     * The initial state to use.
     * @return A motion profile from prev_state to goal_state that satisfies constraints.
     */
    @Synchronized
    fun generateProfile(constraints: MotionProfileConstraints,
                        goal_state: MotionProfileGoal,
                        prev_state: MotionState): MotionProfile {
        var deltaPos = goal_state.pos - prev_state.pos
        if (deltaPos < 0.0 || deltaPos == 0.0 && prev_state.vel < 0.0) {
            // For simplicity, we always assume the goal requires positive movement. If negative, we flip to solve, then
            // flip the solution.
            return generateFlippedProfile(constraints, goal_state, prev_state)
        }
        // Invariant from this point on: deltaPos >= 0.0
        // Clamp the start state to be valid.
        var startState = MotionState(prev_state.t, prev_state.pos,
                Math.signum(prev_state.vel) * Math.min(Math.abs(prev_state.vel), constraints.maxAbsoluteVelocity),
                Math.signum(prev_state.acc) * Math.min(Math.abs(prev_state.acc), constraints.maxAbsoluteAcceleration))
        val profile = MotionProfile()
        profile.reset(startState)
        // If our velocity is headed away from the goal, the first thing we need to do is to stop.
        if (startState.vel < 0.0 && deltaPos > 0.0) {
            val stoppingTime = Math.abs(startState.vel / constraints.maxAbsoluteAcceleration)
            profile.appendControl(constraints.maxAbsoluteAcceleration, stoppingTime)
            startState = profile.endState()
            deltaPos = goal_state.pos - startState.pos
        }
        // Invariant from this point on: startState.vel() >= 0.0
        val minAbsoluteVelocityAtGoalSquared = startState.vel * startState.vel - 2.0 * constraints.maxAbsoluteAcceleration * deltaPos
        val minAbsoluteVelocityAtGoal = Math.sqrt(Math.abs(minAbsoluteVelocityAtGoalSquared))
        val maxAbsoluteVelocityAtGoal = Math.sqrt(startState.vel * startState.vel + 2.0 * constraints.maxAbsoluteAcceleration * deltaPos)
        var goalVelocity = goal_state.maxAbsoluteVelocity
        var maxAccelleration = constraints.maxAbsoluteAcceleration
        if (minAbsoluteVelocityAtGoalSquared > 0.0 && minAbsoluteVelocityAtGoal > goal_state.maxAbsoluteVelocity + goal_state.velocityTolerance) {
            // Overshoot is unavoidable with the current constraints. Look at completion_behavior to see what we should
            // do.
            when {
                goal_state.completionBehavior === CompletionBehavior.VIOLATE_MAX_ABS_VEL -> // Adjust the goal velocity.
                    goalVelocity = minAbsoluteVelocityAtGoal
                goal_state.completionBehavior === CompletionBehavior.VIOLATE_MAX_ACCEL -> {
                    if (Math.abs(deltaPos) < goal_state.positionTolerance) {
                        // Special case: We are at the goal but moving too fast. This requires 'infinite' acceleration,
                        // which will result in NaNs below, so we can return the profile immediately.
                        profile.appendSegment(MotionSegment(
                                MotionState(profile.endTime(), profile.endPos(), profile.endState().vel,
                                        java.lang.Double.NEGATIVE_INFINITY),
                                MotionState(profile.endTime(), profile.endPos(), goalVelocity, java.lang.Double.NEGATIVE_INFINITY)))
                        profile.consolidate()
                        return profile
                    }
                    // Adjust the max acceleration.
                    maxAccelleration = Math.abs(goalVelocity * goalVelocity - startState.vel * startState.vel) / (2.0 * deltaPos)
                }
                else -> {
                    // We are going to overshoot the goal, so the first thing we need to do is come to a stop.
                    val stopping_time = Math.abs(startState.vel / constraints.maxAbsoluteAcceleration)
                    profile.appendControl(-constraints.maxAbsoluteAcceleration, stopping_time)
                    // Now we need to travel backwards, so generate a flipped profile.
                    profile.appendProfile(generateFlippedProfile(constraints, goal_state, profile.endState()))
                    profile.consolidate()
                    return profile
                }
            }
        }
        goalVelocity = Math.min(goalVelocity, maxAbsoluteVelocityAtGoal)
        // Invariant from this point forward: We can achieve goalVelocity at goal_state.pos exactly using no more than +/-
        // maxAccelleration.

        // What is the maximum velocity we can reach (Vmax)? This is the intersection of two curves: one accelerating
        // towards the goal from profile.finalState(), the other coming from the goal at max vel (in reverse). If Vmax
        // is greater than constraints.max_abs_vel, we will clamp and cruise.
        // Solve the following three equations to find Vmax (by substitution):
        // Vmax^2 = Vstart^2 + 2*a*d_accel
        // Vgoal^2 = Vmax^2 - 2*a*d_decel
        // deltaPos = d_accel + d_decel
        val vMax = Math.min(constraints.maxAbsoluteVelocity,
                Math.sqrt((startState.vel * startState.vel + goalVelocity * goalVelocity) / 2.0 + deltaPos * maxAccelleration))

        // Accelerate to vMax
        if (vMax > startState.vel) {
            val accelerationTime = (vMax - startState.vel) / maxAccelleration
            profile.appendControl(maxAccelleration, accelerationTime)
            startState = profile.endState()
        }
        // Figure out how much distance will be covered during deceleration.
        val decelerationDistance = Math.max(0.0,
                (startState.vel * startState.vel - goalVelocity * goalVelocity) / (2.0 * constraints.maxAbsoluteAcceleration))
        val cruiseDistance = Math.max(0.0, goal_state.pos - startState.pos - decelerationDistance)
        // Cruise at constant velocity.
        if (cruiseDistance > 0.0) {
            val cruiseTime = cruiseDistance / startState.vel
            profile.appendControl(0.0, cruiseTime)
            startState = profile.endState()
        }
        // Decelerate to goal velocity.
        if (decelerationDistance > 0.0) {
            val decelerationTime = (startState.vel - goalVelocity) / maxAccelleration
            profile.appendControl(-maxAccelleration, decelerationTime)
        }

        profile.consolidate()
        return profile
    }
}