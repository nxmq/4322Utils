package org.usfirst.frc.team4322.logging

import edu.wpi.first.wpilibj.DriverStation
import org.usfirst.frc.team4322.dashboard.DashboardInputField
import java.io.*
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*

/**
 * @NOTE: The following methods are used in this class:
 * initialize() - opens and updates the file system
 * writeToFile(String) - writes message to log file ONLY when the system is open
 * writeErrorToFile(String, Throwable) - writes exception to log file
 * sendToConsole(String) - writes message to system output (Riolog) AND to the log file
 * getString(Throwable) - gets a string out of a throwable
 * switchToMatchLog - Initiates match logging mode.
 */

/**
 * @author FRC4322
 */

object RobotLogger {
    // Instance for Driver Station
    private val driverStation = DriverStation.getInstance()
    // Instances for the log files
    private val logFolder = System.getProperty("user.home") + "/logs"
    // Log writer
    private var pw: PrintWriter? = null
    // Log Entry Date Format
    private val logTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
    // Log File Date Format
    private val logFileDateFormat = SimpleDateFormat("yyyy-MM-dd-hh:mm:ss")
    // Log File Name
    private var logFile = File("/")
    // Logging Level
    @DashboardInputField(field = "Logging Level")
    var currentLogLevel = LogLevel.DEBUG

    enum class LogLevel {
        DEBUG,
        INFO,
        LOG,
        WARN,
        ERR
    }

    @Synchronized
    fun switchToMatchLogging() {
        var oldFile = logFile
        logFile = File("$logFolder/${String.format("RobotCompetitionLog-%s-%s-%d-%d.log", driverStation.eventName, driverStation.matchType.name, driverStation.matchNumber, driverStation.replayNumber)}")
        if(logFile.toPath().equals(oldFile.toPath()))
            return
        pw?.flush()
        pw?.close()
        Files.move(oldFile.toPath(), logFile.toPath(),StandardCopyOption.REPLACE_EXISTING)
        pw = PrintWriter(BufferedWriter(FileWriter(logFile, true)))
    }

    @Synchronized
    fun initialize() {
            try {
                // Get the  file
                logFile = File("$logFolder/RobotLog_${logFileDateFormat.format(Calendar.getInstance().time)}.log")
                // Make sure the log directory exists.
                if (!logFile.parentFile.exists()) {
                    logFile.parentFile.mkdirs()
                    logFile.createNewFile()
                }
                pw = PrintWriter(BufferedWriter(FileWriter(logFile)))
            } catch (ex: IOException) {
                writeErrorToFile("RobotLogger.initialize()", ex)
            }

            info("Successfully updated logging file system.")
    }

    /*
	 * If there already is a file, write the data to it.
	 * If there is not, create the file.
	 */
    private fun writeToFile(msg: String, vararg args: Any) {
        pw?.format(msg, *args)
    }

    // Writes the throwable error to the .txt log file
    private fun writeErrorToFile(method: String, t: Throwable) {
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
        writeLogEntry("$thisMessage\n%s", LogLevel.ERR, exc.message ?: "")
    }

    private fun writeLogEntry(message: String, level: LogLevel, vararg args: Any) {
        //check current logging level
        if (level.ordinal < currentLogLevel.ordinal)
            return
        // Output logging messages to the console with a standard format
        val datetimeFormat = "\n [" + currentReadableDateTime() + "] - Robot: - " + level.name + " - "
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
        val datetimeFormat = "\n [" + currentReadableDateTime() + "] - Robot: - " + level.name + " - "
        if (!DriverStation.getInstance().isFMSAttached) {
            System.out.format(datetimeFormat + message + "\n")
            exc.printStackTrace()
        }
        // Output logging messages to a .txt log file
        writeToFile(datetimeFormat + message + "\n")
        exc.printStackTrace(pw)
    }

    // Gets the date in yyyy-MM-dd format
    private fun currentReadableDateTime(): String {
        return logTimeFormat.format(Calendar.getInstance().time)
    }

    // Creates a string out of a throwable
    private fun getString(e: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        e.printStackTrace(pw)
        return sw.toString()
    }
}
