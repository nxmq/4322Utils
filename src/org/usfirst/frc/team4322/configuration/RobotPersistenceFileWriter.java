package org.usfirst.frc.team4322.configuration;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Created by teresamachado on 12/27/16.
 */
public class RobotPersistenceFileWriter
{
	private static RobotPersistenceFileWriter _instance = null;
	public static final String CONFIG_FILE = "/home/lvuser/persistence.ini";
	private HashMap<String,String> values;

	public static RobotPersistenceFileWriter getInstance()
	{
		// Look to see if the instance has already been created
		if(_instance == null)
		{
			// If the instance does not yet exist, create it.
			_instance = new RobotPersistenceFileWriter();
		}
		// Return the singleton instance to the caller.
		return _instance;
	}
	public void set(String key, String value)
	{
		values.put(key,value);
	}

	public void write()
	{
		try(PrintWriter out = new PrintWriter(CONFIG_FILE))
		{
			values.forEach((k,v) -> out.printf("%s=%s\n",k,v));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
