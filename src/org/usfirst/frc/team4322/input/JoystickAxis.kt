package org.usfirst.frc.team4322.input

import edu.wpi.first.wpilibj.Joystick
import kotlin.math.absoluteValue

class JoystickAxis(val parent: Joystick, val axisNum: Int) {
    var deadband = 0.0
    var rampFunction: (Double) -> Double = { x: Double -> x }

    fun get(): Double {
        return rampFunction(unramped())
    }

    fun unramped(): Double {
        val axisValue = parent.getRawAxis(axisNum)
        if (axisValue.absoluteValue < deadband) {
            return 0.0
        } else {
            return axisValue
        }
    }
}