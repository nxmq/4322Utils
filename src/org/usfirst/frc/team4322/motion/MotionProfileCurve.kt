package org.usfirst.frc.team4322.motion

import org.usfirst.frc.team4322.math.Calculus.Companion.integrateArray
import org.usfirst.frc.team4322.math.Trig
import java.io.*


/*
 * Class for generating motion profiles from quintic or cubic hermite splines
 * Needs some cleaning up but it works
 * @author garrettluu
 */
class MotionProfileCurve(private var theta1: Double, private var theta2: Double, private var distance: Double, velocity: Double, private var acceleration: Double, private var wheelBaseWidth: Double, var name: String) {
    private var targetVelocity = velocity
    private var maxTime: Double = 15.0
    private var numOfPoints: Int = 0

    private val duration = .1

    private var quintic = 0.0
    private var quartic = 0.0
    private var cubic = 0.0
    private var quadratic = 0.0
    private var linear = 0.0

    lateinit var position: Array<DoubleArray>
    lateinit var positionLeft: Array<DoubleArray>
    lateinit var positionRight: Array<DoubleArray>

    lateinit var velocity: DoubleArray
    lateinit var velocityLeft: DoubleArray
    lateinit var velocityRight: DoubleArray

    lateinit var generatedProfileLeft: Array<DoubleArray>
    lateinit var generatedProfileRight: Array<DoubleArray>

    lateinit var csvFileLeft: File
    lateinit var csvFileRight: File
    lateinit var writer: FileWriter

    enum class interpMode {
        CUBIC_SPLINE,
        QUINTIC_SPLINE
    }

    var interp = interpMode.QUINTIC_SPLINE

    constructor(theta1: Double,
                theta2: Double,
                distance: Double,
                velocity: Double,
                acceleration: Double,
                wheelBaseWidth: Double,
                name: String, interp: interpMode) : this(theta1, theta2, distance, velocity, acceleration, wheelBaseWidth, name) {
        this.interp = interp
    }

    fun trapezoidalProfile(distance: Double, velocity: Double, acceleration: Double): DoubleArray {
        val result = DoubleArray(numOfPoints)
        var time = 0.000001

        for (i in 0 until numOfPoints) {
            if (time < 0) {
                result[i] = 0.0
            } else if (time > 0 && time < velocity / acceleration) {
                result[i] = acceleration * time
            } else if (time >= velocity / acceleration && time < distance / velocity) {
                result[i] = velocity
            } else if (time >= distance / velocity && time <= maxTime) {
                result[i] = -acceleration * (time - maxTime)
            } else if (time >= maxTime) {
                result[i] = 0.0
            }
            print("Profile Value: ")
            println(result[i])
            time += duration
        }
        return result
    }

    fun fillHermite() {
        //calculates coefficients for quintic polynomial
        if (interp == interpMode.QUINTIC_SPLINE) {
            quintic = -3 * (Trig.tanDeg(theta2) + Trig.tanDeg(theta1)) / Math.pow(distance, 4.0)
            quartic = (Trig.tanDeg(theta1) - 2.333333 * Math.pow(distance, 4.0) * quintic) / Math.pow(distance, 3.0)
            cubic = (10 * Math.pow(distance, 2.0) * quintic + 6 * distance * quartic) / -3
            quadratic = 0.0
            linear = Trig.tanDeg(theta1)
        } else if (interp == interpMode.CUBIC_SPLINE) {
            quintic = 0.0
            quartic = 0.0
            linear = Trig.tanDeg(theta1)
            quadratic = (Trig.tanDeg(theta2) + (2 * linear)) / (-distance)
            cubic = (Trig.tanDeg(theta2) - (quadratic * distance)) / (2 * Math.pow(distance, 2.0))
        }

        System.out.println("Quintic Term: $quintic")
        System.out.println("Quartic Term: $quartic")
        System.out.println("Cubic Term: $cubic")
        System.out.println("Linear Term: $linear")

        position = Array(numOfPoints) { DoubleArray(2) }
        positionLeft = Array(numOfPoints) { DoubleArray(2) }
        positionRight = Array(numOfPoints) { DoubleArray(2) }
        velocity = DoubleArray(numOfPoints)
        velocityLeft = DoubleArray(numOfPoints)
        velocityRight = DoubleArray(numOfPoints)
    }

