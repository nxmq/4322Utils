package org.usfirst.frc.team4322.command;

import edu.wpi.first.wpilibj.HLUsageReporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Created by nicolasmachado on 4/20/16.
 */
public class Scheduler
{
	private ScheduledThreadPoolExecutor core;
	private HashMap<Subsystem,Command> systemMap;
	private static Scheduler _inst = new Scheduler();
	ArrayList<Subsystem> systems;
	private Scheduler()
	{
		HLUsageReporting.reportScheduler();
		systems = new ArrayList<>();
		systemMap = new HashMap<>();
		core = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
		core.setRemoveOnCancelPolicy(true);
		core.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		core.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		core.prestartAllCoreThreads();
	}
	public static Scheduler getInstance()
	{
		return _inst;
	}
	public ScheduledFuture add(Command c)
	{
		for(Subsystem s : c.subsystems)
		{
			if(systemMap.get(s) != null)
			{
				System.err.println("Conflict!");
				Command r = systemMap.get(s);
				r.interrupt();
				core.remove(r);
				core.purge();
				systemMap.values().removeIf((x) -> x == r);
			}
				systemMap.put(s,c);
		}
		return core.scheduleWithFixedDelay(c,0,50,TimeUnit.MILLISECONDS);
	}
	public void run()
	{
		for(Subsystem s : systems)
		{
			Command c = s.getDefaultCommand();
			systemMap.putIfAbsent(s, c);
			if(c != null)
			{
				c.start();
			}
		}
	}

	public void remove(Command c)
	{
		core.remove(c);
		core.purge();
		core.getQueue().removeIf((x) -> x== c);
		systemMap.values().removeIf((x) -> x == c);
	}
	public void reset()
	{
		core.shutdownNow();
		core.prestartAllCoreThreads();
	}

}
