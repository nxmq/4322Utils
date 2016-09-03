package org.usfirst.frc.team4322.dashboard;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

	private static HashMap<Class<?>,Method> primitiveMap = new HashMap<Class<?>,Method>();
	static {
		try {
			primitiveMap.put(boolean.class, Boolean.class.getMethod("parseBoolean",String.class));
			primitiveMap.put(byte.class, Byte.class.getMethod("parseByte",String.class));
			primitiveMap.put(short.class, Short.class.getMethod("parseShort",String.class));
			primitiveMap.put(int.class, Integer.class.getMethod("parseInt",String.class));
			primitiveMap.put(long.class, Long.class.getMethod("parseLong",String.class));
			primitiveMap.put(float.class, Float.class.getMethod("parseFloat",String.class));
			primitiveMap.put(double.class, Double.class.getMethod("parseDouble",String.class));
		} catch (NoSuchMethodException | SecurityException ex) {
			RobotLogger.getInstance().err("Exception caught in MapSynchronizer", ex);
		}
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
					valMap.get(key).set(null,((SendableChooser)value).getSelected());
				}
				else
				{
				valMap.get(key).set(null, value);
				}

			}
			catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void valueChangedEx(ITable source, String key, Object value, int flags)
	{
		if(valMap.containsKey(key))
		{
			try
			{
				valMap.get(key).set(null,value);
			}
			catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void link(Class<?> robotMap)
	{
		for( Field f : robotMap.getFields())
		{
			if(!f.isAnnotationPresent(DashboardField.class))
			{
				continue;
			}
			DashboardField field = f.getAnnotation(DashboardField.class);
			if(f.getType().isArray())
			{
				RobotLogger.getInstance().err("Arrays are not supported by MapUtils at this time. The field %s will not be synchronized with the SmartDashboard.", f.getName());
			}
			try
			{
				if(f.getType() == double.class || f.getType() == float.class)
				{
					SmartDashboard.putNumber(field.field(), f.getDouble(null));
				}
				else if(f.getType() == long.class || f.getType() == int.class || f.getType() == short.class || f.getType() == byte.class)
				{
					SmartDashboard.putNumber(field.field(), f.getLong(null));
				}
				else if(f.getType() == boolean.class)
				{
					SmartDashboard.putBoolean(field.field(), f.getBoolean(null));
				}
				else if(f.getType() == String.class)
				{
					SmartDashboard.putString(field.field(), (String)f.get(null));
				}
				else if(f.getType().isEnum())
				{
					SendableChooser enumChooser = new SendableChooser();
					for(int i = 0; i < f.getType().getEnumConstants().length; i++)
					{
						enumChooser.addObject(f.getType().getEnumConstants()[i].toString(),f.getType().getEnumConstants()[i]);
					}
					SmartDashboard.putData(field.field(), enumChooser);
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
			valMap.put(field.field(),f);
		}
	}
}
