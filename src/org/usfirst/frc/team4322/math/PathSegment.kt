package org.usfirst.frc.team4322.math

import org.usfirst.frc.team4322.motion.*


class PathSegment {


    var start: Translation
        private set

    var end: Translation
        private set
    private lateinit var center: Translation
    private var deltaStart: Translation
    private lateinit var deltaEnd: Translation

    var maxSpeed: Double = 0.0
        private set
    private var isLine: Boolean = false
    private var speedController: MotionProfile? = null
    private var extrapolateLookahead: Boolean = false
    var marker: String? = null
        private set

    /**
     * @return the total length of the segment
     */
    val length: Double
        get() = if (isLine) {
            deltaStart.norml2()
        } else {
            deltaStart.norml2() * Translation.getAngle(deltaStart, deltaEnd).radians()
        }

    val endState: MotionState
        get() = speedController!!.endState()

    val startState: MotionState
        get() = speedController!!.startState()

    /**
     * Constructor for a linear segment
     *
     * @param x1
     * start x
     * @param y1
     * start y
     * @param x2
     * end x
     * @param y2
     * end y
     * @param maxSpeed
     * maximum speed allowed on the segment
     */
    constructor(x1: Double, y1: Double, x2: Double, y2: Double, maxSpeed: Double, startState: MotionState,
                endSpeed: Double) {
        start = Translation(x1, y1)
        end = Translation(x2, y2)
        deltaStart = Translation(start, end)
        this.maxSpeed = maxSpeed
        extrapolateLookahead = false
        isLine = true
        createMotionProfiler(startState, endSpeed)
    }

    constructor(x1: Double, y1: Double, x2: Double, y2: Double, maxSpeed: Double, startState: MotionState,
                endSpeed: Double, marker: String) {
        this.start = Translation(x1, y1)
        this.end = Translation(x2, y2)
        this.deltaStart = Translation(start, end)
        this.maxSpeed = maxSpeed
        extrapolateLookahead = false
        isLine = true
        this.marker = marker
        createMotionProfiler(startState, endSpeed)
    }

    /**
     * Constructor for an arc segment
     *
     * @param x1
     * start x
     * @param y1
     * start y
     * @param x2
     * end x
     * @param y2
     * end y
     * @param cx
     * center x
     * @param cy
     * center y
     * @param maxSpeed
     * maximum speed allowed on the segment
     */
    constructor(x1: Double, y1: Double, x2: Double, y2: Double, cx: Double, cy: Double, maxSpeed: Double,
                startState: MotionState, endSpeed: Double) {
        this.start = Translation(x1, y1)
        this.end = Translation(x2, y2)
        this.center = Translation(cx, cy)

        this.deltaStart = Translation(center, start)
        this.deltaEnd = Translation(center, end)

        this.maxSpeed = maxSpeed
        extrapolateLookahead = false
        isLine = false
        createMotionProfiler(startState, endSpeed)
    }

    constructor(x1: Double, y1: Double, x2: Double, y2: Double, cx: Double, cy: Double, maxSpeed: Double,
                startState: MotionState, endSpeed: Double, marker: String) {
        this.start = Translation(x1, y1)
        this.end = Translation(x2, y2)
        this.center = Translation(cx, cy)

        this.deltaStart = Translation(center, start)
        this.deltaEnd = Translation(center, end)

        this.maxSpeed = maxSpeed
        extrapolateLookahead = false
        isLine = false
        this.marker = marker
        createMotionProfiler(startState, endSpeed)
    }

    fun createMotionProfiler(start_state: MotionState, end_speed: Double) {
        val motionConstraints = MotionProfileConstraints(maxSpeed, 120.0)
        val goalState = MotionProfileGoal(length, end_speed)
        speedController = MotionProfileGenerator.generateProfile(motionConstraints, goalState, start_state)
    }

    /**
     * Set whether or not to extrapolate the lookahead point. Should only be true for the last segment in the path
     *
     * @param extrapolate
     */
    fun extrapolateLookahead(extrapolate: Boolean) {
        extrapolateLookahead = extrapolate
    }

    /**
     * Gets the point on the segment closest to the robot
     *
     * @param position
     * the current position of the robot
     * @return the point on the segment closest to the robot
     */
    fun getClosestPoint(position: Translation): Translation {
        if (isLine) {
            val delta = Translation(start, end)
            val u = ((position.x - start.x) * delta.x + (position.y - start.y) * delta.y) / (delta.x * delta.x + delta.y * delta.y)
            if (u in 0.0..1.0)
                return Translation(start.x + u * delta.x, start.y + u * delta.y)
            return if (u < 0) start else end
        } else {
            var deltaPosition = Translation(center, position)
            deltaPosition = deltaPosition.scale(deltaStart.norml2() / deltaPosition.norml2())
            return if (Translation.cross(deltaPosition, deltaStart) * Translation.cross(deltaPosition, deltaEnd) < 0) {
                center + deltaPosition
            } else {
                val startDist = Translation(position, start)
                val endDist = Translation(position, end)
                if (endDist.norml2() < startDist.norml2()) end else start
            }
        }
    }

    /**
     * Calculates the point on the segment `dist` distance from the starting point along the segment.
     *
     * @param distance
     * distance from the starting point
     * @return point on the segment `dist` distance from the starting point
     */
    fun getPointByDistance(distance: Double): Translation {
        var dist = distance
        val length = length
        if (!extrapolateLookahead && dist > length) {
            dist = length
        }
        return if (isLine) {
            start + deltaStart.scale(dist / length)
        } else {
            var deltaAngle = Translation.getAngle(deltaStart, deltaEnd).radians() * if (Translation.cross(deltaStart, deltaEnd) >= 0) 1 else -1
            deltaAngle *= dist / length
            val t = deltaStart.rotateBy(Rotation.fromRadians(deltaAngle))
            center + t
        }
    }

    /**
     * Gets the remaining distance left on the segment from point `point`
     *
     * @param position
     * result of `getClosestPoint()`
     * @return distance remaining
     */
    fun getRemainingDistance(position: Translation): Double {
        return if (isLine) {
            Translation(end, position).norml2()
        } else {
            val deltaPosition = Translation(center, position)
            val angle = Translation.getAngle(deltaEnd, deltaPosition).radians()
            val totalAngle = Translation.getAngle(deltaStart, deltaEnd).radians()
            angle / totalAngle * length
        }
    }

    private fun getDistanceTravelled(robotPosition: Translation): Double {
        val pathPosition = getClosestPoint(robotPosition)
        val remainingDist = getRemainingDistance(pathPosition)
        return length - remainingDist

    }

    fun getSpeedByDistance(distance: Double): Double {
        var dist = distance
        if (dist < speedController!!.startPos()) {
            dist = speedController!!.startPos()
        } else if (dist > speedController!!.endPos()) {
            dist = speedController!!.endPos()
        }
        val state = speedController!!.firstStateByPos(dist)
        return if (state.isPresent) {
            state.get().vel
        } else {
            println("Velocity does not exist at that position!")
            0.0
        }
    }

    fun getSpeedByClosestPoint(robotPosition: Translation): Double {
        return getSpeedByDistance(getDistanceTravelled(robotPosition))
    }

    override fun toString(): String {
        return if (isLine) {
            "(start: $start, end: $end, speed: $maxSpeed)"
        } else {
            "(start: $start, end: $end, center: $center, speed: $maxSpeed)"
        }
    }
}