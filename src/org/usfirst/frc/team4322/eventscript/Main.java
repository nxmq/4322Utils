package org.usfirst.frc.team4322.eventscript;

/**
 * Created by teresamachado on 11/14/16.
 */
public class Main
{
	public static void main(String[] args)
	{
		Parser p = new Parser();
		p.parse("state : rest, acquire, aim, wait, fire, reset\n" +
				"event : start, cancel, shoot\n" +
				"start : rest -> acquire -> aim -> wait\n" +
				"cancel : wait -> reset \n" +
				"shoot : wait -> fire -> reset\n" +
				"reset -> rest");
		p.printStates();
	}
}
