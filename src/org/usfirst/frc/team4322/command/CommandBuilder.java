package org.usfirst.frc.team4322.command;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by nicolasmachado on 4/20/16.
 */
public class CommandBuilder
{
	private Consumer<Command> act = ((c)->{});
	private Consumer<Command> init = ((c)->{});
	private Predicate<Command> cond = ((c)->false);
	ArrayList<Subsystem> subsystems = new ArrayList<>();
	private Consumer<Command> onEnd = (c) -> {};
	private Consumer<Command> onInt = (c) -> {};
	long timeout = 0;
	public static CommandBuilder create()
	{
		return new CommandBuilder();
	}
	public CommandBuilder execute(Consumer<Command> act)
	{
		this.act = act;
		return this;
	}

	public CommandBuilder onInit(Consumer<Command> init)
    {
        this.init = init;
        return this;
    }

	public CommandBuilder runForTime(long millis)
	{
		this.cond = (Command c) -> c.runTimeMillis() < millis;
		return this;
	}
	public CommandBuilder withTimeout(long millis)
	{
		this.timeout = millis;
		return this;
	}
	public CommandBuilder runWhile(Predicate<Command> cond)
	{
		this.cond = cond;
		return this;
	}
	public CommandBuilder onEnd(Consumer<Command> onEnd)
	{
		this.onEnd = onEnd;
		return this;
	}
	public CommandBuilder onInterrupt(Consumer<Command> onInt)
	{
		this.onInt = onInt;
		return this;
	}
	public CommandBuilder require(Subsystem s)
	{
		subsystems.add(s);
		return this;
	}
	public CommandBuilder requires(Subsystem... s)
	{
		for(Subsystem sys : s)
		{
			subsystems.add(sys);
		}
		return this;
	}
	public Command build()
	{
		return new Command(subsystems,timeout) {
			@Override
            protected void initialize()
			{
				init.accept(this);
			}

			@Override
            protected void end()
			{
				onEnd.accept(this);
			}

			@Override
            protected void interrupted()
			{
				onInt.accept(this);
			}

			@Override
            protected boolean isFinished()
			{
				return !cond.test(this);
			}

			@Override
            protected void execute()
			{
				act.accept(this);
			}
		};
	}
}
