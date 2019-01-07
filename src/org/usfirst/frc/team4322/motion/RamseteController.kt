package org.usfirst.frc.team4322.motion

class RamseteController(val path: Trajectory, val robotWheelBase: Double, val kb: Double, val kzeta: Double) {
    var seg = 0
    var lastGoalAngle = 0.0
    var goalXLast = 0.0
    var goalYLast = 0.0
    var goalXDeltaLast = 0.0
    var goalYDeltaLast = 0.0
    private var initMod = Double.NaN

    fun reset() {
        seg = 0
        lastGoalAngle = 0.0
        goalXLast = 0.0
        goalYLast = 0.0
        goalXDeltaLast = 0.0
        goalYDeltaLast = 0.0
        initMod = Double.NaN
    }

    fun isFinished(): Boolean {
        return seg + 1 == path.length()
    }

    fun run(): Pair<Double, Double> {

        if (isFinished())
            return Pair(0.0, 0.0)
        if (initMod.isNaN()) {
            initMod = Math.round((RobotPositionIntegrator.lastPose.theta - (path[0].heading + Math.PI)) / (2.0 * Math.PI)) * 2.0 * Math.PI
            if (path[seg].heading + initMod + Math.PI - lastGoalAngle >= Math.PI) {
                initMod -= path[seg].heading + initMod + Math.PI - lastGoalAngle
            }
        }


        val goalAngle = path[seg].heading + initMod + Math.PI
        // This is the path vel and can be used for vd
        val goalVelocity = -path[seg].velocity

        val goalAngleDelta = (goalAngle - lastGoalAngle) * (1.0 / path[seg].dt)
        lastGoalAngle = goalAngle

        //ez stuff
        val goalX = path[seg].x
        val goalY = path[seg].y
        val goalXDelta = (goalX - goalXLast) * (1.0 / path[seg].dt)
        val goalYDelta = (goalY - goalYLast) * (1.0 / path[seg].dt)
        goalXLast = goalX
        goalYLast = goalY
        goalXDeltaLast = goalXDelta
        goalYDeltaLast = goalYDelta


        //robot localization is needed for this part and gyro can be used for angle
        val angle = RobotPositionIntegrator.lastPose.theta
        val xError = goalX - RobotPositionIntegrator.lastPose.x
        val yError = goalY - RobotPositionIntegrator.lastPose.y


        // This is the constant function... read the paper for more info
        val k1 = 2.0 * kzeta * Math.sqrt(Math.pow(goalAngleDelta, 2.0) + kb * Math.pow(goalVelocity, 2.0))

        /*
         * Here she is! Equation 5.12. if you notice while reading the white paper equation 5.12 is wrong... it improperly implements linear projection
         * the correct implementation is (Math.cos(angle) * (gy - ry) - Math.sin(angle) * (gx - rx)) in the ramw
         */
        val angleError = Math.max(goalAngle - angle, Math.nextUp(0.0))
        val ramv = goalVelocity * Math.cos(angleError) + k1 * (Math.cos(angle) * xError + Math.sin(angle) * yError)
        var ramw = goalAngleDelta + kb * goalVelocity * (Math.sin(angleError) * (1.0 / angleError)) * (Math.cos(angle) * yError - Math.sin(angle) * xError) + k1 * angleError

        if (Math.abs(ramw) > 100) {
            ramw = 0.0
        }
        val velr = ramv + ramw * (robotWheelBase / 2.0)
        val vell = ramv - ramw * (robotWheelBase / 2.0)
        return Pair(ramv, ramw)
    }
}