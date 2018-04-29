package org.usfirst.frc.team4322.math

object PathBuilder {

    fun buildPathFromWaypoints(w: List<Waypoint>): Path {
        val p = Path()
        if (w.size < 2)
            throw Error("Path must contain at least 2 waypoints")
        var i = 0
        if (w.size > 2) {
            do {
                Arc(getPoint(w, i), getPoint(w, i + 1), getPoint(w, i + 2)).addToPath(p)
                i++
            } while (i < w.size - 2)
        }
        Line(w[w.size - 2], w[w.size - 1]).addToPath(p, 0.0)
        p.extrapolateLast()
        p.verifySpeeds()
        // System.out.println(p);
        return p
    }

    private fun getPoint(w: List<Waypoint>, i: Int): Waypoint {
        return if (i > w.size) w[w.size - 1] else w[i]
    }

    /**
     * A waypoint along a path. Contains a position, radius (for creating curved paths), and speed. The information from
     * these waypoints is used by the PathBuilder class to generate Paths. Waypoints also contain an optional marker
     * that is used by the WaitForPathMarkerAction.
     *
     * @see PathBuilder
     *
     * @see WaitForPathMarkerAction
     */
    class Waypoint {
        internal var position: Translation
        internal var radius: Double = 0.0
        internal var speed: Double = 0.0
        internal var marker: String? = null

        constructor(other: Waypoint) : this(other.position.x, other.position.y, other.radius, other.speed, other.marker)

        constructor(x: Double, y: Double, r: Double, s: Double) {
            position = Translation(x, y)
            radius = r
            speed = s
        }

        constructor(pos: Translation, r: Double, s: Double) {
            position = pos
            radius = r
            speed = s
        }

        constructor(x: Double, y: Double, r: Double, s: Double, m: String?) {
            position = Translation(x, y)
            radius = r
            speed = s
            marker = m
        }
    }

    /**
     * A Line object is formed by two Waypoints. Contains a start and end position, slope, and speed.
     */
    internal class Line(var a: Waypoint, var b: Waypoint) {
        var start: Translation
        var end: Translation
        var slope: Translation = Translation(a.position, b.position)
        var speed: Double = 0.0

        init {
            speed = b.speed
            start = a.position + (slope.scale(a.radius / slope.norml2()))
            end = b.position + (slope.scale(-b.radius / slope.norml2()))
        }

        fun addToPath(p: Path, endSpeed: Double) {
            val pathLength = Translation(end, start).norml2()
            if (pathLength > 1e-9) {
                if (b.marker != null) {
                    p.addSegment(PathSegment(start.x, start.y, end.x, end.y, b.speed,
                            p.lastMotionState, endSpeed, b.marker))
                } else {
                    p.addSegment(PathSegment(start.x, start.y, end.x, end.y, b.speed,
                            p.lastMotionState, endSpeed))
                }
            }

        }
    }

    /**
     * An Arc object is formed by two Lines that share a common Waypoint. Contains a center position, radius, and speed.
     */
    internal class Arc(var a: Line, var b: Line) {
        var center: Translation
        var radius: Double = 0.0
        var speed: Double = 0.0

        constructor(a: Waypoint, b: Waypoint, c: Waypoint) : this(Line(a, b), Line(b, c))

        init {
            this.speed = (a.speed + b.speed) / 2
            this.center = intersect(a, b)
            this.radius = Translation(center, a.end).norml2()
        }

        fun addToPath(p: Path) {
            a.addToPath(p, speed)
            if (radius > 1e-9 && radius < 1e9) {
                p.addSegment(PathSegment(a.end.x, a.end.y, b.start.x, b.start.y, center.x, center.y,
                        speed, p.lastMotionState, b.speed))
            }
        }

        private fun intersect(l1: Line, l2: Line): Translation {
            val lineA = Transform(l1.end, Rotation(l1.slope, true).normal())
            val lineB = Transform(l2.start, Rotation(l2.slope, true).normal())
            return lineA.intersection(lineB)
        }
    }
}