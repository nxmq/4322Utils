package org.usfirst.frc.team4322.math

object Equality {
    fun epsilonEquals(a: Double, b: Double, epsilon: Double): Boolean {
        return a - epsilon <= b && a + epsilon >= b
    }
}