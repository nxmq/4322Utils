package org.usfirst.frc.team4322.command;

import java.util.function.Supplier;

public class RouterBuilder
{
    public static Router build(Supplier<Command> path)
    {
        return new Router() {
            @Override
            protected Command route()
            {
                return path.get();
            }
        };
    }
}
