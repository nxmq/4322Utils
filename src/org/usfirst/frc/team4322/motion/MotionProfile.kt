package org.usfirst.frc.team4322.motion

import org.usfirst.frc.team4322.math.Equality.epsilonEquals
import java.util.*


class MotionProfile {
    protected var segments = mutableListOf<MotionSegment>()

    /**
     * Checks if the given MotionProfile is valid. This checks that:
     *
     * 1. All segments are valid.
     *
     * 2. Successive segments are C1 continuous in position and C0 continuous in velocity.
     *
     * @return True if the MotionProfile is valid.
     */
    // Adjacent segments are not continuous.
    val isValid: Boolean
        get() {
            var prev_segment: MotionSegment? = null
            for (s in segments) {
                if (!s.isValid) {
                    return false
                }
                if (prev_segment != null && !s.start.coincident(prev_segment.end)) {
                    System.err.println("Segments not continuous! End: " + prev_segment.end + ", Start: " + s.start)
                    return false
                }
                prev_segment = s
            }
            return true
        }

    /**
     * Check if the profile is empty.
     *
     * @return True if there are no segments.
     */
    val isEmpty: Boolean
        get() = segments.isEmpty()

    /**
     * Create an empty MotionProfile.
     */
    constructor() {
        segments = ArrayList<MotionSegment>()
    }

    /**
     * Create a MotionProfile from an existing list of segments (note that validity is not checked).
     *
     * @param segments
     * The new segments of the profile.
     */
    constructor(segments: MutableList<MotionSegment>) {
        this.segments.clear()
        this.segments.addAll(segments)
    }

    /**
     * Get the interpolated MotionState at any given time.
     *
     * @param t
     * The time to query.
     * @return Empty if the time is outside the time bounds of the profile, or the resulting MotionState otherwise.
     */
    fun stateByTime(t: Double): Optional<MotionState> {
        if (t < startTime() && t + 1e-6 >= startTime()) {
            return Optional.of(startState())
        }
        if (t > endTime() && t - 1e-6 <= endTime()) {
            return Optional.of(endState())
        }
        for (s in segments) {
            if (s.containsTime(t)) {
                return Optional.of(s.start.extrapolate(t))
            }
        }
        return Optional.empty()
    }

    /**
     * Get the interpolated MotionState at any given time, clamping to the endpoints if time is out of bounds.
     *
     * @param t
     * The time to query.
     * @return The MotionState at time t, or closest to it if t is outside the profile.
     */
    fun stateByTimeClamped(t: Double): MotionState {
        if (t < startTime()) {
            return startState()
        } else if (t > endTime()) {
            return endState()
        }
        for (s in segments) {
            if (s.containsTime(t)) {
                return s.start.extrapolate(t)
            }
        }
        // Should never get here.
        return MotionState.invalidState
    }

    /**
     * Get the interpolated MotionState by distance (the "pos()" field of MotionState). Note that since a profile may
     * reverse, this method only returns the *first* instance of this position.
     *
     * @param pos
     * The position to query.
     * @return Empty if the profile never crosses pos or if the profile is invalid, or the resulting MotionState
     * otherwise.
     */
    fun firstStateByPos(pos: Double): Optional<MotionState> {
        for (s in segments) {
            if (s.containsPos(pos)) {
                if (epsilonEquals(s.end.pos, pos, 1e-6)) {
                    return Optional.of(s.end)
                }
                val t = Math.min(s.start.nextTimeAtPos(pos), s.end.t)
                if (t.isNaN()) {
                    System.err.println("Error! We should reach 'pos' but we don't")
                    return Optional.empty()
                }
                return Optional.of(s.start.extrapolate(t))
            }
        }
        // We never reach pos.
        return Optional.empty()
    }

