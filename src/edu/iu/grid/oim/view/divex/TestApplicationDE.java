package edu.iu.grid.oim.view.divex;

import com.webif.divex.DivEx;

public class TestApplicationDE extends DivEx {
	public Integer count = 0;
	public DivEx display = new CounterDE(this);
	public DivEx plusbutton = new PlusButtonDE(this);
	
	public String toHTML() 
	{
		String out = "";
		out += plusbutton.render();
		out += display.render();
		return out;
	}
}
