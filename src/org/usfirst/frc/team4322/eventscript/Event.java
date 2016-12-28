package org.usfirst.frc.team4322.eventscript;

/**
 * Created by teresamachado on 11/10/16.
 */
public class Event
{
	private int id;
	private String name;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Event(int id, String name)
	{
		this.id = id;
		this.name = name;
	}
}
