package org.usfirst.frc.team4322.input

import edu.wpi.first.wpilibj.Joystick
import org.usfirst.frc.team4322.command.ButtonTrigger

class InputThrustmaster(port : Int, val hand : Hand ) : Joystick(port) {

     enum class Hand {
        Left,
        Right
     }

    class LeftButtonCluster(val parent : Joystick,val hand : Hand) {
        val topLeft = ButtonTrigger(parent,if(hand == Hand.Left) 5 else 11)
        val topCenter = ButtonTrigger(parent,if(hand == Hand.Left) 6 else 12)
        val topRight = ButtonTrigger(parent,if(hand == Hand.Left) 7 else 13)
        val bottomLeft = ButtonTrigger(parent,if(hand == Hand.Left) 10 else 16)
        val bottomCenter = ButtonTrigger(parent,if(hand == Hand.Left) 9 else 15)
        val bottomRight = ButtonTrigger(parent,if(hand == Hand.Left) 8 else 14)
    }

    class RightButtonCluster(val parent : Joystick,val hand : Hand) {
        val topLeft = ButtonTrigger(parent,if(hand == Hand.Left) 13 else 7)
        val topCenter = ButtonTrigger(parent,if(hand == Hand.Left) 12 else 6)
        val topRight = ButtonTrigger(parent,if(hand == Hand.Left) 11 else 5)
        val bottomLeft = ButtonTrigger(parent,if(hand == Hand.Left) 16 else 8)
        val bottomCenter = ButtonTrigger(parent,if(hand == Hand.Left) 15 else 9)
        val bottomRight = ButtonTrigger(parent,if(hand == Hand.Left) 14 else 10)
    }

    val trigger = ButtonTrigger(this,1)
    val topLeft = ButtonTrigger(this,3)
    val topRight = ButtonTrigger(this,4)
    val topBottom = ButtonTrigger(this,2)
    val leftCluster = LeftButtonCluster(this,hand)
    val rightCluster = RightButtonCluster(this,hand)
}