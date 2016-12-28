package org.usfirst.frc.team4322.eventscript;

/**
 * Created by teresamachado on 11/10/16.
 */
public class Outcome
{
	State next;
	Event trigger;

	public Outcome(State next, Event trigger)
	{
		this.next = next;
		this.trigger = trigger;
	}

	public State getNext()
	{
		return next;
	}

	public void setNext(State next)
	{
		this.next = next;
	}
}
