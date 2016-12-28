package org.usfirst.frc.team4322.eventscript;

import java.util.ArrayList;

/**
 * Created by teresamachado on 11/10/16.
 */
public class Parallel extends State
{
	private ArrayList<State> states = new ArrayList<>();
	public ArrayList<State> getStates()
	{
		return states;
	}
	public void addState(State st)
	{
		states.add(st);
	}
}
