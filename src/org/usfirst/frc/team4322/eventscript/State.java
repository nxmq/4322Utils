package org.usfirst.frc.team4322.eventscript;

import java.util.ArrayList;

/**
 * Created by teresamachado on 11/10/16.
 */
public class State
{
	private int id;
	private String name;
	private ArrayList<Outcome> results = new ArrayList<>();

	public void addOutcome(Outcome out)
	{
		results.add(out);
	}

	public ArrayList<Outcome> getOutcomes()
	{
		return results;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public State()
	{

	}
	public State(int id, String name)
	{

		this.id = id;
		this.name = name;
	}
}
