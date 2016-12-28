package org.usfirst.frc.team4322.dashboard;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.usfirst.frc.team4322.configuration.RobotPersistenceFileWriter;
import org.usfirst.frc.team4322.logging.RobotLogger;

import static org.usfirst.frc.team4322.configuration.RobotConfigFileReader.primitiveMap;

/**
 * Created by nicolasmachado on 3/30/16.
 */

public class MapSynchronizer implements ITableListener
{


	//Values take from ntcore_c.h
	private static int NT_UNASSIGNED = 0;
	private static int NT_BOOLEAN = 0x01;
	private static int NT_DOUBLE = 0x02;
	private static int NT_STRING = 0x04;

	private class FieldInfo
	{
		boolean persistent;
		Field field;

		public FieldInfo(boolean persistent, Field field)
		{
			this.persistent = persistent;
			this.field = field;
		}
	}

	private HashMap<String,FieldInfo> valMap = new HashMap<>();
	private static MapSynchronizer _instance = new MapSynchronizer();


	private MapSynchronizer()
	{
		NetworkTable.getTable("SmartDashboard").addTableListener(this);
	}
	public static MapSynchronizer getInstance()
	{
		return _instance;
	}

	@Override
	public void valueChanged(ITable source, String key, Object value, boolean isNew)
	{
		if(valMap.containsKey(key))
		{
			try
			{
				Field field = valMap.get(key).field;
				Class<?> type = field.getType();
				if(type.isEnum())
				{
					RobotLogger.getInstance().info("Setting field \"%s\" to \"%s.\".",key,((SendableChooser)value).getSelected().toString());
					field.set(null,((SendableChooser)value).getSelected());
				}
				else
				{
					if(type.isPrimitive())
					{
						RobotLogger.getInstance().debug("Reflection field is a primitive!");
						if(type == int.class)
						{
							field.setInt(null,((Double)value).intValue());
						}
						else if(type == byte.class)
						{
							field.setByte(null,((Double)value).byteValue());
						}
						else if(type == boolean.class)
						{
							field.set(null,value);
						}
						else if(type == short.class)
						{
							field.setShort(null,((Double)value).shortValue());
						}
						else if(type == long.class)
						{
							field.setLong(null,((Double)value).longValue());
						}
						else if(type == float.class)
						{
							field.setFloat(null,((Double)value).floatValue());
						}
						else if(type == double.class)
						{
							field.setDouble(null,((Double)value).doubleValue());
						}
					}
					else
					{
						field.set(null, value);
					}
					RobotLogger.getInstance().info("Setting field \"%s\" to \"%s\".",key,value.toString());
				}
				if(valMap.get(key).persistent)
				{
					if(type.isArray())
					{
						RobotPersistenceFileWriter.getInstance().set(key, Arrays.toString((Object[])field.get(null)).replace('[','{').replace(']','}'));
					}
					else
					{
						RobotPersistenceFileWriter.getInstance().set(key,field.get(null).toString());
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			RobotLogger.getInstance().debug("Unknown key %s changed!",key);
		}
	}

	@Override
	public void valueChangedEx(ITable source, String key, Object value, int flags)
	{
		valueChanged(source,key,value,false);
	}

	public void loadPersistentValues()
	{
		try(FileInputStream fstream = new FileInputStream(RobotPersistenceFileWriter.CONFIG_FILE))
		{
			Properties p = new Properties();
			p.load(fstream);
			p.forEach((k,v) ->
			{
				if(valMap.containsKey(k))
				{
					Field current = valMap.get(k).field;
					try
					{
						current.set(null, current.getType() == String.class ? v : primitiveMap.get(current.getType()).invoke(null, v));
					}
					catch (IllegalAccessException | InvocationTargetException e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void link(Class<?> robotMap)
	{
		for( Field f : robotMap.getFields())
		{
			boolean persistent;
			String field;
			if(f.isAnnotationPresent(DashboardInputField.class))
			{
				persistent = false;
				field = f.getAnnotation(DashboardInputField.class).field();
			}
			else if(f.isAnnotationPresent(PersistentDashboardInputField.class))
			{
				persistent = true;
				field = f.getAnnotation(PersistentDashboardInputField.class).field();
			}
			else
			{
				continue;
			}
			Class<?> type = f.getType();
			if(type.isArray())
			{
				RobotLogger.getInstance().err("Arrays are not supported by MapUtils at this time. The field %s will not be synchronized with the SmartDashboard.", f.getName());
			}
			try
			{
				FieldInfo fi = new FieldInfo(persistent,f);
				if(type == double.class || type == float.class)
				{
					SmartDashboard.putNumber(field, f.getDouble(null));
					valMap.put(field,fi);

				}
				else if(type == long.class || type == int.class || type == short.class || type == byte.class)
				{
					SmartDashboard.putNumber(field, f.getLong(null));
					valMap.put(field,fi);

				}
				else if(type == boolean.class)
				{
					SmartDashboard.putBoolean(field, f.getBoolean(null));
					valMap.put(field,fi);

				}
				else if(type == String.class)
				{
					SmartDashboard.putString(field, (String)f.get(null));
					valMap.put(field,fi);

				}
				else if(type.isEnum())
				{
					if(persistent)
					{
						RobotLogger.getInstance().err("SendableChoosers cannot be persistent. Sorry.");
						continue;
					}
					SendableChooser enumChooser = new SendableChooser();
					for(int i = 0; i < type.getEnumConstants().length; i++)
					{
						enumChooser.addObject(type.getEnumConstants()[i].toString(),type.getEnumConstants()[i]);
					}
					SmartDashboard.putData(field, enumChooser);
					new SendableChooserListener(enumChooser,f);
				}
				else
				{
					RobotLogger.getInstance().err("The type of field %s is unsupported by MapUtils at this time. This will not be synchronized with the SmartDashboard.", f.getName());
				}

			}
			catch(IllegalAccessException ex)
			{
				RobotLogger.getInstance().err("MapUtils.initUpdater()",ex);
			}
		}
	}
}
