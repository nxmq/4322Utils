package org.usfirst.frc.team4322.dashboard;

import edu.wpi.first.networktables.*;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

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

public class MapSynchronizer
{

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


	public static MapSynchronizer getInstance()
	{
		return _instance;
	}

	private class KeyListener implements TableEntryListener
	{
	    private final Field store;
	    private final Class<?> type;
        private final boolean persistent;

        public KeyListener(Field store,boolean persistent)
        {
            this.store = store;
            type = store.getType();
            this.persistent = persistent;
        }

        @Override
		public void valueChanged(NetworkTable table, String key, NetworkTableEntry entry, NetworkTableValue value, int flags)
		{
			try
			{

				if(type.isPrimitive())
				{
					RobotLogger.getInstance().debug("Reflection field is a primitive!");
					if(type == int.class)
					{
                        store.setInt(null,(int)value.getDouble());
					}
					else if(type == byte.class)
					{
                        store.setByte(null,(byte)value.getDouble());
					}
					else if(type == boolean.class)
					{
                        store.set(null,value.getBoolean());
					}
					else if(type == short.class)
					{
                        store.setShort(null,(short)value.getDouble());
					}
					else if(type == long.class)
					{
                        store.setLong(null,(long)value.getDouble());
					}
					else if(type == float.class)
					{
                        store.setFloat(null,(float)value.getDouble());
					}
					else if(type == double.class)
					{
                        store.setDouble(null,value.getDouble());
					}
				}
				else
				{
					store.set(null, value);
				}
				RobotLogger.getInstance().info("Setting field \"%s\" to \"%s\".",key,value.toString());
				if(persistent)
				{
					if(type.isArray())
					{
						RobotPersistenceFileWriter.getInstance().set(key, Arrays.toString((Object[])store.get(null)).replace('[','{').replace(']','}'));
					}
					else
					{
						RobotPersistenceFileWriter.getInstance().set(key,store.get(null).toString());
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
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
                    NetworkTableInstance.getDefault().getTable("SmartDashboard").addEntryListener(field,new KeyListener(f,persistent),TableEntryListener.kUpdate);
				}
				else if(type == long.class || type == int.class || type == short.class || type == byte.class)
				{
					SmartDashboard.putNumber(field, f.getLong(null));
                    NetworkTableInstance.getDefault().getTable("SmartDashboard").addEntryListener(field,new KeyListener(f,persistent),TableEntryListener.kUpdate);
				}
				else if(type == boolean.class)
				{
					SmartDashboard.putBoolean(field, f.getBoolean(null));
                    NetworkTableInstance.getDefault().getTable("SmartDashboard").addEntryListener(field,new KeyListener(f,persistent),TableEntryListener.kUpdate);
				}
				else if(type == String.class)
				{
					SmartDashboard.putString(field, (String)f.get(null));
                    NetworkTableInstance.getDefault().getTable("SmartDashboard").addEntryListener(field,new KeyListener(f,persistent),TableEntryListener.kUpdate);

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
					new SendableChooserListener(enumChooser,f,field);
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
