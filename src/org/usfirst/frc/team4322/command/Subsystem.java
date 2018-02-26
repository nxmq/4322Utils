package org.usfirst.frc.team4322.command;

/**
 * Created by nicolasmachado on 4/20/16.
 */
public abstract class Subsystem
{
	private Command defaultCommand;

	public Subsystem()
	{
		Scheduler.getInstance().systems.add(this);
	}

	abstract void initDefaultCommand();

	private void setDefaultCommand(Command cmd)
	{
		defaultCommand = cmd;
	}

	public Command getDefaultCommand()
	{
		return defaultCommand;
	}
}
