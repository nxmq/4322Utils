package org.usfirst.frc.team4322.commandv2

import edu.wpi.first.wpilibj.Joystick

open class ButtonTrigger(private val joystick: Joystick, private val button: Int) : Trigger() {
    override fun get(): Boolean {
        return joystick.getRawButton(button)
    }
}