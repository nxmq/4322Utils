package org.usfirst.frc.team4322.command;
import java.util.ArrayList;
import java.util.function.*;

/**
 * Created by nicolasmachado on 4/20/16.
 */
public abstract class Command implements Runnable
{

	private boolean started = false;
	private boolean hasRun = false;
	private long startTime;
	private long timeout = 0;
	private ArrayList<Subsystem> subsystems = new ArrayList<>();

	public Command(ArrayList<Subsystem> subsystems, long timeout)
	{
		this.timeout = timeout;
		this.subsystems = subsystems;
	}

	public Command()
	{

	}

	protected void setTimeout(long millis)
	{
		this.timeout = millis;
	}

	public ArrayList<Subsystem> getSubsystems()
	{
		return subsystems;
	}

    protected abstract void initialize();

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

	public void cancel()
	{
		end();
		Scheduler.getInstance().remove(this);
	}

    protected abstract void end();

    protected abstract void interrupted();

	public void start()
	{
		startTime = System.currentTimeMillis();
		started = true;
		initialize();
		Scheduler.getInstance().add(this);
	}
	public long runTimeMillis()
	{
		return System.currentTimeMillis() - startTime;
	}

	protected abstract boolean isFinished();

	public boolean shouldRun()
	{
		if(timeout > 0)
		{
			return !isFinished() && timeout < runTimeMillis();
		}
		return !isFinished();
	}

    protected abstract void execute();

	public void run()
	{
		if(shouldRun())
		{
			execute();
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
				execute();
				hasRun = true;
			}
			cancel();
		}

	}
	void interrupt()
	{
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
