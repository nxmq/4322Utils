package org.usfirst.frc.team4322.command;

/**
 * Created by nicolasmachado on 4/20/16.
 */
public class Subsystem
{
	public Subsystem()
	{
		Scheduler.getInstance().systems.add(this);
	}
	public Command getDefaultCommand()
	{
		return null;
	}
}
