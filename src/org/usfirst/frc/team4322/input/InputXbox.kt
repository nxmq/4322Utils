package org.usfirst.frc.team4322.input

import edu.wpi.first.wpilibj.GenericHID
import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj.buttons.JoystickButton
import org.usfirst.frc.team4322.command.ButtonTrigger
import org.usfirst.frc.team4322.command.Trigger
import org.usfirst.frc.team4322.logging.RobotLogger


/**
 * [class] InputXbox
 *
 * @author AJ Granowski & 4624 Owatonna Robotics
 * @version 2015
 *
 *
 * This class wraps around the Joystick class in order to make
 * working with Xbox360 controllers less of a pain.
 *
 *
 * The values from this class can be used in two ways. One could
 * either check each Button every cycle with .get(), or they
 * could call commands directly from the Buttons with .whenPressed()
 *
 *
 * USAGE:
 * // Initialization
 * myXboxController = new InputXbox( <port the controller is on (starts at 0)> );
 * myXboxController.leftStick.setThumbstickDeadZone( .2 );  // Optional. See code below for defaults.
</port> *
 *
 * // Using buttons
 * myXboxController.a.whenPressed( new MyCommand() );
 * myXboxController.lb.toggleWhenPressed( new MyCommand() );
 * myXboxController.rightStick.whenPressed( new MyCommand() );
 *
 *
 * // Getting values directly
 * if( myXboxController.leftStick.getY() > .4 ) ...
 *
 *
 * // Support of legacy methods (NOTE: These values are straight from the Joystick class. No deadzone stuff or anything)
 * if( xboxController.getX() > .4 ) ...
 *
 *
 * NOTES:
 * Although I have confidence that this will work, not everything has been tested.
 * This should work for the 2015 WPILib. The mappings of axis's and buttons may change in later years.
 * I am not a good Java programmer.
 */
class InputXbox
/**
 * (Constructor #1)
 * There are two ways to make an InputXbox. With this constructor,
 * you can specify which port you expect the controller to be on.
 *
 * @param port
 */
