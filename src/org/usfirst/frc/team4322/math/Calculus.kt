package org.usfirst.frc.team4322.math

/*
 * Class for integrating and deriving arrays as if they were functions
 * @author garrettluu
 */
class Calculus {
    companion object {
        @JvmStatic
        fun integrateArray(input: DoubleArray, dt: Double): DoubleArray {
            //calculates position setpoint at each node
            val tmp = DoubleArray(input.size + 1)
            val result = DoubleArray(input.size)
            tmp[0] = 0.0
            for (i in 1 until input.size + 1) {
                if (i == input.size) {
                    tmp[i] = tmp[i - 1]
                } else {
                    tmp[i] = input[i] * dt + tmp[i - 1] //numerical integration
                }
                result[i - 1] = tmp[i]
                print("Integration value: ")
                println(tmp[i])
            }
            return result
        }

        @JvmStatic
        fun deriveArray(input: DoubleArray, dt: Double): DoubleArray {
            val result = DoubleArray(input.size)
            for (i in 1 until input.size) {
                result[i] = (input[i] - input[i - 1]) / dt
            }
            result[0] = result[1]
            return result
        }
    }
}