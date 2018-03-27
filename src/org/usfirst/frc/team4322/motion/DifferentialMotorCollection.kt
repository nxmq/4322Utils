package org.usfirst.frc.team4322.motion

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorController
import org.tenkiv.physikal.core.div
import org.tenkiv.physikal.core.radian
import org.tenkiv.physikal.core.second

class DifferentialMotorCollection(masterLeft: IMotorController, masterRight: IMotorController) {
    val foo = Math.PI.radian / 1.0.second
    private var left: MotorCollection = MotorCollection(masterLeft)
    private var right: MotorCollection = MotorCollection(masterRight)

    fun addLeft(slave: IMotorController) {
        left.add(slave)
    }
    fun addRight(slave: IMotorController) {
        right.add(slave)
    }

    fun setOutput(value: Double, mode: ControlMode = ControlMode.PercentOutput) {
        left.motors[0].set(mode,value)
        right.motors[0].set(mode,-value)
    }
    fun setOutput(valueLeft: Double,valueRight: Double, mode: ControlMode = ControlMode.PercentOutput) {
        left.motors[0].set(mode,valueLeft)
        right.motors[0].set(mode,valueRight)
    }

    fun disable() {
        left.motors[0].set(ControlMode.Disabled,0.0)
        right.motors[0].set(ControlMode.Disabled,0.0)
    }
}