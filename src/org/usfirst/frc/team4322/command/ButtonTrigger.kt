package org.usfirst.frc.team4322.command

import edu.wpi.first.wpilibj.Joystick

open class ButtonTrigger(val joystick : Joystick,val button: Int) : Trigger() {
    override fun get(): Boolean {
        return joystick.getRawButton(button)
    }
}