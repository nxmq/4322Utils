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

		@Override
        protected void initialize()
		{
			for(Command c : toDo)
			{
				if(!c.isStarted())
				{
					c.start();
				}
			}
		}

		@Override
        protected void execute()
		{
			toDo.removeIf((cmd)->!cmd.shouldRun());
		}

        @Override
        protected void end()
        {
            queue.remove(this);
        }

        @Override
        protected void interrupted()
        {

        }

        @Override
        protected boolean isFinished()
        {
            return toDo.size() == 0;
        }
    }

    public CommandGroup()
    {
        setTimeout(-1);
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
    protected void execute()
	{
		if(queue.size() > 0)
		{
			if(!queue.get(0).isStarted())
			{
				queue.get(0).start();
			}
			else
            {
                if(!queue.get(0).shouldRun())
                {
                    queue.remove(0);
                    if(queue.size() > 0)
                        queue.get(0).start();
                    else
                        cancel();
                }
            }
		}
		else
		{
			cancel();
		}
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
        return queue.size()==0;
    }
}
