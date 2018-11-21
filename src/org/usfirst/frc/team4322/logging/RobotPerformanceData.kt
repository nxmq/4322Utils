package org.usfirst.frc.team4322.logging

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard

object RobotPerformanceData {
    val stats = mutableListOf<(() -> Pair<String, Any>)>()
    @JvmStatic
    fun addToLog(vararg stats: () -> Pair<String, Any>) {
        this.stats.addAll(stats)
    }

    @JvmStatic
    fun update() {

        val tab = Shuffleboard.getTab("Robot Perf Logs")
        for (entry: () -> Pair<String, Any> in stats) {
            val data = entry.invoke()
            tab.add(data.first, data.second)
        }
        Shuffleboard.update()
    }
}

