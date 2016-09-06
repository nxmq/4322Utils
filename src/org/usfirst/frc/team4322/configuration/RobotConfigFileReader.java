package org.usfirst.frc.team4322.configuration;

import org.usfirst.frc.team4322.logging.RobotLogger;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.*;

/**
 * Created by nicolasmachado on 2/19/15.
 */

public class RobotConfigFileReader
{
	private static RobotConfigFileReader _instance = null;
	private static Pattern arrayFinder = Pattern.compile("\\{\\s*([^}]+)\\s*\\}");
	public final String CONFIG_FILE = "/home/lvuser/robotConfig.ini";
	private static Map<Class<?>, Method> primitiveMap = new HashMap<Class<?>, Method>();
	static
	{
		try
		{
			primitiveMap.put(boolean.class, Boolean.class.getMethod("parseBoolean", String.class));
			primitiveMap.put(byte.class, Byte.class.getMethod("parseByte", String.class));
			primitiveMap.put(short.class, Short.class.getMethod("parseShort", String.class));
			primitiveMap.put(int.class, Integer.class.getMethod("parseInt", String.class));
			primitiveMap.put(long.class, Long.class.getMethod("parseLong", String.class));
			primitiveMap.put(float.class, Float.class.getMethod("parseFloat", String.class));
			primitiveMap.put(double.class, Double.class.getMethod("parseDouble", String.class));
		}
		catch(NoSuchMethodException | SecurityException ex)
		{
			RobotLogger.getInstance().exc("Exception caught in RobotConfigFileReader", ex);
		}
	}
	public static RobotConfigFileReader getInstance()
	{
		// Look to see if the instance has already been created
		if(_instance == null)
		{
			// If the instance does not yet exist, create it.
			_instance = new RobotConfigFileReader();
		}
		// Return the singleton instance to the caller.
		return _instance;
	}

	/**
	 * Behold the magical update method.
	 * It reads new constants from robotConfig.ini
	 */
	public void runRobotFileReader(Class<?> toFill)
	{
		RobotLogger.getInstance().info("Started Config Update.");
		//Initialize INI value holder.
		Properties p = new Properties();
		try
		{
			//load Values from File.
			p.load(new FileInputStream(CONFIG_FILE));
		}
		catch(IOException ex)
		{
			RobotLogger.getInstance().err("Failed to load robotConfig.ini");
			return;
		}
		try
		{
			//Make an enumerable list of keys in the INI.
			Enumeration<?> enumeration = p.propertyNames();
			//While there are unparsed keys;
			while(enumeration.hasMoreElements())
			{
				//Get the name of the next key.
				String key = (String) enumeration.nextElement();
				//Grab the value for the key.
				String value = p.getProperty(key);
				//create a field to store the RobotMap var.
				Field current = null;
				try
				{
					//Attempt to get the field for the key name.
					current = toFill.getField(key);
				}
				//If the field doesnt exist, log it as a warning.
				catch(NoSuchFieldException ex)
				{
					RobotLogger.getInstance().warn("The field \"%s\" doesnt exist in RobotMap!", key);
					RobotLogger.getInstance().exc("RobotConfigFileReader.runRobotFileReader()", ex);
					continue;
				}
				//if the field is an array....
				if(current.getType().isArray())
				{
					//use the array Finder to split the values.
					Matcher m = arrayFinder.matcher(value);
					//apply the matcher to the string.
					m.find();
					//get our values into a string array.
					String[] arrayValues = m.group().split("[\\s,]+");
					//remove the brackets from the first and last values.
					arrayValues[0] = arrayValues[0].replace("{", "");
					arrayValues[arrayValues.length - 1] = arrayValues[arrayValues.length - 1].replace("}", "");
					//instantiate an array.
					Object elementArray = Array.newInstance(current.getType().getComponentType(), arrayValues.length);
					try
					{
						//If we are dealing with a string array, directly set the values.
						if(current.getType().getComponentType() == String.class)
						{
							//Set each value in the array.
							for(int i = 0; i < arrayValues.length; i++)
							{
								Array.set(elementArray, i, arrayValues[i]);
							}
						}
						//if not, cast appropriately.
						else
						{
							//set each value in the array.
							for(int i = 0; i < arrayValues.length; i++)
							{
								Array.set(elementArray, i, primitiveMap.get(current.getType().getComponentType()).invoke(null, arrayValues[i]));
							}
						}
					}
					//If we cant set it, log the error.
					catch(InvocationTargetException ex)
					{
						RobotLogger.getInstance().warn("Unable to set property \"%s\" to \"%s\". Target type was %s[].", key, value, current.getType().getComponentType().getSimpleName());
						RobotLogger.getInstance().exc("RobotConfigFileReader.runRobotFileReader()", ex);
						continue;
					}
					//update the field.
					current.set(null, elementArray);
				}
				//If we are dealing with a single value....
				else
				{
					try
					{
						//set it, with a cast if necessary.
						current.set(null, current.getType() == String.class ? value : primitiveMap.get(current.getType()).invoke(null, value));
					}
					catch(InvocationTargetException ex)
					{
						RobotLogger.getInstance().warn("Unable to set property \"%s\" to \"%s\". Target type was %s.", key, value, current.getType().getSimpleName());
						RobotLogger.getInstance().exc("RobotConfigFileReader.runRobotFileReader()", ex);
						continue;
					}
				}
			}
		}
		//Deal with misc errors.
		catch(IllegalArgumentException ex)
		{
			RobotLogger.getInstance().exc("Exception caught in runRobotFileReader()", ex);
		}
		catch(IllegalAccessException ex)
		{
			RobotLogger.getInstance().exc("Exception caught in runRobotFileReader()", ex);
		}
		catch(SecurityException ex)
		{
			RobotLogger.getInstance().exc("Exception caught in runRobotFileReader()", ex);
		}
		RobotLogger.getInstance().info("Finished Config Update.");
	}
}