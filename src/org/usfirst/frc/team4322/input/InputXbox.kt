package org.usfirst.frc.team4322.input

import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Joystick
import org.usfirst.frc.team4322.commandv2.ButtonTrigger
import org.usfirst.frc.team4322.commandv2.Trigger


class InputXbox
@JvmOverloads constructor(port: Int = 0) : Joystick(port) {

    val leftStick: Thumbstick by lazy { Thumbstick(HAND.LEFT) }
    val rightStick: Thumbstick by lazy { Thumbstick(HAND.RIGHT) }
    val dPad: DirectionalPad by lazy { DirectionalPad() }
    val lt: XboxTrigger by lazy { XboxTrigger(HAND.LEFT) }
    val rt: XboxTrigger by lazy { XboxTrigger(HAND.RIGHT) }
    val a: ButtonTrigger by lazy { ButtonTrigger(this, A_BUTTON_ID) }
    val b: ButtonTrigger by lazy { ButtonTrigger(this, B_BUTTON_ID) }
    val x: ButtonTrigger by lazy { ButtonTrigger(this, X_BUTTON_ID) }
    val y: ButtonTrigger by lazy { ButtonTrigger(this, Y_BUTTON_ID) }
    val lb: ButtonTrigger by lazy { ButtonTrigger(this, LB_BUTTON_ID) }
    val rb: ButtonTrigger by lazy { ButtonTrigger(this, RB_BUTTON_ID) }
    val back: ButtonTrigger by lazy { ButtonTrigger(this, BACK_BUTTON_ID) }
    val start: ButtonTrigger by lazy { ButtonTrigger(this, START_BUTTON_ID) }

    /**
     * Make the controller vibrate
     *
     * @param hand      The side of the controller to rumble
     * @param intensity How strong the rumble is
     */
    fun setRumble(hand: HAND, intensity: Double) {
        if (hand == HAND.LEFT) {
            setRumble(GenericHID.RumbleType.kLeftRumble, intensity)
        } else {
            setRumble(GenericHID.RumbleType.kRightRumble, intensity)
        }
    }

    /**
     * Make the controller vibrate
     *
     * @param intensity How strong the rumble is
     */
    fun setRumble(intensity: Double) {

        setRumble(GenericHID.RumbleType.kLeftRumble, intensity)
        setRumble(GenericHID.RumbleType.kRightRumble, intensity)
    }

    /*
	 * Set both axis deadzones of both thumbsticks
	 * @param number
	 */
    fun setDeadZone(number: Double) {
        leftStick.setDeadZone(number)
        rightStick.setDeadZone(number)
    }

    /**
     * Rather than use an integer (which might not be what we expect)
     * we use an enum which has a set amount of possibilities.
     */
    enum class HAND {
        LEFT, RIGHT
    }

    /**
     * This is the relation of direction and number for .getPOV() used
     * in the DirectionalPad class.
     */
    enum class DPAD
    (/* Instance Value */
            val value: Int) {
        UP(0),
        UP_RIGHT(45),
        RIGHT(90),
        DOWN_RIGHT(135),
        DOWN(180),
        DOWN_LEFT(225),
        LEFT(270),
        UP_LEFT(315);


        companion object {

            /**
             * Convert integers to DPAD values
             *
             * @param angle
             * @return DPAD with matching angle
             */
            fun getEnum(angle: Int): DPAD {
                var modifiedAngle: Int = (Math.abs(angle) % 360)
                if ((modifiedAngle % 45) != 0) {
                    modifiedAngle = ((modifiedAngle / 45) + 1) * 45
                }
                return DPAD.values()[modifiedAngle / 45]
            }
        }
    }

    /* Set Methods */

    /**
     * This class is used to represent the thumbsticks on the
     * Xbox360 controller.
     */
    inner class Thumbstick
    /**
     * Constructor
     *
     * @param hand
     */
    internal constructor(private val hand: HAND) : Trigger() {
        val x: JoystickAxis
        val y: JoystickAxis
        private val buttonID: Int

        override fun get(): Boolean {
            return this@InputXbox.getRawButton(buttonID)
        }

        /**
         * 0    = Up;
         * 90   = Right;
         * ...
         *
         * @return Angle the thumbstick is pointing
         */
        val angle: Double
            get() {
                val angle = Math.atan2(x.unramped(), x.unramped())

                return Math.toDegrees(angle)
            }

        /**
         * getMagnitude
         *
         * @return A number between 0 and 1
         */
        val magnitude: Double
            get() {
                var magnitude = scaleMagnitude(x.unramped(), y.unramped())

                if (magnitude > 1) {
                    magnitude = 1.0
                }

                return magnitude
            }

        init {
            if (hand == HAND.LEFT) {
                x = JoystickAxis(this@InputXbox, LEFT_THUMBSTICK_X_AXIS_ID)
                y = JoystickAxis(this@InputXbox, LEFT_THUMBSTICK_Y_AXIS_ID)
                buttonID = LEFT_THUMBSTICK_BUTTON_ID
            } else {                                            // If right hand
                x = JoystickAxis(this@InputXbox, RIGHT_THUMBSTICK_X_AXIS_ID)
                y = JoystickAxis(this@InputXbox, RIGHT_THUMBSTICK_Y_AXIS_ID)
                buttonID = RIGHT_THUMBSTICK_BUTTON_ID
            }
            x.deadband = DEFAULT_THUMBSTICK_DEADZONE
            y.deadband = DEFAULT_THUMBSTICK_DEADZONE
        }

        /**
         * magnitude
         *
         * @param x
         * @param y
         * @return Magnitude of thing
         */
        private fun magnitude(x: Double, y: Double): Double {
            val xSquared = Math.pow(x, 2.0)
            val ySquared = Math.pow(y, 2.0)

            return Math.sqrt(xSquared + ySquared)
        }

        /**
         * angleToSquareSpace
         *
         * @param angle
         * @return Number between 0 and PI/4
         */
        private fun angleToSquareSpace(angle: Double): Double {
            val absAngle = Math.abs(angle)
            val halfPi = Math.PI / 2
            val quarterPi = Math.PI / 4
            val modulus = absAngle % halfPi

            return -Math.abs(modulus - quarterPi) + quarterPi
        }

        /**
         * scaleMagnitude
         *
         * @param x
         * @param y
         * @return
         */
        private fun scaleMagnitude(x: Double, y: Double): Double {
            val magnitude = magnitude(x, y)
            val angle = Math.atan2(x, y)
            val newAngle = angleToSquareSpace(angle)
            val scaleFactor = Math.cos(newAngle)

            return magnitude * scaleFactor
        }


        /**
         * Set both axis deadzones of this thumbstick
         *
         * @param number
         */
        fun setDeadZone(number: Double) {
            x.deadband = number
            y.deadband = number
        }
    }

    /**
     * This class is used to represent one of the two
     * Triggers on an Xbox360 controller.
     */
    inner class XboxTrigger
    /**
     * Constructor
     *
     * @param hand
     */
    internal constructor(private val hand: HAND) : Trigger() {

        var deadZone: Double = 0.0
        var sensitivity: Double = 0.0

        /**
         * 0 = Not pressed
         * 1 = Completely pressed
         *
         * @return How far its pressed
         */
        fun axis(): Double {
                val rawInput: Double = if (hand == HAND.LEFT) {
                    this@InputXbox.getRawAxis(LEFT_TRIGGER_AXIS_ID)
                } else {
                    this@InputXbox.getRawAxis(RIGHT_TRIGGER_AXIS_ID)
                }

            return if (rawInput < deadZone) 0.0 else rawInput
            }

        init {
            deadZone = DEFAULT_TRIGGER_DEADZONE
            sensitivity = DEFAULT_TRIGGER_SENSITIVITY
        }/* Initialize */


        /* Extended Methods */
        override fun get(): Boolean {
            return axis() > sensitivity
        }

    }

    /**
     * This is a weird object which is essentially just 8 buttons.
     */
    inner class DirectionalPad internal constructor() {

        val up: DPadButton by lazy { DPadButton(DPAD.UP) }
        val upRight: DPadButton by lazy { DPadButton(DPAD.UP_RIGHT) }
        val right: DPadButton by lazy { DPadButton(DPAD.RIGHT) }
        val downRight: DPadButton by lazy { DPadButton(DPAD.DOWN_RIGHT) }
        val down: DPadButton by lazy { DPadButton(DPAD.DOWN) }
        val downLeft: DPadButton by lazy { DPadButton(DPAD.DOWN_LEFT) }
        val left: DPadButton by lazy { DPadButton(DPAD.LEFT) }
        val upLeft: DPadButton by lazy { DPadButton(DPAD.UP_LEFT) }


        /**
         * Just like getAngle, but returns a direction instead of an angle
         *
         * @return A DPAD direction
         */
        val direction: DPAD
            get() = DPAD.getEnum(angle())


        fun angle(): Int {
            return this@InputXbox.pov
        }

        /**
         * This class is used to represent each of the 8 values a
         * dPad has as a button.
         */
        inner class DPadButton internal constructor(private val direction: DPAD) : Trigger() {
            override fun get(): Boolean = this@DirectionalPad.angle() == direction.value
        }
    }

    companion object {

        /* Default Values */
        private const val DEFAULT_THUMBSTICK_DEADZONE = 0.02  // Jiggle room for the thumbsticks
        private const val DEFAULT_TRIGGER_DEADZONE = 0.01 // Jiggle room for the triggers
        private const val DEFAULT_TRIGGER_SENSITIVITY = 0.6  // If the trigger is beyond this limit, say it has been pressed

        /* Button Mappings */
        private const val A_BUTTON_ID = 1
        private const val B_BUTTON_ID = 2
        private const val X_BUTTON_ID = 3
        private const val Y_BUTTON_ID = 4
        private const val LB_BUTTON_ID = 5
        private const val RB_BUTTON_ID = 6
        private const val BACK_BUTTON_ID = 7
        private const val START_BUTTON_ID = 8
        private const val LEFT_THUMBSTICK_BUTTON_ID = 9
        private const val RIGHT_THUMBSTICK_BUTTON_ID = 10

        /* Axis Mappings */
        private const val LEFT_THUMBSTICK_X_AXIS_ID = 0
        private const val LEFT_THUMBSTICK_Y_AXIS_ID = 1
        private const val LEFT_TRIGGER_AXIS_ID = 2
        private const val RIGHT_TRIGGER_AXIS_ID = 3
        private const val RIGHT_THUMBSTICK_X_AXIS_ID = 4
        private const val RIGHT_THUMBSTICK_Y_AXIS_ID = 5
    }
}