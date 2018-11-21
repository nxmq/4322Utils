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
        val topLeft = ButtonTrigger(parent,if(hand == Hand.Left) 5 else 11)
        val topCenter = ButtonTrigger(parent,if(hand == Hand.Left) 6 else 12)
        val topRight = ButtonTrigger(parent,if(hand == Hand.Left) 7 else 13)
        val bottomLeft = ButtonTrigger(parent,if(hand == Hand.Left) 10 else 16)
        val bottomCenter = ButtonTrigger(parent,if(hand == Hand.Left) 9 else 15)
        val bottomRight = ButtonTrigger(parent,if(hand == Hand.Left) 8 else 14)
    }

    class RightButtonCluster(parent : Joystick,hand : Hand) {
        val topLeft = ButtonTrigger(parent,if(hand == Hand.Left) 13 else 7)
        val topCenter = ButtonTrigger(parent,if(hand == Hand.Left) 12 else 6)
        val topRight = ButtonTrigger(parent,if(hand == Hand.Left) 11 else 5)
        val bottomLeft = ButtonTrigger(parent,if(hand == Hand.Left) 16 else 8)
        val bottomCenter = ButtonTrigger(parent,if(hand == Hand.Left) 15 else 9)
        val bottomRight = ButtonTrigger(parent,if(hand == Hand.Left) 14 else 10)
    }

    class KnobCluster(parent: Joystick) {
        val left = ButtonTrigger(parent, 3)
        val right = ButtonTrigger(parent, 4)
        val bottom = ButtonTrigger(parent, 2)
    }

    val leftCluster = LeftButtonCluster(joystick, hand)
    val rightCluster = RightButtonCluster(joystick, hand)
    val knobCluster = KnobCluster(joystick)
    val trigger = ButtonTrigger(joystick, 1)


    val yAxis = JoystickAxis(joystick, 1)
    val xAxis = JoystickAxis(joystick, 0)
    val scrollWheel = JoystickAxis(joystick, 2)
}