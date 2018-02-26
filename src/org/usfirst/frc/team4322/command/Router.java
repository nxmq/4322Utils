package org.usfirst.frc.team4322.command;

import java.util.function.*;
/**
 * Created by nicolasmachado on 4/20/16.
 */
public abstract class Router extends Command
{
	public Router()
	{

	}
	protected abstract Command route();

	@Override
	public void start()
	{
		route().start();
	}

	@Override
	protected void initialize()
	{

	}

	@Override
	protected void end()
	{

	}

	@Override
	protected void interrupted()
	{

	}

	@Override
	protected boolean isFinished()
	{
		return false;
	}

	@Override
	protected void execute()
	{

	}
}