    fun fillPosition() {
        var timeConstant = 0.0000001
        println("--- Generating Position Values! ---")
        val basePositionProfile = integrateArray(trapezoidalProfile(distance, targetVelocity, acceleration), duration)
        for (i in 0 until numOfPoints) {
            val grad: Double

            position[i][0] = basePositionProfile[i]//x
            position[i][1] = quintic * Math.pow(position[i][0], 5.0) +
                    quartic * Math.pow(position[i][0], 4.0) +
                    cubic * Math.pow(position[i][0], 3.0) +
                    linear * position[i][0]//y

            println("$timeConstant (" + position[i][0] + ", " + position[i][1] + ")")

            if (i == numOfPoints - 1) {
                grad = Math.atan2(position[i][1] - position[i - 1][1], position[i][0] - position[i - 1][0])
            } else {
                grad = Math.atan2(position[i + 1][1] - position[i][1], position[i + 1][0] - position[i][0])
            }
            println("Grad: $grad")

            positionLeft[i][0] = wheelBaseWidth / 2 * Math.cos(grad + Math.PI / 2) + position[i][0]
            positionLeft[i][1] = wheelBaseWidth / 2 * Math.sin(grad + Math.PI / 2) + position[i][1]
            System.out.println("Left x: " + positionLeft[i][0])
            System.out.println("Left y: " + positionLeft[i][1])

            positionRight[i][0] = wheelBaseWidth / 2 * Math.cos(grad - Math.PI / 2) + position[i][0]
            positionRight[i][1] = wheelBaseWidth / 2 * Math.sin(grad - Math.PI / 2) + position[i][1]
            System.out.println("Right x: " + positionRight[i][0])
            System.out.println("Right y: " + positionRight[i][1])

            timeConstant += duration
        }
        println("--- End of Position Values! ---")
    }

    fun fillVelocity(position: Array<DoubleArray>): DoubleArray {
        val velocity = DoubleArray(numOfPoints)
        //velocity = derivative of position
        var dxdt: Double
        var dydt: Double
        println("--- Generating Velocity Values ---")
        velocity[0] = 0.0
        for (i in 1 until numOfPoints - 1) {

            dxdt = (position[i][0] - position[i - 1][0]) / duration //run an approximation of dxdt and dydt
            dydt = (position[i][1] - position[i - 1][1]) / duration

            velocity[i] = Math.sqrt(Math.pow(dxdt, 2.0) + Math.pow(dydt, 2.0)) * 60.0 * 12.0 / (wheelBaseWidth * Math.PI) //calculate magnitude of velocity, convert to rpm
            println(velocity[i])
        }
        velocity[numOfPoints - 2] = 0.0
        println("--- End of Velocity Values ---")
        return velocity
    }

    fun arcLength(velocity: DoubleArray) //calculate number of rotations for encoder
            : DoubleArray {
        //calculates position setpoint at each node
        val result = DoubleArray(numOfPoints)
        result[0] = 0.0
        for (i in 1 until numOfPoints) {
            result[i] = velocity[i] * duration / 60 + result[i - 1] //numerical integration of velocity
        }
        return result
    }

    fun compileProfile(distanceInput: DoubleArray, velocityInput: DoubleArray, filename: String): Array<DoubleArray> {

        println("--- START OF PROFILE ---")
        val generatedProfile = Array(numOfPoints) { DoubleArray(3) }
        try {
            writer = FileWriter("/home/lvuser/$filename.csv")
        } catch (e: IOException) {

        }

        for (i in 0 until numOfPoints) {
            generatedProfile[i][0] = distanceInput[i]
            generatedProfile[i][1] = velocityInput[i]
            generatedProfile[i][2] = duration * 1000
            println("{" + generatedProfile[i][0] + ", " + generatedProfile[i][1] + ", " + generatedProfile[i][2] + "}")

            try {
                writer.append("" + generatedProfile[i][0])
                writer.append(',')
                writer.append("" + generatedProfile[i][1])
                writer.append(',')
                writer.append("" + generatedProfile[i][2])
                writer.append('\n')
            } catch (e: IOException) {

            }

        }
        try {
            writer.flush()
            writer.close()
        } catch (e: IOException) {

        }

        println("--- END OF PROFILE ---")
        return generatedProfile
    }

