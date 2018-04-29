package org.usfirst.frc.team4322.motion

import org.usfirst.frc.team4322.math.*


class PurePursuitController(private val path: Path, private val reversed: Boolean = false, val lookahead: Lookahead) {

    private var atEndOfPath = false

    fun update(pose: Transform): NavPoint {
        var pose = pose
        if (reversed) {
            pose = Transform(pose.translation,
                    pose.rotation.rotateBy(Rotation.fromRadians(Math.PI)))
        }

        val report = path.getTargetPoint(pose.translation, lookahead)
        if (isFinished()) {
            // Stop.
            return NavPoint(Twist.identity, report.closestPointDistance, report.maxSpeed, 0.0,
                    report.lookaheadPoint, report.remainingPathDistance)
        }

        val arc = Arc(pose, report.lookaheadPoint)
        var scaleFactor = 1.0
        // Ensure we don't overshoot the end of the path (once the lookahead speed drops to zero).
        if (report.lookaheadPointSpeed < 1E-6 && report.remainingPathDistance < arc.length) {
            scaleFactor = Math.max(0.0, report.remainingPathDistance / arc.length)
            atEndOfPath = true
        } else {
            atEndOfPath = false
        }
        if (reversed) {
            scaleFactor *= -1.0
        }

        return NavPoint(
                Twist(scaleFactor * arc.length, 0.0,
                        arc.length * getDirection(pose, report.lookaheadPoint).toDouble() * Math.abs(scaleFactor) / arc.radius),
                report.closestPointDistance, report.maxSpeed,
                report.lookaheadPointSpeed * Math.signum(scaleFactor), report.lookaheadPoint,
                report.remainingPathDistance)
    }

    fun hasPassedMarker(marker: String): Boolean {
        return path.hasPassedMarker(marker)
    }

    class Arc(pose: Transform, point: Translation) {
        var center: Translation
        var radius: Double = 0.toDouble()
        var length: Double = 0.toDouble()

        init {
            center = getCenter(pose, point)
            radius = Translation(center, point).norml2()
            length = getLength(pose, point, center, radius)
        }
    }

    /**
     * Gives the center of the circle joining the lookahead point and robot pose
     *
     * @param pose
     * robot pose
     * @param point
     * lookahead point
     * @return center of the circle joining the lookahead point and robot pose
     */
    companion object {
        fun getCenter(pose: Transform, point: Translation): Translation {
            val poseToPointHalfway = pose.translation.lerp(point, 0.5)
            val normal = (pose.translation.inverse() + poseToPointHalfway).direction().normal()
            val perpendicularBisector = Transform(poseToPointHalfway, normal)
            val normalFromPose = Transform(pose.translation,
                    pose.rotation.normal())
            return if (normalFromPose.isColinear(perpendicularBisector.normal())) {
                // Special case: center is poseToPointHalfway.
                poseToPointHalfway
            } else normalFromPose.intersection(perpendicularBisector)
        }

        /**
         * Gives the radius of the circle joining the lookahead point and robot pose
         *
         * @param pose
         * robot pose
         * @param point
         * lookahead point
         * @return radius of the circle joining the lookahead point and robot pose
         */
        fun getRadius(pose: Transform, point: Translation): Double {
            val center = getCenter(pose, point)
            return Translation(center, point).norml2()
        }

        /**
         * Gives the length of the arc joining the lookahead point and robot pose (assuming forward motion).
         *
         * @param pose
         * robot pose
         * @param point
         * lookahead point
         * @return the length of the arc joining the lookahead point and robot pose
         */
        fun getLength(pose: Transform, point: Translation): Double {
            val radius = getRadius(pose, point)
            val center = getCenter(pose, point)
            return getLength(pose, point, center, radius)
        }

        fun getLength(pose: Transform, point: Translation, center: Translation, radius: Double): Double {
            if (radius < 1E6) {
                val centerToPoint = Translation(center, point)
                val centerToPose = Translation(center, pose.translation)
                // If the point is behind pose, we want the opposite of this angle. To determine if the point is behind,
                // check the sign of the cross-product between the normal vector and the vector from pose to point.
                val behind = Math.signum(
                        Translation.cross(pose.rotation.normal().toTranslation(),
                                Translation(pose.translation, point))) > 0.0
                val angle = Translation.getAngle(centerToPose, centerToPoint)
                return radius * if (behind) 2.0 * Math.PI - Math.abs(angle.radians()) else Math.abs(angle.radians())
            } else {
                return Translation(pose.translation, point).norml2()
            }
        }

        /**
         * Gives the direction the robot should turn to stay on the path
         *
         * @param pose
         * robot pose
         * @param point
         * lookahead point
         * @return the direction the robot should turn: -1 is left, +1 is right
         */
        fun getDirection(pose: Transform, point: Translation): Int {
            val poseToPoint = Translation(pose.translation, point)
            val robot = pose.rotation.toTranslation()
            val cross = robot.x * poseToPoint.y - robot.y * poseToPoint.x
            return if (cross < 0) -1 else 1 // if robot < pose turn left
        }
    }

    /**
     * @return has the robot reached the end of the path
     */
    fun isFinished(): Boolean {
        return atEndOfPath
    }
}