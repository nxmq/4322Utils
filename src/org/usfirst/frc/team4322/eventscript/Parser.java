package org.usfirst.frc.team4322.eventscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by teresamachado on 11/10/16.
 */
public class Parser
{
	private ArrayList<State> states = new ArrayList<>();
	private ArrayList<Event> events = new ArrayList<>();
	private HashMap<String, Integer> symbols = new HashMap<>();
	private Pattern state = Pattern.compile("\\s*state\\s*?:\\s*?(?:\\s*?(\\w+)\\s*?(?:,(?:\\s*)?|$))+?");
	private Pattern event = Pattern.compile("\\s*event\\s*?:\\s*?(?:\\s*?(\\w+)\\s*?(?:,(?:\\s*)?|$))+?");
	private Pattern expr = Pattern.compile("(?:\\s*?(\\w+?)\\s*?:\\s*?)?(?:((?:\\((?:\\s*?\\w+?\\s*?(?:,|))*?\\))|\\w+)\\s*?)->\\s*?((?:\\((?:\\s*?\\w+?\\s*?(?:,|))*?\\))|\\w+)");

	public void parse(String in)
	{
		String[] lines = in.split("\n");
		if(!state.matcher(lines[0]).matches())
		{
			System.err.println("Error: No states declared.");
			System.exit(-1);
		}
		String[] stlist = lines[0].replaceAll("(\\s*state\\s*:|\\s)","").split(",");
		for(int i = 0; i < stlist.length; i++)
		{
			State st = new State(this.states.size(),stlist[i]);
			this.states.add(st);
			symbols.put(st.getName(),st.getId());
		}
		if(!event.matcher(lines[1]).matches())
		{
			System.err.println("Error: No events declared.");
			System.exit(-1);
		}
		String[] evlist = lines[1].replaceAll("(\\s*event\\s*:|\\s)","").split(",");
		this.events.ensureCapacity(evlist.length+stlist.length);
		for(int i = 0; i < evlist.length; i++)
		{
			Event en = new Event(this.events.size(),evlist[i]);
			this.events.add(en);
			symbols.put(en.getName(),en.getId());
		}
		for(int i = 2; i < lines.length;i++)
		{
			Matcher exp = expr.matcher(lines[i]);
			while(exp.find())
			{
				int st = exp.start(3);

				if(exp.group(3).matches("\\(.+\\)"))
				{
					Parallel pr = new Parallel();
					pr.setId(states.size());
					pr.setName(exp.group(3)+"_impl_parallel");
					symbols.put(exp.group(3),states.size());
					states.add(pr);
					for(String gr : exp.group(3).replaceAll("(\\(|\\)|\\s)","").split(","))
					{
						pr.addState(this.states.get(symbols.get(gr)));
					}
					State j = new State(states.size(),exp.group(3)+"_impl_join");
					symbols.put(j.getName(),j.getId());
					states.add(j);
					pr.addOutcome(new Outcome(j,null));
				}
				State targ;
				if(exp.group(2).matches("\\(.+\\)"))
				{
					if(!this.symbols.containsKey(exp.group(2)))
					{

						Parallel pr = new Parallel();
						pr.setId(states.size());
						pr.setName(exp.group(2)+"_impl_parallel");
						symbols.put(exp.group(2),states.size());
						states.add(pr);
						for(String gr : exp.group(2).replaceAll("(\\(|\\)|\\s)","").split(","))
						{
							pr.addState(this.states.get(symbols.get(gr)));
						}
						State j = new State(states.size(),exp.group(2)+"_impl_join");
						symbols.put(j.getName(),j.getId());
						states.add(j);
						pr.addOutcome(new Outcome(j,null));
					}
					targ = this.states.get(symbols.get(exp.group(2))).getOutcomes().get(0).next;
				}
				else
				{
					targ = this.states.get(symbols.get(exp.group(2)));
				}
				if (exp.group(1)!=null)
				{
					targ.addOutcome(new Outcome(this.states.get(symbols.get(exp.group(3))),
							this.events.get(symbols.get(exp.group(1)))));
				}
				else
				{
					targ.addOutcome(new Outcome(this.states.get(symbols.get(exp.group(3))), null));
				}

				exp = exp.region(st,lines[i].length());
			}
		}
	}

	public void printStates()
	{
		for(int i = 0; i<states.size();i++)
		{
			System.out.printf("State %s leads to :\n",this.states.get(i).getName());
			for(int j = 0; j < states.get(i).getOutcomes().size();j++)
			{
				Outcome o = states.get(i).getOutcomes().get(j);
				if(o.trigger == null)
				{
					System.out.printf("State %s\n",o.next.getName());
				}
				else
				{
					System.out.printf("State %s when Event %s occurs.\n",o.next.getName(),o.trigger.getName());
				}
			}
		}
	}
}
