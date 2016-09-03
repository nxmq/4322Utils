package org.usfirst.frc.team4322.command;

/**
 * Created by nicolasmachado on 4/20/16.
 */
public class Main
{
	public static void main(String[] args)
	{
		Subsystem s = new Subsystem();
		Subsystem y = new Subsystem();
		CommandGroup g = new CommandGroup();
		g.addSequential(CommandBuilder.create()
				.require(s)
				.runForTime(5000)
				.task((c) -> System.out.println("Ran "+ c.runTimeMillis()))
				.build());
		g.addSequential(CommandBuilder.create()
				.require(y)
				.runForTime(8000)
				.task((c) -> System.out.println("Walked  "+ c.runTimeMillis()))
				.build());
		g.addParallel(CommandBuilder.create()
				.require(s)
				.runForTime(5000)
				.task((c) -> System.out.println("Sprinted  "+ c.runTimeMillis()))
				.build());
		g.addParallel(CommandBuilder.create()
				.require(y)
				.runForTime(2000)
				.task((c) -> System.out.println("Jumped  "+ c.runTimeMillis()))
				.build());
		g.addSequential(CommandBuilder.create()
				.require(s)
				.runForTime(8000)
				.task((c) -> System.out.println("Climbed  "+ c.runTimeMillis()))
				.build());
		g.start();

	}
}
