package org.usfirst.frc.team4322.logging

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.usfirst.frc.team4322.dashboard.DashboardInputField

import java.io.*
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * @NOTE: The following methods are used in this class:
 * getInstance() - gets the instance for the class (Singleton method)
 * update() - opens and updates the file system [FileWriter and BufferedWriter] ONLY when the system is initially
 * closed, then sets closed to false
 * writeToFile(String) - writes message to log file ONLY when the system is open
 * writeErrorToFile(String, Throwable) - writes exception to log file
 * sendToConsole(String) - writes message to system output (Riolog) AND to the log file
 * addFileToZip(File, String) - sends a file to a zip folder
 * CurrentReadable_DateTime() - gets the current date and time
 * getString(Throwable) - gets a string out of a throwable
 * close() - closes the FileWriter and BufferedWriter
 */

/**
 * @author FRC4322
 */

object RobotLogger {
    // Instance for Driver Station
    private val driverStation = DriverStation.getInstance()
    // Instances for the log files
    private val logFolder = System.getProperty("user.home") + "/logs"
    private const val LOG_FILE = "RobotInitLog"
    private const val Robot_Disabled_Log = "RobotDisabledLog"
    private const val Robot_Auto_Log = "RobotAutoLog"
    private const val Robot_Teleop_Log = "RobotTeleopLog"
    private const val Robot_Test_Log = "RobotTestLog"
    // Instances for ZIP File
    private val LOGS_ZIP_FILE = System.getProperty("user.home") + "logs/RobotLogs.zip"
    // Constants for file
    private const val MAX_FILE_LENGTH: Long = 10485760
    // Log writer
    private var pw: PrintWriter? = null
    // Log File status
    private var closed = true
    // Get Date Format
    private val sdf_ = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
    // Instance for Singleton class
    private var _instance: RobotLogger? = null
    // Logging Level
    @DashboardInputField(field = "Logging Level")
    var currentLogLevel = LogLevel.DEBUG


    private val properLogFile: String
        get() {
            var file = logFolder + LOG_FILE
            if (driverStation.isFMSAttached) {
                return logFolder + String.format("RobotCompetitionMatch-%s-%s-%d-%d.txt", driverStation.eventName, driverStation.matchType.name, driverStation.matchNumber, driverStation.replayNumber)
            }
            if (driverStation.isDisabled)
                file = logFolder + Robot_Disabled_Log
            if (driverStation.isAutonomous)
                file = logFolder + Robot_Auto_Log
            if (driverStation.isOperatorControl)
                file = logFolder + Robot_Teleop_Log
            if (driverStation.isTest)
                file = logFolder + Robot_Test_Log
            file += " [" + CurrentReadable_DateTime() + "].txt"
            return file
        }


    enum class LogLevel {
        DEBUG,
        INFO,
        LOG,
        WARN,
        ERR
    }

    init {
        val enumChooser = SendableChooser<LogLevel>()
        for (i in 0 until LogLevel::class.java.enumConstants.size) {
            if (i == 3) {
                enumChooser.addDefault(LogLevel.values()[i].toString(), LogLevel.values()[i])
            } else {
                enumChooser.addObject(LogLevel.values()[i].toString(), LogLevel.values()[i])
            }
        }
        SmartDashboard.putData("Logging Level", enumChooser)
    }

    fun update() {
        if (closed) {
            try {
                // Get the correct file
                var log = File(properLogFile)

                // Make sure the log directory exists.
                if (!log.parentFile.exists()) {
                    log.parentFile.mkdirs()
                    log.createNewFile()
                }

                // If the file exists & the length is too long, send it to ZIP
                if (log.exists()) {
                    if (log.length() > MAX_FILE_LENGTH) {
                        val archivedLog = File(log.absolutePath.replace(".txt", "") + " [" +
                                CurrentReadable_DateTime() + "]" + ".txt")
                        log.renameTo(archivedLog)
                        log = File(properLogFile)
                    }
                } else {
                    if (!log.createNewFile())
                        println("****************ERROR IN CREATING FILE: $log ***********")
                }// If log file does not exist, create it
                pw = PrintWriter(BufferedWriter(FileWriter(log)))
                closed = false
            } catch (ex: IOException) {
                writeErrorToFile("RobotLogger.update()", ex)
            }

            info("Successfully updated logging file system.")
        }
    }

    fun setLogLevel(l: LogLevel) {
        currentLogLevel = l
    }

    /*
	 * If there already is a file, write the data to it.
	 * If there is not, create the file.
	 */
    private fun writeToFile(msg: String, vararg args: Any) {
        pw!!.format(msg, *args)
    }

    // Writes the throwable error to the .txt log file
    private fun writeErrorToFile(method: String, t: Throwable) {
        if (closed) {
            update()
        }
        val msg = "\nException in " + method + ": " + getString(t)
        if (!DriverStation.getInstance().isFMSAttached) {
            System.err.println(msg)
        }
        writeToFile(msg)
    }

    //Writes throwable error to DS.
    private fun writeErrorToDS(message: String) {
        DriverStation.reportError(message, false)
    }

    @Synchronized
    fun debug(thisMessage: String, vararg args: Any) {
        writeLogEntry(thisMessage, LogLevel.DEBUG, *args)
    }

    @Synchronized
    fun info(thisMessage: String, vararg args: Any) {
        writeLogEntry(thisMessage, LogLevel.INFO, *args)
    }

    @Synchronized
    fun log(thisMessage: String, vararg args: Any) {
        writeLogEntry(thisMessage, LogLevel.LOG, *args)
    }

    @Synchronized
    fun warn(thisMessage: String, vararg args: Any) {
        writeLogEntry(thisMessage, LogLevel.WARN, *args)
    }

    @Synchronized
    fun err(thisMessage: String, vararg args: Any) {
        writeLogEntry(thisMessage, LogLevel.ERR, *args)
        writeErrorToDS(String.format(thisMessage, *args))
    }

    @Synchronized
    fun exc(thisMessage: String, exc: Throwable) {
        writeLogEntry("$thisMessage\n%s", LogLevel.ERR, exc.message!!)
    }

    private fun writeLogEntry(message: String, level: LogLevel, vararg args: Any) {
        //check current logging level
        if (level.ordinal < currentLogLevel.ordinal)
            return
        // Output logging messages to the console with a standard format
        val datetimeFormat = "\n [" + CurrentReadable_DateTime() + "] - Robot4322: - " + level.name + " - "
        if (!DriverStation.getInstance().isFMSAttached) {
            System.out.format(datetimeFormat + message + "\n", *args)
        }
        // Output logging messages to a .txt log file
        writeToFile(datetimeFormat + message + "\n", *args)
    }

    private fun writeException(message: String, level: LogLevel, exc: Exception) {
        //check current logging level
        if (level.ordinal < currentLogLevel.ordinal)
            return
        // Output logging messages to the console with a standard format
        val datetimeFormat = "\n [" + CurrentReadable_DateTime() + "] - Robot: - " + level.name + " - "
        if (!DriverStation.getInstance().isFMSAttached) {
            System.out.format(datetimeFormat + message + "\n")
            exc.printStackTrace()
        }
        // Output logging messages to a .txt log file
        writeToFile(datetimeFormat + message + "\n")
        exc.printStackTrace(pw)
    }

    fun close() {
        if (closed)
            return
        if (pw != null) {
            pw!!.close()
            closed = true
        }
    }
        // Gets the date in yyyy-MM-dd format
        private fun CurrentReadable_DateTime(): String {
            return sdf_.format(Calendar.getInstance().time)
        }

        // Creates a string out of a throwable
        private fun getString(e: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            return sw.toString()
        }
}