package org.usfirst.frc.team4322.logging;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team4322.dashboard.DashboardInputField;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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

public class RobotLogger
{


	public enum LogLevel
	{
		DEBUG,
		INFO,
		LOG,
		WARN,
		ERR;
	}

	// Get Date Format
	private static final SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
	// Instance for Singleton class
	private static RobotLogger _instance = null;
	// Instance for Driver Station
	private final DriverStation driverStation = DriverStation.getInstance();
	// Instances for the log files
	private final String logFolder = System.getProperty("user.home") + "/logs";
	private final String LOG_FILE = "RobotInitLog",
			Robot_Disabled_Log = "RobotDisabledLog",
			Robot_Auto_Log = "RobotAutoLog",
			Robot_Teleop_Log = "RobotTeleopLog",
			Robot_Test_Log = "RobotTestLog";
	// Instances for ZIP File
	private final String LOGS_ZIP_FILE = System.getProperty("user.home") + "logs/RobotLogs.zip";
	// Constants for file
	private final long MAX_FILE_LENGTH = 10485760;
	// Log writer
	private PrintWriter pw = null;
	// Log File status
	private boolean closed = true;
	// Logging Level
	@DashboardInputField(field = "Logging Level")
	public static LogLevel currentLogLevel = LogLevel.DEBUG;

    private RobotLogger()
    {
        SendableChooser<LogLevel> enumChooser = new SendableChooser();
        for(int i = 0; i < LogLevel.class.getEnumConstants().length; i++)
        {
            if(i == 3)
            {
                enumChooser.addDefault(LogLevel.values()[i].toString(),LogLevel.values()[i]);
            }
            else
            {
                enumChooser.addObject(LogLevel.values()[i].toString(),LogLevel.values()[i]);
            }
        }
        SmartDashboard.putData("Logging Level", enumChooser);
    }

    // This is the static getInstance() method that provides easy access to the RobotLogger singleton class.
	public static RobotLogger getInstance()
	{
		// Look to see if the instance has already been created...
		if (_instance == null)
		{
			// If the instance does not yet exist, create it.
			_instance = new RobotLogger();
		}
		// Return the singleton instance to the caller.
		return _instance;
	}

	// Gets the date in yyyy-MM-dd format
	public static String CurrentReadable_DateTime()
	{
		return sdf_.format(Calendar.getInstance().getTime());
	}

	// Creates a string out of a throwable
	public static String getString(final Throwable e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	private String getProperLogFile()
	{
		String file = logFolder + LOG_FILE;
		if (driverStation.isFMSAttached())
		{
			return logFolder + String.format("RobotCompetitionMatch-%s-%s-%d-%d.txt", driverStation.getEventName(), driverStation.getMatchType().name(), driverStation.getMatchNumber(), driverStation.getReplayNumber());
		}
		if (driverStation.isDisabled())
			file = logFolder + Robot_Disabled_Log;
		if (driverStation.isAutonomous())
			file = logFolder + Robot_Auto_Log;
		if (driverStation.isOperatorControl())
			file = logFolder + Robot_Teleop_Log;
		if (driverStation.isTest())
			file = logFolder + Robot_Test_Log;
		file += (" [" + CurrentReadable_DateTime() + "].txt");
		return file;
	}

	public void update()
	{
		if (closed)
		{
			try
			{
				// Get the correct file
				File log = new File(getProperLogFile());

				// Make sure the log directory exists.
				if (!log.getParentFile().exists())
				{
					log.getParentFile().mkdirs();
					log.createNewFile();
				}

				// If the file exists & the length is too long, send it to ZIP
				if (log.exists())
				{
					if (log.length() > MAX_FILE_LENGTH)
					{
						File archivedLog = new File(log.getAbsolutePath().replace(".txt", "") + " [" +
								CurrentReadable_DateTime() + "]" + ".txt");
						log.renameTo(archivedLog);
						log = new File(getProperLogFile());
					}
				}
				// If log file does not exist, create it
				else
				{
					if (!log.createNewFile())
						System.out.println("****************ERROR IN CREATING FILE: " + log + " ***********");
				}
				pw = new PrintWriter(new BufferedWriter(new FileWriter(log)));
				closed = false;
			}
			catch (IOException ex)
			{
				writeErrorToFile("RobotLogger.update()", ex);
			}
			info("Successfully updated logging file system.");
		}
	}

	public void setLogLevel(LogLevel l)
	{
		currentLogLevel = l;
	}
	/*
	 * If there already is a file, write the data to it.
	 * If there is not, create the file.
	 */
	private void writeToFile(final String msg, Object... args)
	{
		pw.format(msg, args);
	}

	// Writes the throwable error to the .txt log file
	private void writeErrorToFile(final String method, final Throwable t)
	{
		if (closed)
		{
			update();
		}
		String msg = "\nException in " + method + ": " + getString(t);
		if (!DriverStation.getInstance().isFMSAttached())
		{
			System.err.println(msg);
		}
		writeToFile(msg);
	}

	//Writes throwable error to DS.
	private void writeErrorToDS(final String message)
	{
		DriverStation.reportError(message, false);
	}

	public synchronized void debug(String thisMessage, Object... args)
	{
		writeLogEntry(thisMessage,LogLevel.DEBUG,args);
	}
	public synchronized void info(String thisMessage, Object... args)
	{
		writeLogEntry(thisMessage, LogLevel.INFO, args);
	}
	public synchronized void log(String thisMessage, Object... args)
	{
		writeLogEntry(thisMessage,LogLevel.LOG,args);
	}
	public synchronized void warn(String thisMessage, Object... args)
	{
		writeLogEntry(thisMessage,LogLevel.WARN,args);
	}
	public synchronized void err(String thisMessage, Object... args)
	{
		writeLogEntry(thisMessage,LogLevel.ERR,args);
		writeErrorToDS(String.format(thisMessage,args));
	}
	public synchronized void exc(String thisMessage, Throwable exc)
	{
		writeLogEntry(thisMessage+"\n%s",LogLevel.ERR,exc.getMessage());
	}

	private void writeLogEntry(String message, LogLevel level, Object... args)
	{
		//check current logging level
		if(level.ordinal() < currentLogLevel.ordinal())
			return;
		// Output logging messages to the console with a standard format
		String datetimeFormat = "\n [" + CurrentReadable_DateTime() + "] - Robot4322: - "+ level.name() +" - ";
		if(!DriverStation.getInstance().isFMSAttached())
		{
			System.out.format(datetimeFormat + message + "\n", args);
		}
		// Output logging messages to a .txt log file
		writeToFile(datetimeFormat + message + "\n", args);
	}

	private void writeException(String message, LogLevel level, Exception exc)
	{
		//check current logging level
		if(level.ordinal() < currentLogLevel.ordinal())
			return;
		// Output logging messages to the console with a standard format
		String datetimeFormat = "\n [" + CurrentReadable_DateTime() + "] - Robot: - "+ level.name() +" - ";
		if(!DriverStation.getInstance().isFMSAttached())
		{
			System.out.format(datetimeFormat + message + "\n");
			exc.printStackTrace();
		}
		// Output logging messages to a .txt log file
		writeToFile(datetimeFormat + message + "\n");
		exc.printStackTrace(pw);
	}

	public void close()
	{
		if (closed)
			return;
		if (pw != null)
		{
			pw.close();
			closed = true;
		}
	}
}