    /**
     * Remove all parts of the profile prior to the query time. This eliminates whole segments and also shortens any
     * segments containing t.
     *
     * @param t
     * The query time.
     */
    fun trimBeforeTime(t: Double) {
        val iterator = segments.iterator()
        while (iterator.hasNext()) {
            val s = iterator.next()
            if (s.end.t <= t) {
                // Segment is fully before t.
                iterator.remove()
                continue
            }
            if (s.start.t <= t) {
                // Segment begins before t; let's shorten the segment.
                s.start = (s.start.extrapolate(t))
            }
            break
        }
    }

    /**
     * Remove all segments.
     */
    fun clear() {
        segments.clear()
    }

    /**
     * Remove all segments and initialize to the desired state (actually a segment of length 0 that starts and ends at
     * initial_state).
     *
     * @param initial_state
     * The MotionState to initialize to.
     */
    fun reset(initial_state: MotionState) {
        clear()
        segments.add(MotionSegment(initial_state, initial_state))
    }

    /**
     * Remove redundant segments (segments whose start and end states are coincident).
     */
    fun consolidate() {
        val iterator = segments.iterator()
        while (iterator.hasNext() && segments.size > 1) {
            val s = iterator.next()
            if (s.start.coincident(s.end)) {
                iterator.remove()
            }
        }
    }

    /**
     * Add to the profile by applying an acceleration control for a given time. This is appended to the previous last
     * state.
     *
     * @param acc
     * The acceleration to apply.
     * @param dt
     * The period of time to apply the given acceleration.
     */
    fun appendControl(acc: Double, dt: Double) {
        if (isEmpty) {
            System.err.println("Error!  Trying to append to empty profile")
            return
        }
        val lastEndState = segments[segments.size - 1].end
        val newStartState = MotionState(lastEndState.t, lastEndState.pos, lastEndState.vel, acc)
        appendSegment(MotionSegment(newStartState, newStartState.extrapolate(newStartState.t + dt)))
    }

    /**
     * Add to the profile by inserting a new segment. No validity checking is done.
     *
     * @param segment
     * The segment to add.
     */
    fun appendSegment(segment: MotionSegment) {
        segments.add(segment)
    }

    /**
     * Add to the profile by inserting a new profile after the final state. No validity checking is done.
     *
     * @param profile
     * The profile to add.
     */
    fun appendProfile(profile: MotionProfile) {
        for (s in profile.segments()) {
            appendSegment(s)
        }
    }

    /**
     * @return The number of segments.
     */
    fun size(): Int {
        return segments.size
    }

    /**
     * @return The list of segments.
     */
    fun segments(): List<MotionSegment> {
        return segments
    }

    /**
     * @return The first state in the profile (or kInvalidState if empty).
     */
    fun startState(): MotionState {
        return if (isEmpty) {
            MotionState.invalidState
        } else segments[0].start
    }

    /**
     * @return The time of the first state in the profile (or NaN if empty).
     */
    fun startTime(): Double {
        return startState().t
    }

    /**
     * @return The pos of the first state in the profile (or NaN if empty).
     */
    fun startPos(): Double {
        return startState().pos
    }

    /**
     * @return The last state in the profile (or kInvalidState if empty).
     */
    fun endState(): MotionState {
        return if (isEmpty) {
            MotionState.invalidState
        } else segments[segments.size - 1].end
    }

    /**
     * @return The time of the last state in the profile (or NaN if empty).
     */
    fun endTime(): Double {
        return endState().t
    }

    /**
     * @return The pos of the last state in the profile (or NaN if empty).
     */
    fun endPos(): Double {
        return endState().pos
    }

    /**
     * @return The duration of the entire profile.
     */
    fun duration(): Double {
        return endTime() - startTime()
    }

    /**
     * @return The total distance covered by the profile. Note that distance is the sum of absolute distances of all
     * segments, so a reversing profile will count the distance covered in each direction.
     */
    fun length(): Double {
        var length = 0.0
        for (s in segments()) {
            length += Math.abs(s.end.pos - s.start.pos)
        }
        return length
    }

    override fun toString(): String {
        val builder = StringBuilder("Profile:")
        for (s in segments()) {
            builder.append("\n\t")
            builder.append(s)
        }
        return builder.toString()
    }
}