package org.usfirst.frc.team4322.dashboard;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

import java.lang.reflect.Field;
import java.util.HashMap;
import org.usfirst.frc.team4322.logging.RobotLogger;

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

	private HashMap<String,Field> valMap = new HashMap<>();
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

				if(valMap.get(key).getType().isEnum())
				{
					RobotLogger.getInstance().info("Setting field \"%s\" to \"%s.\".",key,((SendableChooser)value).getSelected().toString());
					valMap.get(key).set(null,((SendableChooser)value).getSelected());
				}
				else
				{
					if(valMap.get(key).getType().isPrimitive())
					{
						RobotLogger.getInstance().debug("Reflection field is a primitive!");
						if(valMap.get(key).getType() == int.class)
						{
							valMap.get(key).setInt(null,((Double)value).intValue());
						}
						else if(valMap.get(key).getType() == byte.class)
						{
							valMap.get(key).setByte(null,((Double)value).byteValue());
						}
						else if(valMap.get(key).getType() == boolean.class)
						{
							valMap.get(key).set(null,value);
						}
						else if(valMap.get(key).getType() == short.class)
						{
							valMap.get(key).setShort(null,((Double)value).shortValue());
						}
						else if(valMap.get(key).getType() == long.class)
						{
							valMap.get(key).setLong(null,((Double)value).longValue());
						}
						else if(valMap.get(key).getType() == float.class)
						{
							valMap.get(key).setFloat(null,((Double)value).floatValue());
						}
						else if(valMap.get(key).getType() == double.class)
						{
							valMap.get(key).setDouble(null,((Double)value).doubleValue());
						}
					}
					else
					{
						valMap.get(key).set(null, value);
					}
					RobotLogger.getInstance().info("Setting field \"%s\" to \"%s\".",key,value.toString());
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

	public void link(Class<?> robotMap)
	{
		for( Field f : robotMap.getFields())
		{
			if(!f.isAnnotationPresent(DashboardInputField.class))
			{
				continue;
			}
			DashboardInputField field = f.getAnnotation(DashboardInputField.class);
			if(f.getType().isArray())
			{
				RobotLogger.getInstance().err("Arrays are not supported by MapUtils at this time. The field %s will not be synchronized with the SmartDashboard.", f.getName());
			}
			try
			{
				if(f.getType() == double.class || f.getType() == float.class)
				{
					SmartDashboard.putNumber(field.field(), f.getDouble(null));
					valMap.put(field.field(),f);

				}
				else if(f.getType() == long.class || f.getType() == int.class || f.getType() == short.class || f.getType() == byte.class)
				{
					SmartDashboard.putNumber(field.field(), f.getLong(null));
					valMap.put(field.field(),f);

				}
				else if(f.getType() == boolean.class)
				{
					SmartDashboard.putBoolean(field.field(), f.getBoolean(null));
					valMap.put(field.field(),f);

				}
				else if(f.getType() == String.class)
				{
					SmartDashboard.putString(field.field(), (String)f.get(null));
					valMap.put(field.field(),f);

				}
				else if(f.getType().isEnum())
				{
					SendableChooser enumChooser = new SendableChooser();
					for(int i = 0; i < f.getType().getEnumConstants().length; i++)
					{
						enumChooser.addObject(f.getType().getEnumConstants()[i].toString(),f.getType().getEnumConstants()[i]);
					}
					SmartDashboard.putData(field.field(), enumChooser);
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
