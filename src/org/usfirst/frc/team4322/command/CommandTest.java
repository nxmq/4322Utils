package org.usfirst.frc.team4322.command;

import java.util.Calendar;

public class CommandTest
{
    public static void main(String[] args)
    {
        Scheduler.getInstance();
        Command a = CommandBuilder.create().execute((cmd)-> System.out.println("A!")).runForTime(1000).build();
        Command b = CommandBuilder.create().execute((cmd)-> System.out.println("B!")).runForTime(1500).build();
        Command c = CommandBuilder.create().execute((cmd)-> System.out.println("C!")).runForTime(500).build();
        Command d = CommandBuilder.create().execute((cmd)-> System.out.println("D!")).runForTime(2000).build();
        Command e = CommandBuilder.create().execute((cmd)-> System.out.println("E!")).runForTime(750).build();
        Command f = CommandBuilder.create().execute((cmd)-> System.out.println("F!")).runForTime(250).build();
        Command g = CommandBuilder.create().execute((cmd)-> System.out.println("G!")).runForTime(250).build();
        CommandGroup cg = new CommandGroup();
        cg.addParallel(a);
        cg.addParallel(b);
        cg.addParallel(c);
        cg.addSequential(d);
        cg.addSequential(e);
        cg.addSequential(RouterBuilder.build(()-> {
        if( Calendar.getInstance().get(Calendar.MINUTE) % 2 == 0)
        {
            return f;
        }
        else
        {
            return g;
        }}));
        cg.start();
        while(!cg.isFinished());
        System.out.println("Done!!");
        Scheduler.getInstance().shutdown();
    }
}
