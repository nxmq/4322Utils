package org.usfirst.frc.team4322.motion

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