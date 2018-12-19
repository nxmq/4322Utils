package org.usfirst.frc.team4322.motion

import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files

class Trajectory(var segments: Array<Segment>) {

    /**
     * A Trajectory Segment is a particular point in a trajectory. The segment contains the xy position and the velocity,
     * acceleration, jerk and heading at this point
     */
    class Segment(var dt: Double, var x: Double, var y: Double, var position: Double, var velocity: Double, var acceleration: Double, var jerk: Double, var heading: Double) {

        fun copy(): Segment {
            return Segment(dt, x, y, position, velocity, acceleration, jerk, heading)
        }

        fun equals(seg: Segment): Boolean {
            return seg.dt == dt && seg.x == x && seg.y == y &&
                    seg.position == position && seg.velocity == velocity &&
                    seg.acceleration == acceleration && seg.jerk == jerk && seg.heading == heading
        }

        fun fuzzyEquals(seg: Segment): Boolean {
            return ae(seg.dt, dt) && ae(seg.x, x) && ae(seg.y, y) && ae(seg.position, position) &&
                    ae(seg.velocity, velocity) && ae(seg.acceleration, acceleration) && ae(seg.jerk, jerk) &&
                    ae(seg.heading, heading)
        }

        private fun ae(one: Double, two: Double): Boolean {
            return Math.abs(one - two) < 0.0001
        }
    }

    operator fun get(index: Int): Segment {
        return segments[index]
    }

    fun length(): Int {
        return segments.size
    }

    companion object {
        @JvmStatic
        fun load(pathFile: String): Trajectory? {
            val segs = mutableListOf<Segment>()
            try {
                val csvLines = Files.readAllLines(FileSystems.getDefault().getPath(pathFile))
                for (line in csvLines) {
                    if (line.startsWith("dt"))
                        continue
                    val values = line.split(",")
                    segs.add(Segment(values[0].toDouble(),
                            values[1].toDouble(),
                            values[2].toDouble(),
                            values[3].toDouble(),
                            values[4].toDouble(),
                            values[5].toDouble(),
                            values[6].toDouble(),
                            values[7].toDouble()))
                }
                return Trajectory(segs.toTypedArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }

}