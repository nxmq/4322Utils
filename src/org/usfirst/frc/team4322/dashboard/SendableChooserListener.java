package org.usfirst.frc.team4322.dashboard;

import edu.wpi.first.networktables.*;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import org.usfirst.frc.team4322.logging.RobotLogger;

import java.lang.reflect.Field;

/**
 * Created by nicolasmachado on 9/3/16.
 */
public class SendableChooserListener implements TableEntryListener
{
	private Field backingField;
	private SendableChooser backingChooser;

	public SendableChooserListener(SendableChooser sc, Field field, String name)
	{
		backingField = field;
		backingChooser = sc;
		NetworkTableInstance.getDefault().getTable("SmartDashboard").getSubTable(name).addEntryListener("selected",this,TableEntryListener.kUpdate);
		RobotLogger.getInstance().debug("Field with name %s has been bound to a SendableChooserListener",field.getName());
	}


	@Override
	public void valueChanged(NetworkTable source, String key, NetworkTableEntry entry, NetworkTableValue value, int flags)
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
}
