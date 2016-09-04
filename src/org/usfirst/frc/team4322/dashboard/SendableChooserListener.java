package org.usfirst.frc.team4322.dashboard;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import org.usfirst.frc.team4322.logging.RobotLogger;

import java.lang.reflect.Field;

/**
 * Created by nicolasmachado on 9/3/16.
 */
public class SendableChooserListener implements ITableListener
{
	private Field backingField;
	private SendableChooser backingChooser;

	public SendableChooserListener(SendableChooser sc, Field field)
	{
		backingField = field;
		backingChooser = sc;
		backingChooser.getTable().addTableListener(this);
		RobotLogger.getInstance().debug("Field with name %s has been bound to a SendableChooserListener",field.getName());
	}

	@Override
	public void valueChanged(ITable source, String key, Object value, boolean isNew)
	{
		if(key.equals("selected"))
		{
			try
			{
				backingField.set(null,backingChooser.getSelected());
				RobotLogger.getInstance().debug("Set field %s to %s.\n",backingField.getName(),backingChooser.getSelected().toString());
			}
			catch(IllegalAccessException e)
			{
				RobotLogger.getInstance().exc("Exception in SendableChooserListener:",e);
			}
		}
		else
		{
			RobotLogger.getInstance().debug("Unknown Table Member %s changed!",key);
		}
	}

	@Override
	public void valueChangedEx(ITable source, String key, Object value, int flags)
	{
		valueChanged(source,key,value,false);
	}
}
