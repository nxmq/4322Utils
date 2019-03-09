package org.usfirst.frc.team4322.input

import edu.wpi.first.wpilibj.Joystick
import org.usfirst.frc.team4322.commandv2.ButtonTrigger

class InputThrustmaster(port: Int, hand: Hand) {

    private val joystick: Joystick = Joystick(port)

     enum class Hand {
        Left,
        Right
     }

    class LeftButtonCluster(parent : Joystick,hand : Hand) {
        val topLeft: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 5 else 11) }
        val topCenter: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 6 else 12) }
        val topRight: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 7 else 13) }
        val bottomLeft: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 10 else 16) }
        val bottomCenter: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 9 else 15) }
        val bottomRight: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 8 else 14) }
    }

    class RightButtonCluster(parent : Joystick,hand : Hand) {
        val topLeft: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 13 else 7) }
        val topCenter: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 12 else 6) }
        val topRight: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 11 else 5) }
        val bottomLeft: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 16 else 8) }
        val bottomCenter: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 15 else 9) }
        val bottomRight: ButtonTrigger by lazy { ButtonTrigger(parent, if (hand == Hand.Right) 14 else 10) }
    }

    class KnobCluster(parent: Joystick) {
        val left: ButtonTrigger by lazy { ButtonTrigger(parent, 3) }
        val right: ButtonTrigger by lazy { ButtonTrigger(parent, 4) }
        val bottom: ButtonTrigger by lazy { ButtonTrigger(parent, 2) }
    }

    val leftCluster: LeftButtonCluster by lazy { LeftButtonCluster(joystick, hand) }
    val rightCluster: RightButtonCluster by lazy { RightButtonCluster(joystick, hand) }
    val knobCluster: KnobCluster by lazy { KnobCluster(joystick) }
    val trigger: ButtonTrigger by lazy { ButtonTrigger(joystick, 1) }


    val yAxis = JoystickAxis(joystick, 1)
    val xAxis = JoystickAxis(joystick, 0)
    val scrollWheel = JoystickAxis(joystick, 2)
}