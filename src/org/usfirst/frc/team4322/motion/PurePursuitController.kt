package org.usfirst.frc.team4322.motion

import org.joml.Matrix3f
import org.joml.Vector3f

class PurePursuitController
{
    val points : ArrayList<Waypoint> = ArrayList()
    fun loadWaypoints(list :List<Waypoint>) {
        points.clear()
        points.addAll(list)
    }

    fun execute(output : DifferentialMotorCollection) {

    }
}