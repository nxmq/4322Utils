package org.usfirst.frc.team4322.command;

import java.util.function.*;
/**
 * Created by nicolasmachado on 4/20/16.
 */
public class Router
{
	private Supplier<Command> cond;
	public Router(Supplier<Command> cond)
	{
		this.cond = cond;
	}
	private void route()
	{
		cond.get().start();
	}
	public Command getRouteCommand()
	{
		return CommandBuilder.create().task( (c) -> this.route()).build();
	}

}