    fun generateProfileLeft(): Array<DoubleArray> {
        println("--- LEFT PROFILE ---")
        val outputLeft: Array<DoubleArray>
        val rampedVelocityLeft: DoubleArray
        val rotLeft: DoubleArray
        fillHermite()
        fillPosition()
        fillPosition() //stupid
        velocityLeft = fillVelocity(positionLeft)
        rampedVelocityLeft = velocityLeft
        //		rampedVelocityLeft = optimizeVelocity(velocityLeft, applyRamping(velocityLeft));
        //	     rampedVelocityLeft = applyRamping(velocityLeft);
        rotLeft = arcLength(rampedVelocityLeft)
        outputLeft = compileProfile(rotLeft, rampedVelocityLeft, name + "_Left")
        println("--- LEFT PROFILE END ---")
        return outputLeft
    }

    fun generateProfileRight(): Array<DoubleArray> {
        println("--- RIGHT PROFILE ---")
        val outputRight: Array<DoubleArray>
        val rampedVelocityRight: DoubleArray
        val rotRight: DoubleArray
        fillHermite()
        fillPosition()
        fillPosition()
        velocityRight = fillVelocity(positionRight)
        rampedVelocityRight = velocityRight
        //		rampedVelocityRight = optimizeVelocity(velocityRight, applyRamping(velocityRight));
        //	     rampedVelocityRight = applyRamping(velocityRight);
        rotRight = arcLength(rampedVelocityRight)
        outputRight = compileProfile(rotRight, rampedVelocityRight, name + "_Right")
        println("--- RIGHT PROFILE END ---")
        return outputRight
    }

    fun readProfileFromCSV() {
        val pathLeft = "/home/lvuser/" + name + "_Left.csv"
        val pathRight = "/home/lvuser/" + name + "_Right.csv"
        csvFileLeft = File(pathLeft)
        csvFileRight = File(pathRight)
        var line = ""
        if (csvFileLeft.exists() && csvFileRight.exists()) {
            //read the file
            println("Files found, read file.")
            generatedProfileLeft = Array(numOfPoints) { DoubleArray(3) }
            generatedProfileRight = Array(numOfPoints) { DoubleArray(3) }
            try {
                BufferedReader(FileReader(csvFileLeft)).use { br ->
                    var i = 0
                    while (br.readLine() != null) {
                        line = br.readLine()
                        val values = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        generatedProfileLeft[i][0] = java.lang.Double.parseDouble(values[0])
                        generatedProfileLeft[i][1] = java.lang.Double.parseDouble(values[1])
                        generatedProfileLeft[i][2] = java.lang.Double.parseDouble(values[2])
                        i++
                    }
                }
            } catch (e: IOException) {

            }

            try {
                BufferedReader(FileReader(csvFileRight)).use { br ->
                    var i = 0
                    while (br.readLine() != null) {
                        line = br.readLine()
                        val values = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        generatedProfileRight[i][0] = java.lang.Double.parseDouble(values[0])
                        generatedProfileRight[i][1] = java.lang.Double.parseDouble(values[1])
                        generatedProfileRight[i][2] = java.lang.Double.parseDouble(values[2])
                        i++
                    }
                }
            } catch (e: IOException) {

            }

            csvFileLeft.delete()
            csvFileRight.delete()
        } else
        //if a file doesn't exist
        {
            try {
                csvFileLeft.delete() //delete the other file
                csvFileRight.delete()
                csvFileLeft.createNewFile() //create new files
                csvFileRight.createNewFile()
            } catch (e: IOException) {

            }

            generatedProfileLeft = generateProfileLeft()
            generatedProfileRight = generateProfileRight()
        }
    }

    init {
        maxTime = (distance / velocity) + (velocity / acceleration)
        numOfPoints = (maxTime / duration).toInt()
    }
}
