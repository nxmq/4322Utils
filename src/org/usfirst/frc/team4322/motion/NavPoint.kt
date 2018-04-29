package org.usfirst.frc.team4322.motion

import org.usfirst.frc.team4322.math.Translation
import org.usfirst.frc.team4322.math.Twist

class NavPoint(var delta: Twist, cross_track_error: Double, max_velocity: Double, end_velocity: Double, lookahead_point: Translation, remaining_path_length: Double) {
    var crossTrackError: Double = cross_track_error
    var maxVelocity: Double = max_velocity
    var endVelocity: Double = end_velocity
    var lookaheadPoint: Translation = lookahead_point
    var remainingPathLength: Double = remaining_path_length

}