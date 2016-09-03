package org.usfirst.frc.team4322.command;

import java.util.ArrayList;

/**
 * Created by nicolasmachado on 4/20/16.
 */
public class CommandGroup extends Command
{
	private ArrayList<Task> queue = new ArrayList<>();

	private class Task extends Command
	{
		boolean parallel;
		private ArrayList<Command> toDo = new ArrayList<>();

		public void add(Command c)
		{
			toDo.add(c);
		}
		public void run()
		{
			for(Command c : toDo)
			{
				if(!c.isStarted())
				{
					c.start();
				}
			}
			toDo.removeIf(Command::isFinished);
			if(toDo.isEmpty())
			{
				end();
			}
		}

		@Override
		public void end()
		{
			super.end();
			queue.remove(this);
		}

		@Override
		public void interrupted()
		{
		}

		@Override
		public boolean shouldRun()
		{
			return toDo.size() > 0;
		}

		@Override
		public boolean isFinished()
		{
			return toDo.size() == 0;
		}
	}
	@Override
	public boolean isFinished()
	{
		return queue.size() == 0;
	}

	@Override
	public boolean shouldRun()
	{
		return !isFinished();
	}

	public void addSequential(Command c)
	{
			Task t = new Task();
			t.parallel = false;
			t.add(c);
			queue.add(t);
	}

	public void addParallel(Command c)
	{
		if(queue.size() < 1 || !queue.get(queue.size()-1).parallel)
		{
			Task t = new Task();
			t.parallel = true;
			t.add(c);
			queue.add(t);
		}
		else
		{
			queue.get(queue.size() - 1).add(c);
		}
	}
	@Override
	public void run()
	{
		if(queue.size() > 0)
		{
			if(!queue.get(0).isFinished())
			{
				queue.get(0).run();
			}
			else
			{
				queue.remove(0);
			}
		}
		else
		{
			end();
		}
	}

}
