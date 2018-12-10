package org.usfirst.frc.team4322.math

interface Interpolable<T> {
    fun lerp(other: T, diff: Double): T
}