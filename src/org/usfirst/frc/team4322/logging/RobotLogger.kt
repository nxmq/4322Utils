package org.usfirst.frc.team4322.logging

import edu.wpi.first.wpilibj.DriverStation
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.LinkedBlockingQueue


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
    var currentLogLevel = LogLevel.DEBUG

    var enableStdoutLogging = false

    data class Message(val level: LogLevel, val text: String)

    val messageQueue: LinkedBlockingQueue<Message> = LinkedBlockingQueue()

    val logThread = Thread {
        while (true) {
            val message = messageQueue.take()
            //check current logging level
            if (message.level.ordinal < currentLogLevel.ordinal)
                continue
            if (!DriverStation.getInstance().isFMSAttached && enableStdoutLogging) {
                System.out.println(message.text)
            }
            // Output logging messages to a .txt log file
            try {
                pw?.println(message.text)
            } catch (ex: IOException) {

            }
        }
    }

    enum class LogLevel {
        DEBUG,
        INFO,
        LOG,
        WARN,
        ERR
    }

    init {
        logThread.start()
    }

    @Synchronized
    fun switchToMatchLogging() {
        val oldFile = logFile
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
                // Get the file
                logFile = File("$logFolder/RobotLog_${logFileDateFormat.format(Calendar.getInstance().time)}.log")
                // Make sure the log directory exists.
                if (!logFile.parentFile.exists()) {
                    logFile.parentFile.mkdirs()
                    logFile.createNewFile()
                }
                pw = PrintWriter(BufferedWriter(FileWriter(logFile)))
            } catch (ex: IOException) {
                exc("RobotLogger.initialize()", ex)
            }
            info("Successfully updated logging file system.")
    }

    @Synchronized
    fun debug(message: String, vararg args: Any) {
        submitLogEntry(message, LogLevel.DEBUG, *args)
    }

    @Synchronized
    fun info(message: String, vararg args: Any) {
        submitLogEntry(message, LogLevel.INFO, *args)
    }

    @Synchronized
    fun log(message: String, vararg args: Any) {
        submitLogEntry(message, LogLevel.LOG, *args)
    }

    @Synchronized
    fun warn(message: String, vararg args: Any) {
        submitLogEntry(message, LogLevel.WARN, *args)
    }

    @Synchronized
    fun err(message: String, vararg args: Any) {
        submitLogEntry(message, LogLevel.ERR, *args)
        DriverStation.reportError(message, false)

    }

    @Synchronized
    fun exc(message: String, exc: java.lang.Exception) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exc.printStackTrace(pw)
        submitLogEntry("$message\n%s", LogLevel.ERR, sw.toString())
        DriverStation.reportError(message, exc.stackTrace)
    }

    private fun submitLogEntry(message: String, level: LogLevel, vararg args: Any) {
        // Output logging messages to the console wih a standard format
        val datetimeFormat = "\n [" + currentReadableDateTime() + "] - Robot: - " + level.name + " - "
        messageQueue.add(Message(level, String.format(datetimeFormat + message + "\n", *args)))
    }

    // Gets the date in yyyy-MM-dd format
    private fun currentReadableDateTime(): String {
        return logTimeFormat.format(Calendar.getInstance().time)
    }
}
