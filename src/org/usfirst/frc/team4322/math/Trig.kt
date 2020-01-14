package org.usfirst.frc.team4322.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/*
 * Some trig functions to make life a little easier
 * @author garrettluu
 */
class Trig {
    companion object {
        @JvmStatic
        fun sinDeg(degrees: Double): Double {
            return Math.sin(Math.toRadians(degrees))
        }

        @JvmStatic
        fun cosDeg(degrees: Double): Double {
            return Math.cos(Math.toRadians(degrees))
        }

        @JvmStatic
        fun tanDeg(degrees: Double): Double {
            return Math.tan(Math.toRadians(degrees))
        }

        @JvmStatic
        fun angleToSlope(degrees: Double): Double {
            return tanDeg(degrees)
        }

        @JvmStatic
        fun slopeToAngle(dy : Double, dx : Double): Double {
            return Math.toDegrees(atan2(dy, dx))
        }

        @JvmStatic
        fun minSignedAngularDistance(a: Double, b: Double): Double {
            return atan2(sin(a - b), cos(a - b))
        }
    }
}