package org.usfirst.frc.team4322.math

import org.usfirst.frc.team4322.motion.Lookahead
import org.usfirst.frc.team4322.motion.MotionState
import java.util.*


class Path {
    private var segments: MutableList<PathSegment> = ArrayList<PathSegment>()
    private lateinit var prevSegment: PathSegment
    private var markersCrossed = HashSet<String>()

    val endPosition: Translation
        get() = segments[segments.size - 1].end

    /**
     * @return the last MotionState in the path
     */
    val lastMotionState: MotionState
        get() {
            return if (segments.size > 0) {
                val endState = segments[segments.size - 1].endState
                MotionState(0.0, 0.0, endState.vel, endState.acc)
            } else {
                MotionState(0.0, 0.0, 0.0, 0.0)
            }
        }

    /**
     * @return the length of the current segment
     */
    val segmentLength: Double
        get() {
            val currentSegment = segments[0]
            return currentSegment.length
        }

    fun extrapolateLast() {
        val last = segments[segments.size - 1]
        last.extrapolateLookahead(true)
    }

    /**
     * add a segment to the Path
     *
     * @param segment
     * the segment to add
     */
    fun addSegment(segment: PathSegment) {
        segments.add(segment)
    }

    /**
     * get the remaining distance left for the robot to travel on the current segment
     *
     * @param robotPos
     * robot position
     * @return remaining distance on current segment
     */
    fun getSegmentRemainingDist(robotPos: Translation): Double {
        val currentSegment = segments[0]
        return currentSegment.getRemainingDistance(currentSegment.getClosestPoint(robotPos))
    }

    class TargetPointReport {
        var closestPoint: Translation = Translation.identity
        var closestPointDistance: Double = 0.0
        var closestPointSpeed: Double = 0.0
        var lookaheadPoint: Translation = Translation.identity
        var maxSpeed: Double = 0.toDouble()
        var lookaheadPointSpeed: Double = 0.0
        var remainingSegmentDistance: Double = 0.0
        var remainingPathDistance: Double = 0.0
    }

    /**
     * Gives the position of the lookahead point (and removes any segments prior to this point).
     *
     * @param robot
     * Translation of the current robot pose.
     * @return report containing everything we might want to know about the target point.
     */
    fun getTargetPoint(robot: Translation, lookahead: Lookahead): TargetPointReport {
        val rv = TargetPointReport()
        var currentSegment = segments[0]
        rv.closestPoint = currentSegment.getClosestPoint(robot)
        rv.closestPointDistance = Translation(robot, rv.closestPoint).norml2()
        rv.remainingSegmentDistance = currentSegment.getRemainingDistance(rv.closestPoint)
        rv.remainingPathDistance = rv.remainingSegmentDistance
        for (i in 1 until segments.size) {
            rv.remainingPathDistance += segments[i].length
        }
        rv.closestPointSpeed = currentSegment
                .getSpeedByDistance(currentSegment.length - rv.remainingSegmentDistance)
        var lookaheadDistance = lookahead.getLookaheadForSpeed(rv.closestPointSpeed) + rv.closestPointDistance
        if (rv.remainingSegmentDistance < lookaheadDistance && segments.size > 1) {
            lookaheadDistance -= rv.remainingSegmentDistance
            for (i in 1 until segments.size) {
                currentSegment = segments[i]
                val length = currentSegment.length
                if (length < lookaheadDistance && i < segments.size - 1) {
                    lookaheadDistance -= length
                } else {
                    break
                }
            }
        } else {
            lookaheadDistance += currentSegment.length - rv.remainingSegmentDistance
        }
        rv.maxSpeed = currentSegment.maxSpeed
        rv.lookaheadPoint = currentSegment.getPointByDistance(lookaheadDistance)
        rv.lookaheadPointSpeed = currentSegment.getSpeedByDistance(lookaheadDistance)
        checkSegmentDone(rv.closestPoint)
        return rv
    }

    /**
     * Gives the speed the robot should be traveling at the given position
     *
     * @param robotPos
     * position of the robot
     * @return speed robot should be traveling
     */
    fun getSpeed(robotPos: Translation): Double {
        val currentSegment = segments[0]
        return currentSegment.getSpeedByClosestPoint(robotPos)
    }

    /**
     * Checks if the robot has finished traveling along the current segment then removes it from the path if it has
     *
     * @param robotPos
     * robot position
     */
    fun checkSegmentDone(robotPos: Translation) {
        val currentSegment = segments[0]
        val remainingDist = currentSegment.getRemainingDistance(currentSegment.getClosestPoint(robotPos))
        if (remainingDist < 0.1) {
            removeCurrentSegment()
        }
    }

    fun removeCurrentSegment() {
        prevSegment = segments.removeAt(0)
        val marker = prevSegment.marker
        if (marker != null)
            markersCrossed.add(marker)
    }

    /**
     * Ensures that all speeds in the path are attainable and robot can slow down in time
     */
    fun verifySpeeds() {
        var maxStartSpeed = 0.0
        val startSpeeds = DoubleArray(segments.size + 1)
        startSpeeds[segments.size] = 0.0
        for (i in segments.indices.reversed()) {
            val segment = segments[i]
            maxStartSpeed += Math
                    .sqrt(maxStartSpeed * maxStartSpeed + 2 * 120 * segment.length)
            startSpeeds[i] = segment.startState.vel
            // System.out.println(maxStartSpeed + ", " + startSpeeds[i]);
            if (startSpeeds[i] > maxStartSpeed) {
                startSpeeds[i] = maxStartSpeed
                // System.out.println("Segment starting speed is too high!");
            }
            maxStartSpeed = startSpeeds[i]
        }
        for (i in segments.indices) {
            val segment = segments[i]
            val endSpeed = startSpeeds[i + 1]
            var startState = if (i > 0) segments[i - 1].endState else MotionState(0.0, 0.0, 0.0, 0.0)
            startState = MotionState(0.0, 0.0, startState.vel, startState.vel)
            segment.createMotionProfiler(startState, endSpeed)
        }
    }

    fun hasPassedMarker(marker: String): Boolean {
        return markersCrossed.contains(marker)
    }

    override fun toString(): String {
        var str = ""
        for (s in segments) {
            str += s.toString() + "\n"
        }
        return str
    }
}