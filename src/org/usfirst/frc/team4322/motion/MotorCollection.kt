package org.usfirst.frc.team4322.motion

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorController

class MotorCollection(master: IMotorController) {
    private var motorSet : ArrayList<IMotorController> = ArrayList()

    public var motors : ArrayList<IMotorController> = motorSet

    init {
        motorSet[0] = master
    }

    fun add(slave: IMotorController) {
        slave.follow(motorSet[0])
        motorSet.add(slave)
    }

    fun setOutput(value: Double, mode: ControlMode = ControlMode.PercentOutput) {
        motorSet[0].set(mode,value);
    }

    fun disable() {
        motorSet[0].set(ControlMode.Disabled,0.0);
    }

}