@JvmOverloads constructor(/* Instance Values */
        private val port: Int = 0) : Joystick(port) {
    public val leftStick: Thumbstick
    public val rightStick: Thumbstick
    public val lt: Trigger
    public val rt: Trigger
    public val dPad: DirectionalPad
    public val a: ButtonTrigger
    public val b: ButtonTrigger
    public val x: ButtonTrigger
    public val y: ButtonTrigger
    public val lb: ButtonTrigger
    public val rb: ButtonTrigger
    public val back: ButtonTrigger
    public val start: ButtonTrigger

    private val joystick: Joystick = Joystick(this.port)


    init {
        // Joystick referenced by everything
        this.leftStick = Thumbstick(this.joystick, HAND.LEFT)
        this.rightStick = Thumbstick(this.joystick, HAND.RIGHT)
        this.dPad = DirectionalPad(this.joystick)
        this.lt = Trigger(this.joystick, HAND.LEFT)
        this.rt = Trigger(this.joystick, HAND.RIGHT)
        this.a =ButtonTrigger(this.joystick, A_BUTTON_ID)
        this.b = ButtonTrigger(this.joystick, B_BUTTON_ID)
        this.x = ButtonTrigger(this.joystick, X_BUTTON_ID)
        this.y = ButtonTrigger(this.joystick, Y_BUTTON_ID)
        this.lb = ButtonTrigger(this.joystick, LB_BUTTON_ID)
        this.rb = ButtonTrigger(this.joystick, RB_BUTTON_ID)
        this.back = ButtonTrigger(this.joystick, BACK_BUTTON_ID)
        this.start = ButtonTrigger(this.joystick, START_BUTTON_ID)
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
    /**
     * Constructor
     *
     * @param value
     */
    private constructor(/* Instance Value */
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
                var angle = angle
                angle = Math.abs(angle)
                angle %= 360
                angle = Math.round((angle / 45).toFloat()) * 45    // May have rounding errors. Due to rounding errors.

                val all = DPAD.values()

                for (i in all.indices) {
                    if (all[i].value == angle) {
                        return all[i]
                    }
                }
                // I don't know what to do here
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
    internal constructor(private val parent: Joystick, val hand: HAND) : org.usfirst.frc.team4322.command.Trigger() {
        private val xAxisID: Int
        private val yAxisID: Int
        private val buttonID: Int
        private var xDeadZone: Double = 0.toDouble()
        private var yDeadZone: Double = 0.toDouble()

        override fun get(): Boolean {
            return parent.getRawButton(buttonID)
        }

        /**
         * getRawX
         *
         * @return X with a deadzone
         */
        val x: Double
            get() = rawX()

        /**
         * getRawY
         *
         * @return Y with a deadzone
         */
        val y: Double
            get() = rawY()

        /**
         * 0    = Up;
         * 90   = Right;
         * ...
         *
         * @return Angle the thumbstick is pointing
         */
        val angle: Double
            get() {
                val angle = Math.atan2(rawX(), rawY())

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
                var magnitude = scaleMagnitude(rawX(), rawY())

                if (magnitude > 1) {
                    magnitude = 1.0
                }

                return magnitude
            }

        /**
         * Get the adjusted thumbstick position (Magnitude <= 1)
         *
         * @return True thumbstick position
         */
        val trueX: Double
            get() {
                val x = rawX()
                val y = rawY()
                val angle = Math.atan2(x, y)

                return scaleMagnitude(x, y) * Math.sin(angle)
            }

        /**
         * Get the adjusted thumbstick position (Magnitude <= 1)
         *
         * @return True thumbstick position
         */
        val trueY: Double
            get() {
                val x = rawX()
                val y = rawY()
                val angle = Math.atan2(x, y)

                return scaleMagnitude(x, y) * Math.cos(angle)
            }


        init {
            this.xDeadZone = DEFAULT_THUMBSTICK_DEADZONE
            this.yDeadZone = DEFAULT_THUMBSTICK_DEADZONE

            if (hand == HAND.LEFT) {
                this.xAxisID = LEFT_THUMBSTICK_X_AXIS_ID
                this.yAxisID = LEFT_THUMBSTICK_Y_AXIS_ID
                this.buttonID = LEFT_THUMBSTICK_BUTTON_ID
            } else {                                            // If right hand
                this.xAxisID = RIGHT_THUMBSTICK_X_AXIS_ID
                this.yAxisID = RIGHT_THUMBSTICK_Y_AXIS_ID
                this.buttonID = RIGHT_THUMBSTICK_BUTTON_ID
            }
        }/* Initialize */


        /**
         * + = right
         * - = left
         *
         * @return X but with a deadzone
         */
        private fun rawX(): Double {
            val rawInput = parent.getRawAxis(xAxisID)

            return createDeadZone(rawInput, xDeadZone)
        }

        /**
         * + = up
         * - = down
         *
         * @return Y but with a deadzone
         */
        private fun rawY(): Double {
            val rawInput = -parent.getRawAxis(yAxisID)    // -Y was up on our thumbsticks. Consider this a fix?

            return createDeadZone(rawInput, yDeadZone)
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

        /* Set Methods */

        /**
         * Set the X axis deadzone of this thumbstick
         *
         * @param number
         */
        fun setXDeadZone(number: Double) {
            xDeadZone = number
        }

        /**
         * Set the Y axis deadzone of this thumbstick
         *
         * @param number
         */
        fun setYDeadZone(number: Double) {
            yDeadZone = number
        }

        /**
         * Set both axis deadzones of this thumbstick
         *
         * @param number
         */
        fun setDeadZone(number: Double) {
            xDeadZone = number
            yDeadZone = number
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
     * @param joystick
     * @param hand
     */
    internal constructor(/* Instance Values */
            private val parent: Joystick,
            val hand: HAND) : org.usfirst.frc.team4322.command.Trigger() {

        private var deadZone: Double = 0.toDouble()
        private var sensitivity: Double = 0.toDouble()

        /**
         * 0 = Not pressed
         * 1 = Completely pressed
         *
         * @return How far its pressed
         */
        val x: Double
            get() {
                val rawInput: Double

                if (hand == HAND.LEFT) {
                    rawInput = parent.getRawAxis(LEFT_TRIGGER_AXIS_ID)
                } else {
                    rawInput = parent.getRawAxis(RIGHT_TRIGGER_AXIS_ID)
                }

                return createDeadZone(rawInput, deadZone)
            }

        // Triggers have one dimensional movement. Use getX() instead
        val y: Double
            get() = x


        init {
            this.deadZone = DEFAULT_TRIGGER_DEADZONE
            this.sensitivity = DEFAULT_TRIGGER_SENSITIVITY
        }/* Initialize */


        /* Extended Methods */
        override fun get(): Boolean {
            return x > sensitivity
        }


        /* Set Methods */

        /**
         * Set the deadzone of this trigger
         *
         * @param number
         */
        fun setTriggerDeadZone(number: Double) {
            this.deadZone = number
        }

        /**
         * How far you need to press this trigger to activate a button press
         *
         * @param number
         */
        fun setTriggerSensitivity(number: Double) {
            this.sensitivity = number
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

        /* Get Methods */

        /**
         * Just like getAngle, but returns a direction instead of an angle
         *
         * @return A DPAD direction
         */
        val direction: DPAD
            get() = DPAD.getEnum(angle())


        init {
            this.up = DPadButton(this, DPAD.UP)
            this.upRight = DPadButton(this, DPAD.UP_RIGHT)
            this.right = DPadButton(this, DPAD.RIGHT)
            this.downRight = DPadButton(this, DPAD.DOWN_RIGHT)
            this.down = DPadButton(this, DPAD.DOWN)
            this.downLeft = DPadButton(this, DPAD.DOWN_LEFT)
            this.left = DPadButton(this, DPAD.LEFT)
            this.upLeft = DPadButton(this, DPAD.UP_LEFT)
        }/* Initialize */

        public fun angle(): Int {
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
         * @param dPadDirection
         */
        internal constructor(private val parent: DirectionalPad, /* Instance Values */
                             private val direction: DPAD)/* Initialize */ : org.usfirst.frc.team4322.command.Trigger()
        {

            /* Extended Methods */
            override fun get(): Boolean {
                return parent.angle() == direction.value
            }
        }
    }

    companion object {

        /* Default Values */
        private val DEFAULT_THUMBSTICK_DEADZONE = 0.1  // Jiggle room for the thumbsticks
        private val DEFAULT_TRIGGER_DEADZONE = 0.01 // Jiggle room for the triggers
        private val DEFAULT_TRIGGER_SENSITIVITY = 0.6  // If the trigger is beyond this limit, say it has been pressed

        /* Button Mappings */
        private val A_BUTTON_ID = 1
        private val B_BUTTON_ID = 2
        private val X_BUTTON_ID = 3
        private val Y_BUTTON_ID = 4
        private val LB_BUTTON_ID = 5
        private val RB_BUTTON_ID = 6
        private val BACK_BUTTON_ID = 7
        private val START_BUTTON_ID = 8
        private val LEFT_THUMBSTICK_BUTTON_ID = 9
        private val RIGHT_THUMBSTICK_BUTTON_ID = 10

        /* Axis Mappings */
        private val LEFT_THUMBSTICK_X_AXIS_ID = 0
        private val LEFT_THUMBSTICK_Y_AXIS_ID = 1
        private val LEFT_TRIGGER_AXIS_ID = 2
        private val RIGHT_TRIGGER_AXIS_ID = 3
        private val RIGHT_THUMBSTICK_X_AXIS_ID = 4
        private val RIGHT_THUMBSTICK_Y_AXIS_ID = 5

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
            val negative: Double
            var deadZoneSizeClamp = deadZoneSize
            var adjusted: Double

            if (deadZoneSizeClamp < 0 || deadZoneSizeClamp >= 1) {
                deadZoneSizeClamp = 0.0  // Prevent any weird errors
            }

            negative = (if (input < 0) -1 else 1).toDouble()

            adjusted = Math.abs(input) - deadZoneSizeClamp  // Subtract the deadzone from the magnitude
            adjusted = if (adjusted < 0) 0.0 else adjusted          // if the new input is negative, make it zero
            adjusted /= (1 - deadZoneSizeClamp)   // Adjust the adjustment so it can max at 1

            return negative * adjusted
        }
    }
}