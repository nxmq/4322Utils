package org.usfirst.frc.team4322.math

class Debouncer(var criterion: () -> Boolean, var counts: Int) {
    private var successes: Int = 0
    fun debounce(): Boolean {
        if (criterion()) {
            successes++
            if (successes >= counts)
                return true
            return false
        } else {
            successes = 0
            return false
        }
    }
}