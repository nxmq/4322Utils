package org.usfirst.frc.team4322.motion

fun main(args: Array<String>) {
    var ramseteController = RamseteController(Trajectory.load("/Users/nicolasmachado/test.csv")!!, 0.6, 1.5, .9)
    var out: Pair<Double, Double>
    while (!ramseteController.isFinished()) {
        out = ramseteController.run()
        RobotPositionIntegrator.updateWithoutGyro(0.05 * ramseteController.seg, out.first, out.second, 0.6)
        System.out.printf("%f,%f\n", RobotPositionIntegrator.getCurrentPose().x, RobotPositionIntegrator.getCurrentPose().y)
    }
}