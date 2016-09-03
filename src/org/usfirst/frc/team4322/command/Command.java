package org.usfirst.frc.team4322.command;
import java.util.ArrayList;
import java.util.function.*;

/**
 * Created by nicolasmachado on 4/20/16.
 */
public class Command implements Runnable
{

	private Predicate<Command> cond = ((c)->false);
	private Consumer<Command> act = ((c)->{});
	private boolean finished = false;
	private boolean started = false;
	private boolean hasRun = false;
	private long startTime;
	private long timeout = 0;
	private Consumer<Command> onEnd = (c) -> {};
	private Consumer<Command> onInt = (c) -> {};
	ArrayList<Subsystem> subsystems = new ArrayList<>();

	public Command(Predicate<Command> cond, Consumer<Command> act, ArrayList<Subsystem> subsystems, long timeout, Consumer<Command> onEnd, Consumer<Command> onInt)
	{
		this.cond = cond;
		this.act = act;
		this.timeout = timeout;
		this.onEnd = onEnd;
		this.onInt = onInt;
		this.subsystems = subsystems;
	}

	public Command(Predicate<Command> cond, Consumer<Command> act, ArrayList<Subsystem> subsystems, long timeout)
	{
		this.act = act;
		this.subsystems = subsystems;
		this.cond = cond;
		this.timeout = timeout;
	}


	public Command(Predicate<Command> cond,Consumer<Command> act, ArrayList<Subsystem> subsystems)
	{
		this.act = act;
		this.subsystems = subsystems;
		this.cond = cond;
	}


	public Command(Predicate<Command> cond, Consumer<Command> act)
	{
		this.cond = cond;
		this.act = act;
	}
	public void require(Subsystem s)
	{
		subsystems.add(s);
	}
	public void require(Subsystem... s)
	{
		for(Subsystem sys : s)
		{
			subsystems.add(sys);
		}
	}
	public Command()
	{

	}
	public void end()
	{
		onEnd.accept(this);
		Scheduler.getInstance().remove(this);
	}
	public void interrupted()
	{
		onInt.accept(this);
		interrupt();
	}

	public void start()
	{
		startTime = System.currentTimeMillis();
		started = true;
		Scheduler.getInstance().add(this);
	}
	public long runTimeMillis()
	{
		return System.currentTimeMillis() - startTime;
	}
	public boolean shouldRun()
	{
		if(timeout != 0)
		{
			return !cond.test(this) && timeout < runTimeMillis();
		}
		return cond.test(this);
	}
	public boolean isFinished()
	{
		return finished;
	}
	public void run()
	{
		if(shouldRun())
		{
			act.accept(this);
			if(!hasRun)
			{
				hasRun = true;
			}
		}
		else
		{
			//All commands must run once.
			if(!hasRun)
			{
				act.accept(this);
				hasRun = true;
			}
			finished = true;
			end();
		}

	}
	void interrupt()
	{
		this.finished = true;
		interrupted();
	}

	public boolean isStarted()
	{
		return started;
	}

	public long getStartTime()
	{
		return startTime;
	}
}
