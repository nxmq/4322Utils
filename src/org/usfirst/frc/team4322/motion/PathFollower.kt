package org.usfirst.frc.team4322.motion

import org.usfirst.frc.team4322.math.Path
import org.usfirst.frc.team4322.math.Transform
import org.usfirst.frc.team4322.math.Twist
import org.usfirst.frc.team4322.motion.MotionProfileGoal.CompletionBehavior


class PathFollower(path: Path, reversed: Boolean, parameters: Parameters) {

    private var steeringController: PurePursuitController
    private var lastSteeringDelta: Twist
    private var velocityController: ProfileFollower
    private val inertiaGain: Double
    private var overrideFinished = false
    private var doneSteering = false
    var debug = DebugOutput()
        internal set

    private var maxProfileVel: Double = 0.0
    private var maxProfileAcc: Double = 0.0
    private val goalPosTolerance: Double
    private val goalVelTolerance: Double
    private val stopSteeringDistance: Double
    var crossTrackError = 0.0
        internal set
    var alongTrackError = 0.0
        internal set

    val isFinished: Boolean
        get() = (steeringController.isFinished() && velocityController.isFinishedProfile
                && velocityController.onTarget()) || overrideFinished

    class DebugOutput {
        var t: Double = 0.0
        var poseX: Double = 0.0
        var poseY: Double = 0.0
        var poseTheta: Double = 0.0
        var linearDisplacement: Double = 0.0
        var linearVelocity: Double = 0.0
        var profileDisplacement: Double = 0.0
        var profileVelocity: Double = 0.0
        var velocityCommandDx: Double = 0.0
        var velocityCommandDy: Double = 0.0
        var velocityCommandDtheta: Double = 0.0
        var steeringCommandDx: Double = 0.0
        var steeringCommandDy: Double = 0.0
        var steeringCommandDtheta: Double = 0.0
        var crossTrackError: Double = 0.0
        var alongTrackError: Double = 0.0
        var lookaheadPointX: Double = 0.0
        var lookaheadPointY: Double = 0.0
        var lookaheadPointVelocity: Double = 0.0
    }

    class Parameters(val lookahead: Lookahead, val inertiaGain: Double, val p: Double, val i: Double,
                     val velocity: Double, val velocityFeedForward: Double, val accelerationFeedForward: Double, val maxVelocity: Double,
                     val maxAcceleration: Double, val positionTolerance: Double, val velocityTolerance: Double,
                     val stop_steering_distance: Double)

    init {
        steeringController = PurePursuitController(path, reversed, parameters.lookahead)
        lastSteeringDelta = Twist.identity
        velocityController = ProfileFollower(parameters.p, parameters.i, parameters.velocity,
                parameters.velocityFeedForward, parameters.accelerationFeedForward)
        velocityController.setConstraints(
                MotionProfileConstraints(parameters.maxVelocity, parameters.maxAcceleration))
        maxProfileVel = parameters.maxVelocity
        maxProfileAcc = parameters.maxAcceleration
        goalPosTolerance = parameters.positionTolerance
        goalVelTolerance = parameters.velocityTolerance
        inertiaGain = parameters.inertiaGain
        stopSteeringDistance = parameters.stop_steering_distance
    }

    private fun execute(t: Double): Pair<Double, Double> {
        val command = update(t, RobotPositionIntegrator.getCurrentPose(), RobotPositionIntegrator.distanceDriven, RobotPositionIntegrator.lastVelocity.dx)
        if (!isFinished) {
            if (Math.abs(command.dθ) < 1e-6) {
                return Pair(command.dx, command.dx)
            }
            val deltaV = 26 * command.dθ / (2 * 0.924)
            return Pair(command.dx - deltaV, command.dx + deltaV)
        } else {
            return Pair(0.0, 0.0)
        }
    }

    /**
     * Get new velocity commands to follow the path.
     *
     * @param t
     * The current timestamp
     * @param pose
     * The current robot pose
     * @param displacement
     * The current robot displacement (total distance driven).
     * @param velocity
     * The current robot velocity.
     * @return The velocity command to apply
     */
    @Synchronized
    fun update(t: Double, pose: Transform, displacement: Double, velocity: Double): Twist {
        if (!steeringController.isFinished()) {
            val steeringCommand = steeringController.update(pose)
            debug.lookaheadPointX = steeringCommand.lookaheadPoint.x
            debug.lookaheadPointY = steeringCommand.lookaheadPoint.y
            debug.lookaheadPointVelocity = steeringCommand.endVelocity
            debug.steeringCommandDx = steeringCommand.delta.dx
            debug.steeringCommandDy = steeringCommand.delta.dy
            debug.steeringCommandDtheta = steeringCommand.delta.dθ
            crossTrackError = steeringCommand.crossTrackError
            lastSteeringDelta = steeringCommand.delta
            velocityController.setGoalAndConstraints(
                    MotionProfileGoal(displacement + steeringCommand.delta.dx,
                            Math.abs(steeringCommand.endVelocity), CompletionBehavior.VIOLATE_MAX_ACCEL,
                            goalPosTolerance, goalVelTolerance),
                    MotionProfileConstraints(Math.min(maxProfileVel, steeringCommand.maxVelocity),
                            maxProfileAcc))

            if (steeringCommand.remainingPathLength < stopSteeringDistance) {
                doneSteering = true
            }
        }

        val velocityCommand = velocityController.update(MotionState(t, displacement, velocity, 0.0), t)
        alongTrackError = velocityController.posError
        val curvature = lastSteeringDelta.dθ / lastSteeringDelta.dx
        var dtheta = lastSteeringDelta.dθ
        if (!java.lang.Double.isNaN(curvature) && Math.abs(curvature) < 1e6) {
            // Regenerate angular velocity command from adjusted curvature.
            val absoluteVelocitySetpoint = Math.abs(velocityController.setpoint.vel)
            dtheta = lastSteeringDelta.dx * curvature * (1.0 + inertiaGain * absoluteVelocitySetpoint)
        }
        val scale = velocityCommand / lastSteeringDelta.dx
        val rv = Twist(lastSteeringDelta.dx * scale, 0.0, dtheta * scale)
        return rv
    }

    fun forceFinish() {
        overrideFinished = true
    }

    fun hasPassedMarker(marker: String): Boolean {
        return steeringController.hasPassedMarker(marker)
    }
}