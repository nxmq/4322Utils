package org.usfirst.frc.team4322.commandv2

import edu.wpi.first.wpilibj.Joystick

/**
 * A trigger that is engaged by a button being pressed.
 *
 * @property joystick The Joystick object on which the button is located.
 * @property button the number of the button on the Joystick object which triggers this trigger.
 * @constructor Creates a Button Trigger.
 */
open class ButtonTrigger(private val joystick: Joystick, private val button: Int) : Trigger() {
    override fun get(): Boolean {
        return joystick.getRawButton(button)
    }
}