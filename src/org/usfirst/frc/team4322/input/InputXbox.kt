package org.usfirst.frc.team4322.input

import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Joystick
import org.usfirst.frc.team4322.commandv2.ButtonTrigger
import org.usfirst.frc.team4322.logging.RobotLogger


class InputXbox
@JvmOverloads constructor(private val port: Int = 0) : Joystick(port) {
    val leftStick: Thumbstick
    val rightStick: Thumbstick
    val lt: Trigger
    val rt: Trigger
    val dPad: DirectionalPad
    val a: ButtonTrigger
    val b: ButtonTrigger
    val x: ButtonTrigger
    val y: ButtonTrigger
    val lb: ButtonTrigger
    val rb: ButtonTrigger
    val back: ButtonTrigger
    val start: ButtonTrigger

    fun leftStick() = leftStick.get()
    fun rightStick() = leftStick.get()
    fun lt() = lt.axis()
    fun rt() = rt.axis()
    fun a() = a.get()
    fun b() = b.get()
    fun x() = x.get()
    fun y() = y.get()
    fun lb() = lb.get()
    fun rb() = rb.get()
    fun back() = back.get()
    fun start() = start.get()

    private val joystick: Joystick = Joystick(port)


    init {
        leftStick = Thumbstick(joystick, HAND.LEFT)
        rightStick = Thumbstick(joystick, HAND.RIGHT)
        dPad = DirectionalPad(joystick)
        lt = Trigger(joystick, HAND.LEFT)
        rt = Trigger(joystick, HAND.RIGHT)
        a = ButtonTrigger(joystick, A_BUTTON_ID)
        b = ButtonTrigger(joystick, B_BUTTON_ID)
        x = ButtonTrigger(joystick, X_BUTTON_ID)
        y = ButtonTrigger(joystick, Y_BUTTON_ID)
        lb = ButtonTrigger(joystick, LB_BUTTON_ID)
        rb = ButtonTrigger(joystick, RB_BUTTON_ID)
        back = ButtonTrigger(joystick, BACK_BUTTON_ID)
        start = ButtonTrigger(joystick, START_BUTTON_ID)
    }// Extends Joystick...
    /* Initialize */

    /**
     * @return The port of this InputXbox
     */
    override fun getPort(): Int {
        return port
    }

    /**
     * Make the controller vibrate
     *
     * @param hand      The side of the controller to rumble
     * @param intensity How strong the rumble is
     */
    fun setRumble(hand: HAND, intensity: Double) {

        if (hand == HAND.LEFT) {
            joystick.setRumble(GenericHID.RumbleType.kLeftRumble, intensity)
        } else {
            joystick.setRumble(GenericHID.RumbleType.kRightRumble, intensity)
        }
    }

    /**
     * Make the controller vibrate
     *
     * @param intensity How strong the rumble is
     */
    fun setRumble(intensity: Double) {

        joystick.setRumble(GenericHID.RumbleType.kLeftRumble, intensity)
        joystick.setRumble(GenericHID.RumbleType.kRightRumble, intensity)
    }

    /*
	 * Set both axis deadzones of both thumbsticks
	 * @param number
	 */
    fun setDeadZone(number: Double) {
        leftStick.setDeadZone(number)
        rightStick.setDeadZone(number)
    }


    /* Get Methods */

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
                var modifiedAngle = Math.round(((Math.abs(angle) % 360) / 45).toFloat()) * 45    // May have rounding errors. Due to rounding errors.
                val all = DPAD.values()

                for (i in all.indices) {
                    if (all[i].value == modifiedAngle) {
                        return all[i]
                    }
                }

                RobotLogger.warn("[InputXbox.DPAD.getEnum()] Angle supplied ($angle) has no related DPad direction")
                return DPAD.UP
            }
        }
    }

    /* Set Methods */

    /**
     * This class is used to represent the thumbsticks on the
     * Xbox360 controller.
     */
    class Thumbstick
    /**
     * Constructor
     *
     * @param parent
     * @param hand
     */
    internal constructor(private val parent: Joystick, private val hand: HAND) : org.usfirst.frc.team4322.commandv2.Trigger() {
        private val x: JoystickAxis
        private val y: JoystickAxis
        private val buttonID: Int

        override fun get(): Boolean {
            return parent.getRawButton(buttonID)
        }

        /**
         * x
         *
         * @return x axis value with a deadzone and ramp applied
         */
        fun x(): Double = x.get()

        /**
         * getRawY
         *
         * @return y axis value with a deadzone and ramp applied
         */
        fun y(): Double = y.get()

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
        // Prevent any errors that might arise
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
                x = JoystickAxis(parent, LEFT_THUMBSTICK_X_AXIS_ID)
                y = JoystickAxis(parent, LEFT_THUMBSTICK_Y_AXIS_ID)
                buttonID = LEFT_THUMBSTICK_BUTTON_ID
            } else {                                            // If right hand
                x = JoystickAxis(parent, RIGHT_THUMBSTICK_X_AXIS_ID)
                y = JoystickAxis(parent, RIGHT_THUMBSTICK_Y_AXIS_ID)
                buttonID = RIGHT_THUMBSTICK_BUTTON_ID
            }
            x.deadband = DEFAULT_THUMBSTICK_DEADZONE
            y.deadband = DEFAULT_THUMBSTICK_DEADZONE
        }/* Initialize */

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
    class Trigger
    /**
     * Constructor
     *
     * @param parent
     * @param hand
     */
    internal constructor(/* Instance Values */
            private val parent: Joystick,
            private val hand: HAND) : org.usfirst.frc.team4322.commandv2.Trigger() {

        private var deadZone: Double = 0.0
        private var sensitivity: Double = 0.0

        /**
         * 0 = Not pressed
         * 1 = Completely pressed
         *
         * @return How far its pressed
         */
        fun axis(): Double {
                val rawInput: Double = if (hand == HAND.LEFT) {
                    parent.getRawAxis(LEFT_TRIGGER_AXIS_ID)
                } else {
                    parent.getRawAxis(RIGHT_TRIGGER_AXIS_ID)
                }

                return createDeadZone(rawInput, deadZone)
            }

        init {
            deadZone = DEFAULT_TRIGGER_DEADZONE
            sensitivity = DEFAULT_TRIGGER_SENSITIVITY
        }/* Initialize */


        /* Extended Methods */
        override fun get(): Boolean {
            return axis() > sensitivity
        }


        /* Set Methods */

        /**
         * Set the deadzone of this trigger
         *
         * @param number
         */
        fun setTriggerDeadZone(number: Double) {
            deadZone = number
        }

        /**
         * How far you need to press this trigger to activate a button press
         *
         * @param number
         */
        fun setTriggerSensitivity(number: Double) {
            sensitivity = number
        }
    }

    /**
     * This is a weird object which is essentially just 8 buttons.
     */
    class DirectionalPad
    /**
     * Constructor
     *
     * @param parent
     */
    internal constructor(/* Instance Values */
            private val parent: Joystick) {

        val up: DPadButton
        val upRight: DPadButton
        val right: DPadButton
        val downRight: DPadButton
        val down: DPadButton
        val downLeft: DPadButton
        val left: DPadButton
        val upLeft: DPadButton

        fun up() = up.get()
        fun upRight() = upRight.get()
        fun right() = right.get()
        fun downRight() = downRight.get()
        fun down() = down.get()
        fun downLeft() = downLeft.get()
        fun left() = left.get()
        fun upLeft() = upLeft.get()

        /* Get Methods */

        /**
         * Just like getAngle, but returns a direction instead of an angle
         *
         * @return A DPAD direction
         */
        val direction: DPAD
            get() = DPAD.getEnum(angle())


        init {
            up = DPadButton(this, DPAD.UP)
            upRight = DPadButton(this, DPAD.UP_RIGHT)
            right = DPadButton(this, DPAD.RIGHT)
            downRight = DPadButton(this, DPAD.DOWN_RIGHT)
            down = DPadButton(this, DPAD.DOWN)
            downLeft = DPadButton(this, DPAD.DOWN_LEFT)
            left = DPadButton(this, DPAD.LEFT)
            upLeft = DPadButton(this, DPAD.UP_LEFT)
        }/* Initialize */

        fun angle(): Int {
            return parent.pov
        }

        /**
         * This class is used to represent each of the 8 values a
         * dPad has as a button.
         */
        class DPadButton
        /**
         * Constructor
         *
         * @param parent
         * @param direction
         */
        internal constructor(private val parent: DirectionalPad, /* Instance Values */
                             private val direction: DPAD)/* Initialize */ : org.usfirst.frc.team4322.commandv2.Trigger()
        {

            /* Extended Methods */
            override fun get(): Boolean {
                return parent.angle() == direction.value
            }
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

        /**
         * Creates a deadzone, but without clipping the lower values.
         * turns this
         * |--1--2--3--4--5--|
         * into this
         * ______|-1-2-3-4-5-|
         *
         * @param input
         * @param deadZoneSize
         * @return adjusted_input
         */
        private fun createDeadZone(input: Double, deadZoneSize: Double): Double {
            val negative: Double = (if (input < 0) -1 else 1).toDouble()
            var deadZoneSizeClamp = deadZoneSize
            var adjusted: Double

            if (deadZoneSizeClamp < 0 || deadZoneSizeClamp >= 1) {
                deadZoneSizeClamp = 0.0  // Prevent any weird errors
            }

            adjusted = Math.abs(input) - deadZoneSizeClamp  // Subtract the deadzone from the magnitude
            adjusted = if (adjusted < 0) 0.0 else adjusted          // if the new input is negative, make it zero
            adjusted /= (1 - deadZoneSizeClamp)   // Adjust the adjustment so it can max at 1

            return negative * adjusted
        }
    }
}